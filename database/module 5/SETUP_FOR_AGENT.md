# Agent setup — module 5

You are helping Member 1, the foundation owner and database developer. Read the user's instructions and root `SETUP_FOR_AGENT.md`, then this folder's role, explanation, commit plan and report context. Preserve the work of other contributors.

1. Locate the **three-folder** 1.1.0 base/current repository. Inspect the real Git status, branch and contributor identity. Old feature-based module bundles do not match this structure.
2. This module is already part of the owner’s foundation. Review the SQL, root build and common baseline.
3. Build at the repository root: `./mvnw.cmd verify` on Windows or `./mvnw verify` elsewhere. Run `./backend/scripts/run-demo.ps1` and inspect the module’s real pages/workflow. Backend tests live in modules 3/4; module-specific frontend behavior also requires a running WAR/browser.
4. Read `backend/docs/MODULE_INTEGRATION.md` for install order **1 → 2 → 3 → 4**, with database module 5 already in the base. Partial installations intentionally expose only the available functionality. The full feature set requires all four add-ons.
5. Review diffs, record actual results and stage the meaningful groups in `COMMIT_PLAN.md`. Use only the contributor’s real identity. Make commits only when authorized by the member; push only when a destination and authorization are supplied. Preserve secrets, Git metadata and applied migrations; no force push or fabricated activity.

1. **Foundation build, security baseline, shared UI and integration hooks** — `Root pom.xml, Maven wrapper; backend/scripts; baseline core backend and shared frontend`.
2. **Versioned schema, constraints, indexes and reference data** — `database/module 5/src/main/resources/db/migration`.
3. **Setup, delivery/integration guides and all member report context** — `README.md, SETUP_FOR_AGENT.md, backend/docs; documentation/report templates across all five modules`.

Use `reports/DAILY_REPORT_TEMPLATE.md`, `TWELVE_WEEK_PLAN.md` and all twelve `reports/weekly/` forms for real work. Fill the actual name and contribution log. Recompute manifests when redistributing changed source; do not ignore checksum failures. Root `backend/docs/testing/TEST_RESULTS.md` records release checks and their limits.

## Release 1.1 update

Read `RELEASE_1.1_CONTEXT.md` and `backend/docs/RELEASE_1.1.md` before importing or explaining this module. Use only the supplied 1.1 foundation and 1.1 bundles. Build with `verify`; document real results. The payload contains only owned feature files; shared account/location assets and the core are already in the foundation.
