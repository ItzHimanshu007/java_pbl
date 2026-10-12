# CivicPulse Jaipur

**Live demo:** https://civicpulse-53ga.onrender.com/login

| Role | Email | Password |
|---|---|---|
| Citizen | citizen@civicpulse.test | `citizen@1234` |
| Officer | officer@civicpulse.test | `officer@1234` |
| Admin | admin@civicpulse.test | `admin@1234` |

The site runs on Render's free plan and sleeps after 15 minutes without visitors, so the first visit can take a minute or two to load.

A responsive Java web app, inspired by city 311 services, for reporting civic problems in Jaipur. Citizens report a problem with a photo and a map pin. Administrators route it to the responsible department, officers repair it, and the citizen verifies the fix using the actual before and after photographs. Every step is tracked publicly by reference number.

Java 17, Spring Boot, Spring Security, JWT cookies, JSP/JSTL, JDBC, MySQL, Flyway, Leaflet and OpenStreetMap power one executable WAR. The optional local demo uses an in-memory H2 database; normal operation uses persistent MySQL.

## Run locally

```sh
./mvnw verify                      # build and run all tests (Windows: .\mvnw.cmd verify)
./backend/scripts/run-demo.sh      # build and start the demo (Windows, after verify: .\backend\scripts\run-demo.ps1)
```

Open http://localhost:8080. Demo accounts: `citizen@civicpulse.demo`, `officer@civicpulse.demo` (Roads department), `admin@civicpulse.demo`; password for all three: `CivicDemo!2026`. The sign-in page has one-click buttons that fill them in. The demo also creates an officer for every department (`lighting.officer@`, `sanitation.officer@`, `water.officer@`, `drainage.officer@`, `animal.officer@`, `enforcement.officer@`, `parks.officer@`, `health.officer@civicpulse.demo`) and resets on restart. Try public tracking with reference `CP-DEMO-1001`.

Use the normal MySQL profile for persistent data and deployment (`CP_DB_URL`, `CP_DB_USERNAME`, `CP_DB_PASSWORD`, `CP_JWT_SECRET`); follow `backend/docs/SETUP.md`. Project documentation: `backend/docs/SRS_DOCUMENT.md`, `backend/docs/PROJECT_REPORT.md`, `backend/docs/ARCHITECTURE.md` and `database/module 5/SCHEMA.md`.

## Deploy on Render

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/ItzHimanshu007/java_pbl)

`render.yaml` configures a free Render Docker web service with a persistent external MySQL database. Follow [Render + Aiven MySQL deployment](backend/docs/RENDER_DEPLOYMENT.md) to create the Aiven database, prepare its CA certificate and deploy using **New → Blueprint** in Render.

