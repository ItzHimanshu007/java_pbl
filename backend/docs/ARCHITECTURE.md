# Architecture and source layout

CivicPulse is a single Spring Boot MVC application packaged as an executable WAR so embedded Tomcat can render JSP/JSTL/EL. Java package names, routes, SQL bytes and user-facing functions survive the folder reorganization unchanged.

```text
civicpulse/
  frontend/
    module 1/   public/auth/citizen/community JSP and shared assets
    module 2/   admin/officer/insights JSP and repair-slider JavaScript
  backend/
    module 3/   Boot, auth/account/security/shared, reporting and insights Java
    module 4/   administration and resolution Java
    docs/       architecture, operation, integration and test evidence
    scripts/    local launcher and checked module importer
    target/     generated executable WAR/classes/test reports (ignored)
  database/
    module 5/   Flyway SQL and schema/ownership context
  pom.xml       one application build
  mvnw.cmd, mvnw, .mvn/   pinned Maven wrapper
```

Both frontend modules have `src/main/webapp/WEB-INF/views` and/or `src/main/resources/static`; both backend modules have Java source/test roots. Database migrations live at `database/module 5/src/main/resources/db/migration`. Root Maven adds backend 4's Java/test roots with Build Helper, copies resources from frontend 1/2, backend 3 and database 5, and combines both JSP roots with the WAR plugin. JSPs stay under WEB-INF; public requests cannot access them directly. There are no five separate deployments and no frontend Node build.

Authentication uses one login page, BCrypt hashes, a signed JWT in an HttpOnly SameSite cookie, and a JDBC login-session record for revocation. Requests also check current user enabled status and role. Citizen registration never accepts a role. Administrator bootstrap is configured externally; officer provisioning is admin-only. CSRF is required because the browser authenticates through a cookie. Authentication-related CSRF rotation occurs at login/logout; asset requests preserve existing form tokens.

HTTP controllers validate request data and use CurrentUser. Services enforce object permissions/state and transactions. JDBC DAOs bind parameters and paginate queues. Business commands lock report rows before acting. Canonical PNG photos live in database BLOBs, and private image reads use the same report-access policy as details. No path supplied by the user is used to store an upload.

`WorkspaceFeature`, `NavigationContributor` and reporting `DetailContributor` allow optional view/model/navigation contributions. Base dashboards fall back to account views when the relevant backend feature is absent. Reporting owns the common private detail page/model; staff actions are supplied by backend 4 hooks. The before/after script comes from frontend 2. Cross-folder JSP fragment paths refer to the assembled WAR, where the source trees merge.

Legal lifecycle: REPORTED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED. Administrators may reject REPORTED reports with a reason, and reassign ASSIGNED reports before work starts. The assigned officer submits progress and repair evidence. The owning citizen closes or reopens RESOLVED → IN_PROGRESS. Every action records actor/note/time; reopening retains past evidence. Closed reports preserve both repair and closure timestamps.

Nearby matching considers active same-category reports within 250 metres, returning the closest five possible matches after bounding-box and Haversine filtering. Community DTOs expose report summary/location/status/support but no account identity, private description or photos. A complaint/citizen primary key makes support unique. Community reads up to 1,000 recent visible reports and shows up to 200 grouped issues, with active issues first and priority based on distinct linked reporters. Deleted reports are excluded. Statistics are admin-all, officer-assigned, citizen-own; average hours measure submission to citizen-confirmed closure. CSV has the same scope, a 1,000-row limit, quoted cells and spreadsheet-formula neutralization.

The normal profile runs on MySQL. The demo/test profiles use H2 in MySQL mode; demonstration examples are clearly illustrative and reset on restart. Versioned SQL is owned entirely by database module 5; migration order is V001, V100, V200, V300, V400, V500, V600.

## Release 1.1

See `RELEASE_1.1.md` for current profile/theme/Hindi, immutable report deletion, camera/location, nearby Feed and priority-map behaviour. The release-specific verification is in `testing/TEST_RESULTS_1.1.md`; earlier testing records describe the prior release.
