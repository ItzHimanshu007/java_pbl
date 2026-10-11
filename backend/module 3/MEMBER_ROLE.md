# Module 3: Core backend and community developer

Member 4 · Actual name: [fill] · Repository path: `backend/module 3/`

Spring Boot application, JWT/security, accounts, common hooks, reporting/JDBC/image validation, community support, nearby matching and statistics/CSV.

The owner supplies a runnable foundation and your drop-in payload. Review and integrate your assigned code under your own account.

Development ownership and application login roles are different: the app has Citizen, Officer and Administrator logins. Work with the other members on shared contracts. The foundation contains prepared core security/shared-UI files inside modules 3/1, so teammates do not replace the owner’s baseline on import. Module 3 maintains core backend code after integration; module 1 maintains shared frontend code after integration.

1. **Review core authentication, accounts and shared integration hooks** — `src/main/java/com/civicpulse/{account,auth,config,shared}; CivicPulseApplication.java (baseline already in foundation)`.
2. **Reporting model, parameterized JDBC, access and image storage** — `src/main/java/com/civicpulse/complaints; src/test/java/com/civicpulse/{FeatureTestSupport,ReportingIntegrationTest}.java`.
3. **Community matching, unique support, statistics and CSV routes** — `src/main/java/com/civicpulse/insights`.
4. **Backend tests, demo fixtures and module handover** — `src/test/java/com/civicpulse/InsightsIntegrationTest.java; module documentation and actual reviewed fixes`.

Understand and demonstrate your module. Reports describe actual review, integration, fixes, tests, learning and commits, including coding-agent assistance. Future activity is not prefilled.

Release 1.1 responsibilities, contribution groups and weekly planning: see `RELEASE_1.1_CONTEXT.md`. For the implemented features, `backend/docs/RELEASE_1.1.md` is authoritative.
