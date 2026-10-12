# CivicPulse on Render with a free external MySQL database

Deployment target: **one free Render Docker web service + one free Aiven MySQL service**, starting with an empty database. The JSP frontend and both Java backend modules are packaged together. Do not create separate frontend services or set Render's root directory to `backend`.

The app uses Java 17, Spring Boot 3.5.16, JDBC and an executable WAR. MySQL-specific migrations use `AUTO_INCREMENT` and `MEDIUMBLOB`; Render Postgres is not a drop-in replacement. Uploaded photographs are stored in MySQL's `attachments.image_data`, so the web service does not need a persistent disk. Flyway applies the nine migrations (V001 through V800) at startup.

## 1. Create the fresh database

1. Sign in at [Aiven Console](https://console.aiven.io/).
2. Create **Aiven for MySQL**, selecting the actual **Free** plan. A paid trial is a different plan. Use MySQL 8.x if offered and the closest available region to Render Singapore.
3. Wait for the service to run. Open its connection information and record **host**, **port**, **username**, **password**, and **database**. Use the actual assigned port, which may not be `3306`.
4. For a new service, `defaultdb` is a suitable empty database. Alternatively create an empty `civicpulse` database in Aiven's Databases view and use that name. Flyway creates the application's tables; do not import SQL files manually.
5. Download the project's CA certificate/bundle (`ca.pem`) from the service overview. If Aiven asks for allowed client IP ranges, use the Render service's outbound IP ranges from its dashboard.

The initial database user must be able to create and alter tables/indexes, insert seed records, and read/write the application tables. Aiven's provided administrator user can bootstrap a small project. The application administrator created below is separate from this MySQL user.

See [Aiven free MySQL details](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier) and [Aiven's certificate requirements](https://aiven.io/docs/platform/concepts/tls-ssl-certificates).

## 2. Prepare the deployment values

Construct this JDBC URL using your connection information (no username or password inside the URL):

```text
jdbc:mysql://YOUR_AIVEN_HOST:YOUR_AIVEN_PORT/defaultdb?sslMode=VERIFY_IDENTITY&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&connectTimeout=10000&socketTimeout=60000
```

Replace `defaultdb` if you created a different database. Use the host's DNS name so TLS hostname verification succeeds. Do not paste Aiven's `mysql://username:password@...` service URI into `CP_DB_URL`.

In PowerShell, encode the **entire downloaded PEM file** as one Base64 value and copy it to the clipboard. Adjust the path to the actual downloaded certificate:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\Downloads\ca.pem")) | Set-Clipboard
```

Paste that value into `CP_DB_CA_CERT_BASE64` when Render prompts. This certificate is public trust material. The container imports it into a MySQL-only Java truststore automatically, including all certificates in a CA rotation bundle. Keep `sslMode=VERIFY_IDENTITY` in the JDBC URL; it checks both the certificate and hostname. Other HTTPS services retain the JVM's standard trusted roots.

Choose an application administrator email and a password of at least **10 characters**, with no more than **72 UTF-8 bytes**. On the first startup the app creates that administrator; it does not create demo citizen/officer accounts. Citizens register normally, and the administrator creates officers through the admin interface.

## 3. Publish the deployment files to your GitHub repository

The configured Git remote is [ItzHimanshu007/java_pbl](https://github.com/ItzHimanshu007/java_pbl). Render must be able to read the branch containing these files:

- `Dockerfile` and `.dockerignore`
- `render.yaml`
- `backend/scripts/render-entrypoint.sh`
- `backend/module 3/src/main/resources/application-render.properties`

Review and commit the deployment changes, then push that branch through your normal Git workflow. No credentials, certificates or database records belong in the commit. The Docker build runs `mvnw verify` with the disposable H2 test profile and packages `backend/target/civicpulse.war`.

## 4. Create the Render Blueprint

1. Sign in at [Render Dashboard](https://dashboard.render.com/), then select **New → Blueprint**.
2. Connect `ItzHimanshu007/java_pbl` and choose the branch containing the deployment files. Render reads `render.yaml` from the repository root.
3. Fill in the prompted values:

| Environment variable | Value |
| --- | --- |
| `CP_DB_URL` | The JDBC URL from step 2 |
| `CP_DB_USERNAME` | The Aiven MySQL username, usually `avnadmin` |
| `CP_DB_PASSWORD` | The Aiven MySQL password |
| `CP_DB_CA_CERT_BASE64` | The Base64 value of the downloaded PEM file |
| `CP_ADMIN_EMAIL` | Your chosen app administrator email |
| `CP_ADMIN_PASSWORD` | Your chosen app administrator password, 10+ characters and at most 72 UTF-8 bytes |

4. Confirm that the only Render resource is the **Free** `civicpulse` web service, then deploy. No Render database or paid private service is included.
5. Render generates persistent Base64-encoded 256-bit values for `CP_JWT_SECRET` and `CP_ANONYMITY_SECRET`. Save these securely along with your database connection values. Keep the keys unchanged across deploys; recreating the service may generate new keys. The anonymity key preserves ownership of existing anonymous reports.

The Blueprint selects `SPRING_PROFILES_ACTIVE=render`, HTTPS cookies, Singapore, Docker and `/health`. Render supplies `PORT`; the app listens on `0.0.0.0`. Do not add a Docker command override or activate `demo`/`test`. Render automatically deploys new commits on the selected branch.

[Render Docker documentation](https://render.com/docs/docker) and [Blueprint secret generation](https://render.com/docs/blueprint-spec#generating-random-secrets) describe these settings.

### If using New → Web Service instead

Use the same repository and branch, **Docker**, **Free**, **Singapore**, root directory **blank**, Dockerfile `./Dockerfile`, context `.`, health path `/health`, and the six values above. Add `SPRING_PROFILES_ACTIVE=render` and `CP_COOKIE_SECURE=true` explicitly. Generate each of `CP_JWT_SECRET` and `CP_ANONYMITY_SECRET` separately with this PowerShell snippet and save each value in Render:

```powershell
$taskKeyBytes = New-Object byte[] 32
$taskRandom = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $taskRandom.GetBytes($taskKeyBytes) } finally { $taskRandom.Dispose() }
[Convert]::ToBase64String($taskKeyBytes) | Set-Clipboard
```

As an alternative to `CP_DB_CA_CERT_BASE64`, add a Render secret file named `ca.pem` containing the downloaded PEM text, and set `CP_DB_CA_CERT_FILE=/etc/secrets/ca.pem`. Use only one certificate option. Secret files are described in [Render's environment documentation](https://render.com/docs/configure-environment-variables#secret-files).

## 5. Check the live app

1. Inspect the Render logs: certificate imports, database connection, successful Flyway migration to V800, and application startup should complete. An already migrated database is normal on later deploys.
2. Visit `https://YOUR-SERVICE.onrender.com/health`; expect `{"status":"UP","application":"CivicPulse"}`. This is a liveness endpoint; it does not query the database.
3. Open the homepage and sign in with the bootstrap administrator. Confirm the admin dashboard loads.
4. Register a citizen, submit a report with a small photo and map pin, and check its reference number. This exercises the database and photo storage.
5. Redeploy/restart the app and confirm the account, report and photo still exist. The cloud database persists independently of Render's container.
6. After first provisioning, remove **both** `CP_ADMIN_EMAIL` and `CP_ADMIN_PASSWORD` from the web service environment (or set both to empty). Keep your login details saved. Leaving only one set causes startup to fail. The Blueprint's `sync: false` entries do not restore these values during later syncs.

## 6. Add the Jaipur sample reports

`render.yaml` sets `CP_SAMPLE_REPORTS=true`, so on its next start the app adds 50 fictional Jaipur complaints to the database, covering every zone, service and status, with timelines, photos and community support. It happens once: later starts find the sample references (`CP-DEMO-…`) and skip it.

- The reports the demo files as "yours" go to the citizen account in `CP_SAMPLE_CITIZEN_EMAIL`. The officer in `CP_SAMPLE_OFFICER_EMAIL` takes the sample work of their own department. Change both values if you use other accounts; the accounts must already exist.
- Every other sample citizen and officer is a new account at `@sample.civicpulse.invalid` with a random password nobody knows, so they cannot be used to sign in. No administrator is created; assignments are recorded under the first administrator.
- Existing accounts and reports are not changed. The sample photos add about 25 MB to the database.
- If the service was created with **New → Web Service** instead of a Blueprint, add the three variables under **Environment** and save; Render redeploys.
- The Render log shows `Added the Jaipur sample reports to the database.` If they cannot be added, it shows `Jaipur sample reports were not added: …` and the app still starts.

## Limits and troubleshooting

- Render's free web service sleeps after 15 minutes without inbound traffic, and the next visit can take about a minute to wake it. [Render free-tier limits](https://render.com/docs/free).
- Aiven's free MySQL plan currently provides **1 GB disk**, 1 GB RAM and 76 connections with no fixed expiry. It may power down inactive services. Photos consume database storage; use this setup for a modest PBL project and monitor usage. [Aiven free-tier limits](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier).
- `CP_DB_URL must start with jdbc:mysql://`: supply a JDBC URL, not a Postgres URL or Aiven service URI.
- `PKIX path building failed` / certificate errors: re-download the correct Aiven project CA bundle, encode the whole file, and update `CP_DB_CA_CERT_BASE64`. Use the supplied host DNS name. Apply Aiven's CA rotation notifications by updating the bundle and redeploying.
- Connection timeout / communications link failure: check that Aiven is running, the host/port are correct, and its IP restrictions allow Render's outbound addresses.
- Access denied: verify the MySQL username/password and that the selected database exists.
- Signing-key errors: each key must be valid Base64 decoding to at least 32 bytes. The Blueprint generates compatible values automatically.
- Bootstrap error: provide both administrator values on the first deploy, use a valid email and a password with at least 10 characters. The bootstrap does not promote an existing citizen account to admin.
- Out-of-memory restart: the image limits Java's heap to 55% of container RAM and uses headless image processing. Large simultaneous uploads still require additional memory or a paid service.
- Tests use disposable H2 data. Do not run the integration tests with production database overrides.

## Local verification on 12 October 2026

All **80 automated tests passed** against disposable H2. An isolated build successfully packaged the executable WAR after the existing local WAR hit a file lock. The packaged app started with the `render` profile and fresh H2 migrations; `/health`, the homepage, login JSP, CSS and translations worked, including secure CSRF cookies. The Blueprint parsed successfully, and POSIX shell syntax plus a local two-certificate truststore import/rejection harness passed.

No Docker engine is installed in this environment, so the complete Docker image was not built locally. Local verification does not establish that Render or Aiven resources are deployed. The real MySQL TLS connection and live persistence check require your provisioned Aiven service and Render deployment.
