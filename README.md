# CivicPulse

A responsive Java web app for citizens to report local civic problems, officers to repair them and administrators to route the work. Citizens track progress and verify the repair using the actual before/after photographs.

Java 17, Spring Boot, Spring Security, JWT cookies, JSP/JSTL, JDBC, MySQL, Flyway, Bootstrap and Leaflet power one executable WAR. The optional local demo uses H2; normal operation uses persistent MySQL.

## Run locally

```powershell
.\mvnw.cmd verify
.\backend\scripts\run-demo.ps1
```

Open http://localhost:8080. Demo accounts: `citizen@civicpulse.demo`, `officer@civicpulse.demo`, `admin@civicpulse.demo`; password for all three: `CivicDemo!2026`. Demo records reset on restart. Use the normal MySQL profile for persistent data and deployment; follow `backend/docs/SETUP.md`.

## Features in 1.1

- English/Hindi interface and light/dark/device theme, saved in Account for every role.
- Optional date of birth, phone, address, city, postal code, neighbourhood and home-area pin. Profile/contact information stays private.
- Real address suggestions, draggable map pin and fresh device location. Coordinates are internal rather than visible form fields.
- Upload preview and camera capture. Capturing a photo requests a fresh device location; permission denial leaves manual location selection available.
- Submitted reports are immutable. Owners can delete to a recoverable Deleted reports view, or restore the original state/history.
- Nearby Feed for other citizens' issues; own status counts and records in My Reports. Public progress excludes private notes and identities.
- Explicitly linked reports of the exact same issue count distinct citizens: five or more = red/high; two to four = yellow/medium; one = green/low. Nearby suggestions alone do not link issues. Closed issues are green and appear after active issues.
- Coloured issue areas, numbered pins, priority filters and progress views across roles. Progress percentages describe workflow stages, not a time estimate.
- Admin assignment/rejection and officer progress/repair evidence; citizen confirmation or reopening retains the history and photos.

The demo contains fictional reports and licensed real sample photographs with attribution. Real reports always use the citizen's upload and the officer's repair upload. No drawn fallback photos or top local-demo banner are used.

## Five members, three source folders

| Folder | Owner responsibility |
| --- | --- |
| `database/module 5` | Member 1 / owner: foundation repository, schema, migrations and integration |
| `frontend/module 1` | Member 2: shared account/auth UI, citizen reporting, feed, map and preferences |
| `frontend/module 2` | Member 3: admin/officer dashboards, assignment and repair/verification UI |
| `backend/module 3` | Member 4: security/accounts, locations, reporting and community APIs |
| `backend/module 4` | Member 5: administration, resolution and end-to-end workflow tests |

These are ownership modules in one app, not independent services. The team kit includes a runnable owner foundation and four add-ons. Import modules 1 → 2 → 3 → 4 into the supplied 1.1 foundation. Do not mix old-version bundles. Every member's folder includes setup instructions, role explanation, meaningful commit groups, a daily template and twelve weekly report forms. Record real work and commits using each contributor's actual identity.

## Location and deployment

GPS/camera require browser permission and HTTPS (localhost is supported). A camera location means the device's location at capture/request time; uploaded gallery images do not establish where they were taken. Address suggestions use a configured Photon-compatible service (`CP_GEOCODER_URL`), defaulting to the public Photon endpoint. The app caches and throttles requests; provider availability and internet access are still required. If lookup fails, choose a pin and enter a landmark. Production deployments should select a suitably hosted service for their load. Map attribution stays visible.

For persistent production operation, use MySQL, a stable secret, HTTPS and secure cookies. Do not deploy the demo profile. See `backend/docs/RELEASE_1.1.md`, setup and test results for the implementation and verification details.
