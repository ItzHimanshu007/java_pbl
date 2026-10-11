# Setup and operation

## Requirements

Java 17, internet access for first dependency download, and MySQL 8 for the normal profile. The checked-in Maven wrapper provides Maven. No separately installed Tomcat is required to run the executable WAR. Local assets include Bootstrap; no CDN is needed at runtime for the UI; map tiles require an internet connection.

## Windows local demonstration

Open PowerShell in the repository root:

```powershell
.\mvnw.cmd verify
.\backend\scripts\run-demo.ps1
```

Open `http://localhost:8080`. Stop with Ctrl+C. Use `.\backend\scripts\run-demo.ps1 -Port 8081` if necessary. If PowerShell blocks the script, use `powershell -ExecutionPolicy Bypass -File .\backend\scripts\run-demo.ps1` for that invocation. The demo profile has in-memory data and a randomly generated process-local signing key. All demo logins stop working when the application restarts. It is for local demonstration only.

On macOS/Linux, run `chmod +x mvnw` after extracting the source, build with `./mvnw verify`, then run `java -jar backend/target/civicpulse.war --spring.profiles.active=demo --server.address=127.0.0.1`. These operating systems were not tested in this release.

## Normal MySQL profile

An administrator of your local MySQL installation creates a dedicated database and user. Choose your own database password; do not commit it.

```sql
CREATE DATABASE civicpulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'civicpulse'@'localhost' IDENTIFIED BY 'replace-with-your-own-database-password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON civicpulse.* TO 'civicpulse'@'localhost';
```

Flyway applies versioned migrations on startup. Never run this application's tests against a database containing real data: the integration-test fixtures remove report/account records from their configured test database.

Set environment variables in the current PowerShell process. Supply credentials interactively rather than putting them in terminal history:

```powershell
$env:CP_DB_URL = 'jdbc:mysql://localhost:3306/civicpulse?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true'
$env:CP_DB_USERNAME = 'civicpulse'
$taskDbCredential = Get-Credential -UserName 'civicpulse' -Message 'Enter the local database password'
$env:CP_DB_PASSWORD = $taskDbCredential.GetNetworkCredential().Password
$taskJwtBytes = New-Object byte[] 32
$taskRandom = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$taskRandom.GetBytes($taskJwtBytes)
$taskRandom.Dispose()
$env:CP_JWT_SECRET = [Convert]::ToBase64String($taskJwtBytes)
$env:CP_ADMIN_EMAIL = 'your-chosen-admin-email@example.com'
$env:CP_ADMIN_NAME = 'Your administrator name'
$taskAdminCredential = Get-Credential -UserName $env:CP_ADMIN_EMAIL -Message 'Choose the first administrator password (at least 10 characters)'
$env:CP_ADMIN_PASSWORD = $taskAdminCredential.GetNetworkCredential().Password
# Local HTTP only. Keep true when serving with HTTPS.
$env:CP_COOKIE_SECURE = 'false'
$taskSocketDir = Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'CivicPulse\sockets'
New-Item -ItemType Directory -Path $taskSocketDir -Force | Out-Null
java "-Djdk.net.unixdomain.tmpdir=$taskSocketDir" -jar .\backend\target\civicpulse.war --server.address=127.0.0.1
```

Keep the same signing key securely between normal-profile restarts. Changing the key invalidates existing tokens. The administrator is created only if the email does not exist; startup refuses to promote an existing citizen account. Remove the bootstrap password from the environment after first provisioning. Citizen registration never accepts a role parameter. Officer account management is delivered in the administration module.

## Deployment behaviour

Use HTTPS and `CP_COOKIE_SECURE=true` for deployed access. The JWT and CSRF cookies are HttpOnly and SameSite=Lax. JWT lifetime is 30 minutes. Database login-session records allow immediate logout revocation and disabled-account checks; this is intentionally not a fully stateless token design. Passwords are BCrypt hashes, not JWT claims.

The `demo` profile explicitly permits insecure cookies for local HTTP only; never expose that profile as a real deployment. Do not disable CSRF because authentication uses JWT. Configure a concrete allowed bind address/reverse proxy for deployment; the local commands bind only to this computer.

## Troubleshooting

- `JAVA_HOME` must point to a JDK 17 installation, not its `bin` directory.
- Build the WAR after source changes; do not run an older packaged file.
- A secure cookie will not accompany plain HTTP requests. Use the local demo profile or explicitly configure local HTTP as described above.
- A normal-profile startup failure about the signing key means `CP_JWT_SECRET` is absent, invalid Base64 or shorter than 32 decoded bytes.
- Database startup failures: verify the configured port, account permissions and dedicated database.
- If 8080 is occupied, add `--server.port=8081` and open that port.
- `/health` is a simple servlet liveness check, not a guarantee that every database operation is available.

## Windows Java temporary-folder compatibility

If Java reports `Unable to establish loopback connection`, its Windows temporary path may use a short filename that cannot be used for a Unix-domain socket. `backend/scripts/run-demo.ps1` selects a dedicated full-path socket directory. You can also supply a short, writable full path with `-Djdk.net.unixdomain.tmpdir=...` before `-jar`. This does not change authentication or network exposure.

## Address service and device permissions

`CP_GEOCODER_URL` configures a Photon-compatible HTTPS endpoint. The default public server supports modest use without an availability guarantee. Address/map requests require internet; failed lookup supports map-pin plus manual landmark. Use HTTPS for camera/geolocation outside localhost. Permission is optional; stored gallery photos do not establish capture location. MySQL receives V500/V600 automatically on upgrade; never edit already applied migrations.
