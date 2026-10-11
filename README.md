# CivicPulse Jaipur

A responsive Java web app, inspired by city 311 services, for reporting civic problems in Jaipur. Citizens report a problem with a photo and a map pin. Administrators route it to the responsible department, officers repair it, and the citizen verifies the fix using the actual before and after photographs. Every step is tracked publicly by reference number.

Java 17, Spring Boot, Spring Security, JWT cookies, JSP/JSTL, JDBC, MySQL, Flyway, Leaflet and OpenStreetMap power one executable WAR. The optional local demo uses an in-memory H2 database; normal operation uses persistent MySQL.

## Run locally

```sh
./mvnw verify                      # build and run all tests (Windows: .\mvnw.cmd verify)
./backend/scripts/run-demo.sh      # start the demo (Windows: .\backend\scripts\run-demo.ps1)
```

Open http://localhost:8080. Demo accounts: `citizen@civicpulse.demo`, `officer@civicpulse.demo` (Roads department), `admin@civicpulse.demo`; password for all three: `CivicDemo!2026`. The sign-in page has one-click buttons that fill them in. The demo also creates an officer for every department (`lighting.officer@`, `sanitation.officer@`, `water.officer@`, `drainage.officer@`, `animal.officer@`, `enforcement.officer@`, `parks.officer@`, `health.officer@civicpulse.demo`) and resets on restart. Try public tracking with reference `CP-DEMO-1001`.

Use the normal MySQL profile for persistent data and deployment (`CP_DB_URL`, `CP_DB_USERNAME`, `CP_DB_PASSWORD`, `CP_JWT_SECRET`).

## Features

- **Government-portal interface in English and हिन्दी.** Every page has a header language switch, a light/dark theme toggle and A-/A/A+ text sizes, for signed-in users and visitors alike. All interface text, including browser messages, comes from one pair of dictionaries (`i18n/en.json`, `i18n/hi.json`).
- **Jaipur 311 service catalogue.** 16 services (roads, streetlights, garbage, water, drainage, sewer overflow, waterlogging, stray animals, dead animals, encroachment, illegal hoardings, parks, fallen trees, public toilets, mosquito control, footpaths) are routed to 9 departments. Each service has a target resolution time. Reports past their target are flagged as overdue for citizens, officers and administrators.
- **All of Jaipur.** 116 localities in 12 zones, searchable in English or Hindi, work even when the online geocoder is unavailable. Reports and statistics are grouped by zone.
- **Guided reporting.** Pick a service from icon tiles, use quick-title suggestions, add or take a photo, and choose a locality, address, GPS or map pin. The form suggests nearby duplicates and keeps a live review checklist. After submission you get an acknowledgement with the reference number, the target date and a printable slip.
- **Anonymous reporting.** See below.
- **Public tracking.** `/track?ref=…` shows status, department, zone, dates and the public timeline without sign-in. Names, contact details, notes and photos are never shown.
- **Better maps.** Street, satellite and OpenStreetMap basemaps (dark streets in dark mode), category icon pins coloured by priority, clustering, full-screen mode, two-way sync between issue cards and pins, and a location mini-map on every report.
- **Dashboards.** The administrator dashboard shows the review queue, overdue work, department workload, complaints by zone and top services. Officers get their assignments and overdue work. Citizens get a nearby feed and My reports with a progress stepper. CSV export includes zone, due date, overdue and anonymity columns.
- Linked reports of the same issue count distinct citizens: five or more = high, two to four = medium, one = low priority. Submitted reports are immutable; owners can delete and restore them. Citizens confirm or reopen repairs, keeping the full history.

## Anonymous reporting

A citizen can tick **Report anonymously** when submitting. The report, its photo and every citizen action on its timeline are then filed under a disabled system account, “Anonymous citizen”. The real account is stored only as `reporter_hash = HMAC-SHA256(server key, account id)`.

- Officers, administrators and anyone reading the database see no name, email or account id, and the hash cannot be reversed or recomputed without the server key.
- The reporter's own account recomputes the same hash, so they can still find the report in My reports, verify or reopen the repair, and delete or restore it.
- Every report stores the hash, so linked-report priority still counts each citizen once, whether named or anonymous.
- The key comes from `CP_ANONYMITY_SECRET` (Base64, at least 32 random bytes). If it is not set, a separate key is derived from `CP_JWT_SECRET`. Keep the key stable: changing it unlinks existing anonymous reports from their reporters.

## Fixes in this version

- The `database/module 5` Flyway migrations, the Maven wrapper and the demo run scripts were missing from the repository, so the application could not start. They are restored (V001–V600) with two new migrations: V700 (service catalogue) and V800 (anonymous reports).
- Language could only be changed from the Account page after signing in; visitors had no way to switch to Hindi.
- Timeline action names (for example “Work started”) were never translated because the lookup key had spaces instead of underscores.
- The 43 KB browser dictionary was a hand-copied duplicate of `hi.json`; it is now served from the same files at `/i18n/messages.js`.
- Address search failed completely when the Photon geocoder was unreachable or rate-limited; Jaipur localities are now matched offline first.

## Five members, three source folders

| Folder | Owner responsibility |
| --- | --- |
| `database/module 5` | Member 1 / owner: foundation repository, schema, migrations and integration |
| `frontend/module 1` | Member 2: shared account/auth UI, citizen reporting, feed, map and preferences |
| `frontend/module 2` | Member 3: admin/officer dashboards, assignment and repair/verification UI |
| `backend/module 3` | Member 4: security/accounts, locations, reporting and community APIs |
| `backend/module 4` | Member 5: administration, resolution and end-to-end workflow tests |

These are ownership modules in one app, not independent services. The root `pom.xml` compiles both backend folders, copies both frontend asset roots and the database migrations, and builds one executable WAR at `backend/target/civicpulse.war`.

## Location, maps and deployment

GPS and camera require browser permission and HTTPS (localhost is supported). Address suggestions combine the offline Jaipur locality list with a Photon-compatible geocoder (`CP_GEOCODER_URL`, default: the public Photon endpoint), which is cached and throttled. Map tiles come from CARTO, Esri World Imagery and OpenStreetMap and are allowed by the Content-Security-Policy. If they are unreachable the map falls back to OpenStreetMap and the report list keeps working. Map attribution stays visible.

For persistent production operation, use MySQL, stable `CP_JWT_SECRET` and `CP_ANONYMITY_SECRET` values, HTTPS and secure cookies. Do not deploy the demo profile.

## Credits

The demo contains fictional reports. Sample photographs: [Marked Pothole](https://commons.wikimedia.org/wiki/File:Marked_Pothole.jpg) (Prosthetic Head) and [Recently patched potholes](https://commons.wikimedia.org/wiki/File:2018-09-03_15_15_33_Recently_patched_potholes_along_Franklin_Farm_Road_in_the_Franklin_Farm_section_of_Oak_Hill,_Fairfax_County,_Virginia.jpg) (Famartin), CC BY-SA 4.0. Icons: [Lucide](https://lucide.dev) (ISC). Fonts: Inter, Noto Sans Devanagari and Poppins (SIL OFL 1.1). Map clustering: Leaflet.markercluster (MIT).

This is a Java project-based-learning (PBL) project, not an official Government website.
