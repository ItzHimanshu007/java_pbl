# CivicPulse repository setup for an agent

Read the user's instructions first. This is one Java 17/Spring Boot/JSP/JDBC/MySQL application with JWT authentication, organized in exactly three visible source folders: `frontend`, `backend`, `database`. Module numbering is now frontend 1/2, backend 3/4, database 5. Do not restore the older feature-based split.

1. Read `README.md`, `backend/docs/BUILD_STATUS.md`, `backend/docs/ARCHITECTURE.md` and `backend/docs/MODULE_INTEGRATION.md`. Inspect actual Git status and preserve existing work. When graph tools are available, follow the user's graph-first discovery instructions and check coverage; otherwise use exact source paths and disclose limitations before exhaustive claims.
2. Root `pom.xml` compiles both backend source/test roots, copies both frontend asset roots and the database migrations, and combines both JSP roots into one executable WAR at `backend/target/civicpulse.war`. Modules are source ownership folders, not separate services.
3. Use Java 17 and the root Maven wrapper. Run `./mvnw.cmd verify` (Windows) and `./backend/scripts/run-demo.ps1`. Tests clean their configured database; only use the test profile with disposable data. See setup for normal MySQL settings.
4. The owner's foundation is the base repository, containing database module 5 and the prepared security/shared-UI/build baseline. Import the four add-ons in order 1 → 2 → 3 → 4 using `backend/scripts/install-module.ps1`. Follow the selected module's role, explanation, setup, commit plan and reporting materials.
5. Preserve endpoint behavior, roles, CSRF checks, SQL migration bytes/versions, private evidence access and lawful state transitions. Do not add unnecessary features, frameworks or duplicate copies of source. The shared core owns optional workspace/navigation/detail hooks.
6. Stage actual changes in meaningful groups under the contributor's actual identity only when authorized. Review each staged diff and push only to a supplied destination when authorized. Never invent student activity, backdate commits, change authors or fill future daily/weekly results.

All five folders include enough implementation context for review and explanation, a daily report template and twelve weekly forms. The owner's three foundation commit groups are in `backend/docs/COMMIT_PLAN.md`.