- The `render` profile uses your external database; Flyway creates the tables automatically. Accounts, reports and photos remain in MySQL across application restarts.
- Supply `CP_DB_URL`, `CP_DB_USERNAME`, `CP_DB_PASSWORD`, `CP_DB_CA_CERT_BASE64`, `CP_ADMIN_EMAIL` and `CP_ADMIN_PASSWORD` when the Blueprint prompts. Render generates stable `CP_JWT_SECRET` and `CP_ANONYMITY_SECRET` values. Save these keys securely.
- After deployment becomes live, open the URL shown in your Render service dashboard and sign in with your chosen administrator credentials. Demo accounts are only available in the local demo profile.
- A free Render service sleeps after 15 minutes without visitors and wakes on the next visit. See [Render's free service limits](https://render.com/docs/free).

The image builds with `docker build -t civicpulse .`. Its entrypoint configures the external MySQL certificate and starts the executable WAR; the deployment guide lists the required environment variables.

## Features

- **Government-portal interface in English and हिन्दी.** Every page has a header language switch, a light/dark theme toggle and A-/A/A+ text sizes, for signed-in users and visitors alike. All interface text, including browser messages, comes from one pair of dictionaries (`i18n/en.json`, `i18n/hi.json`).
- **Jaipur 311 service catalogue.** 16 services (roads, streetlights, garbage, water, drainage, sewer overflow, waterlogging, stray animals, dead animals, encroachment, illegal hoardings, parks, fallen trees, public toilets, mosquito control, footpaths) are routed to 9 departments. Each service has a target resolution time. Reports past their target are flagged as overdue for citizens, officers and administrators.
- **All of Jaipur.** 116 localities in 12 zones, searchable in English or Hindi, work even when the online geocoder is unavailable. Reports and statistics are grouped by zone.
- **Guided reporting.** Pick a service from icon tiles, use quick-title suggestions, add or take a photo, and choose a locality, address, GPS or map pin. The form suggests nearby duplicates and keeps a live review checklist. After submission you get an acknowledgement with the reference number, the target date and a printable slip.
- **Anonymous reporting.** See below.
- **Public tracking.** `/track?ref=…` shows status, department, zone, dates and the public timeline without sign-in. Names, contact details, notes and photos are never shown.
- **Better maps.** OpenStreetMap opens by default on every map, with Satellite available in the layer switcher. Category icon pins are coloured by priority, with clustering, full-screen mode, two-way sync between issue cards and pins, and a location mini-map on every report.
- **Dashboards.** The administrator dashboard shows the review queue, overdue work, department workload, complaints by zone and top services. Officers get their assignments and overdue work. Citizens get a nearby feed and My reports with a progress stepper. CSV export includes zone, due date, overdue and anonymity columns.
- Linked reports of the same issue count distinct citizens: five or more = high, two to four = medium, one = low priority. Submitted reports are immutable; owners can delete and restore them. Citizens confirm or reopen repairs, keeping the full history.

## Anonymous reporting

A citizen can tick **Report anonymously** when submitting. The report, its photo and every citizen action on its timeline are then filed under a disabled system account, “Anonymous citizen”. The real account is stored only as `reporter_hash = HMAC-SHA256(server key, account id)`.

- Officers, administrators and anyone reading the database see no name, email or account id, and the hash cannot be reversed or recomputed without the server key.
- The reporter's own account recomputes the same hash, so they can still find the report in My reports, verify or reopen the repair, and delete or restore it.
- Every report stores the hash, so linked-report priority still counts each citizen once, whether named or anonymous.
- The key comes from `CP_ANONYMITY_SECRET` (Base64, at least 32 random bytes). If it is not set, a separate key is derived from `CP_JWT_SECRET`. Keep the key stable: changing it unlinks existing anonymous reports from their reporters.

## Fixes in this version

- Two new migrations build on the team's V001–V600 schema: V700 (service catalogue, target times, four new departments; the Drainage department becomes Sewerage and Drainage) and V800 (anonymous reports).
- `mvnw` is executable again, so `./mvnw` works on Linux and macOS.
- Language could only be changed from the Account page after signing in; visitors had no way to switch to Hindi.
- Timeline action names (for example “Work started”) were never translated because the lookup key had spaces instead of underscores.
- The 43 KB browser dictionary was a hand-copied duplicate of `hi.json`; it is now served from the same files at `/i18n/messages.js`.
- Address search failed completely when the Photon geocoder was unreachable or rate-limited; Jaipur localities are now matched offline first.

## Five members, three source folders

| Folder | Developed by | Responsibility |
| --- | --- | --- |
| `frontend/module 1` | Dhruv Saini | Shared account/auth UI, citizen reporting, feed, map and preferences |
| `frontend/module 2` | Ayush Sharma | Admin/officer dashboards, assignment and repair/verification UI |
| `backend/module 3` | Himanshu Jasoriya | Security/accounts, locations, reporting and community APIs |
| `backend/module 4` | Himanshu Jasoriya | Administration, resolution and end-to-end workflow tests |
| `database/module 5` | Hemant Jangra | Foundation repository, schema, migrations and integration |
| Everything else | Hitesh Kumar | Root build, scripts, deployment and documentation |

These are ownership modules in one app, not independent services. The root `pom.xml` compiles both backend folders, copies both frontend asset roots and the database migrations, and builds one executable WAR at `backend/target/civicpulse.war`.

## Location, maps and deployment

GPS and camera require browser permission and HTTPS (localhost is supported). Address suggestions combine the offline Jaipur locality list with a Photon-compatible geocoder (`CP_GEOCODER_URL`, default: the public Photon endpoint), which is cached and throttled. Every map starts with OpenStreetMap tiles, with Esri World Imagery available as an optional Satellite layer. Both providers are allowed by the Content-Security-Policy. If Satellite is unreachable the map falls back to OpenStreetMap and the report list keeps working. Map attribution stays visible.

For persistent production operation, use MySQL, stable `CP_JWT_SECRET` and `CP_ANONYMITY_SECRET` values, HTTPS and secure cookies. Do not deploy the demo profile.

For free hosting with a fresh cloud database, follow [Render + Aiven MySQL deployment](backend/docs/RENDER_DEPLOYMENT.md). The root `Dockerfile` builds the complete app, and `render.yaml` configures a free Render web service using external MySQL.

## Credits

The demo contains fictional reports. Sample photographs: [Marked Pothole](https://commons.wikimedia.org/wiki/File:Marked_Pothole.jpg) (Prosthetic Head) and [Recently patched potholes](https://commons.wikimedia.org/wiki/File:2018-09-03_15_15_33_Recently_patched_potholes_along_Franklin_Farm_Road_in_the_Franklin_Farm_section_of_Oak_Hill,_Fairfax_County,_Virginia.jpg) (Famartin), CC BY-SA 4.0. Icons: [Lucide](https://lucide.dev) (ISC). Fonts: Inter, Noto Sans Devanagari and Poppins (SIL OFL 1.1). Map clustering: Leaflet.markercluster (MIT).

This is a Java project-based-learning (PBL) project, not an official Government website.
