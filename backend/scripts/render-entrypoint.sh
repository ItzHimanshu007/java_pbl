#!/bin/sh
set -eu

# Do not print connection strings or credentials in deployment logs.
: "${CP_DB_URL:?Set CP_DB_URL to the external MySQL JDBC URL}"
: "${CP_DB_USERNAME:?Set CP_DB_USERNAME}"
: "${CP_DB_PASSWORD:?Set CP_DB_PASSWORD}"
: "${CP_JWT_SECRET:?Set a stable Base64-encoded CP_JWT_SECRET (32 or more decoded bytes)}"

case "$CP_DB_URL" in
  jdbc:mysql://*) ;;
  *) echo "CP_DB_URL must start with jdbc:mysql://" >&2; exit 1 ;;
esac

# Aiven signs its MySQL certificates with a project CA. Build a Connector/J-only
# truststore so the geocoder and other HTTPS requests keep the JVM's normal roots.
# Either paste a Base64-encoded PEM bundle into Render or mount a PEM secret file.
if [ -n "${CP_DB_CA_CERT_BASE64:-}" ] && [ -n "${CP_DB_CA_CERT_FILE:-}" ]; then
  echo "Set only one of CP_DB_CA_CERT_BASE64 and CP_DB_CA_CERT_FILE" >&2
  exit 1
fi

if [ -n "${CP_DB_CA_CERT_BASE64:-}" ] || [ -n "${CP_DB_CA_CERT_FILE:-}" ]; then
  tls_directory="$(mktemp -d "${TMPDIR:-/tmp}/civicpulse-tls.XXXXXX")"
  if [ -n "${CP_DB_CA_CERT_BASE64:-}" ]; then
    if ! printf '%s' "$CP_DB_CA_CERT_BASE64" | base64 --decode > "$tls_directory/ca.pem"; then
      echo "CP_DB_CA_CERT_BASE64 is not valid Base64" >&2
      exit 1
    fi
  else
    if [ ! -r "$CP_DB_CA_CERT_FILE" ]; then
      echo "CP_DB_CA_CERT_FILE does not point to a readable PEM file" >&2
      exit 1
    fi
    cp "$CP_DB_CA_CERT_FILE" "$tls_directory/ca.pem"
  fi

  # Import every certificate in a rotation bundle, rather than only the first one.
  if ! awk -v directory="$tls_directory" '
    /-----BEGIN CERTIFICATE-----/ { count++; writing = 1 }
    writing { print > (directory "/certificate-" count ".pem") }
    /-----END CERTIFICATE-----/ { close(directory "/certificate-" count ".pem"); writing = 0 }
    END { if (count == 0 || writing) exit 1 }
  ' "$tls_directory/ca.pem"; then
    echo "Database CA must be a complete PEM certificate or certificate bundle" >&2
    exit 1
  fi

  for certificate in "$tls_directory"/certificate-*.pem; do
    keytool -importcert -noprompt -alias "$(basename "$certificate" .pem)" \
      -file "$certificate" -keystore "$tls_directory/truststore.p12" \
      -storetype PKCS12 -storepass changeit >/dev/null
  done

  case "$CP_DB_URL" in
    *\?*) separator='&' ;;
    *) separator='?' ;;
  esac
  # The truststore contains public CA certificates, not private keys or credentials.
  CP_DB_URL="${CP_DB_URL}${separator}trustCertificateKeyStoreUrl=file:${tls_directory}/truststore.p12&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit"
  export CP_DB_URL
fi

exec java -jar /app/civicpulse.war "$@"
