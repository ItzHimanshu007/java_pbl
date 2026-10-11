# Database schema

Migrations live in `src/main/resources/db/migration` and are copied to `classpath:db/migration` by the root build. The SQL migration filenames, versions and bytes have not changed during the three-folder reconfiguration.

| Migration | Tables/indexes/reference data |
|---|---|
| V001__foundation.sql | departments, users, login_sessions; five enabled departments; roles and foreign keys |
| V100__reporting.sql | categories, complaints, complaint_events, attachments; queue/nearby/history indexes; five categories |
| V200__administration.sql | administration_events for officer provisioning/access audit |
| V300__resolution.sql | closure/resolution index for statistics |
| V400__community_support.sql | complaint_supports with unique complaint/citizen key |
| V500__account_preferences.sql | account_profiles, optional contact/home details, theme and language |
| V600__report_deletion_and_issue_links.sql | recoverable deletion timestamp, self-referencing issue root and indexes |

Users link to an optional department; categories link to a department. Complaints link to reporter/category/department/optional assigned officer. Events and photos link to a complaint and actor/uploader. Photos are canonical PNG MEDIUMBLOBs; purpose is ORIGINAL or REPAIR. Support links to report and citizen. Foreign keys protect references; checks constrain roles, states, coordinates and photo purposes. Actual business authorization/state transitions are enforced by Java services, using parameterized JDBC and transactions/row locks.

Use MySQL 8 for normal operation; H2 MySQL mode supports local demos/tests. Flyway keeps schema history and validates previously applied migration checksums. Change a live schema with a new higher-numbered migration, never by editing an applied file. Back up the dedicated database for persistent data, including private image BLOBs. Test fixtures delete records and must use a disposable test database.

Profiles reference exactly one user. Birth dates/contact/home addresses stay private. Home coordinate pairs are optional. Issue roots group explicitly linked reports; distinct active reporters determine public priority. Deletion hides records but retains evidence/history for owner restoration. V500/V600 extend the existing V400 schema without modifying prior migration bytes.
