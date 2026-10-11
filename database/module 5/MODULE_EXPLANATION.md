# Module 5 explanation

## Purpose and implementation

Owner of the base repository: build/setup/integration infrastructure and the complete versioned relational schema, constraints, indexes and seed categories/departments.

The foundation is the main repository, prepared by the owner. It contains the runnable account/security baseline, shared UI/build hooks and database module 5. Flyway loads SQL from the database folder because root Maven resources copy it to classpath:db/migration. Existing SQL bytes and version numbers are unchanged by restructuring, so a previously migrated database validates normally. Foreign keys connect users, departments, categories, reports, events, images and unique support. MySQL is the persistent normal-profile database; H2 MySQL mode is a local demonstration/test convenience. Java in backend modules owns parameterized queries and transactions; this module owns the SQL schema. Test data cleanup must only run on a disposable test database.

## Concrete ownership and commit groups

1. **Foundation build, security baseline, shared UI and integration hooks** — `Root pom.xml, Maven wrapper; backend/scripts; baseline core backend and shared frontend`.
2. **Versioned schema, constraints, indexes and reference data** — `database/module 5/src/main/resources/db/migration`.
3. **Setup, delivery/integration guides and all member report context** — `README.md, SETUP_FOR_AGENT.md, backend/docs; documentation/report templates across all five modules`.

## Explanation practice

- How does Flyway track already-applied SQL migrations?
- Which foreign keys protect the complaint lifecycle?
- Why is complaint/citizen support a composite primary key?
- Which indexes support queues and nearby lookup?
- How do the five folders build into one runnable app?

## Context for reports

The twelve-week plan supplies objectives only. Fill daily entries from actual work and summarize their evidence in weekly forms. Include design decisions, source reviews, fixes, checks and explanations you can demonstrate. No commit date, author or past student activity is invented.

Release 1.1 responsibilities, contribution groups and weekly planning: see `RELEASE_1.1_CONTEXT.md`. For the implemented features, `backend/docs/RELEASE_1.1.md` is authoritative.
