# Agent setup — module 1

You are helping Member 2, the citizen and community frontend developer. Read the user's instructions and root `SETUP_FOR_AGENT.md`, then this folder's role, explanation, commit plan and report context. Preserve the work of other contributors.

1. Locate the **three-folder** 1.1.0 base/current repository. Inspect the real Git status, branch and contributor identity. Old feature-based module bundles do not match this structure.
2. Import bundle `module 1` with `./backend/scripts/install-module.ps1 -ModuleFolder "FULL PATH/module 1"` from the repository root. The payload mirrors `frontend/module 1/`; do not paste payload contents directly at the root. The importer checks base hashes, prerequisites, path limits, checksums and conflicting files before copying.
3. Build at the repository root: `./mvnw.cmd verify` on Windows or `./mvnw verify` elsewhere. Run `./backend/scripts/run-demo.ps1` and inspect the module’s real pages/workflow. Backend tests live in modules 3/4; module-specific frontend behavior also requires a running WAR/browser.
4. Read `backend/docs/MODULE_INTEGRATION.md` for install order **1 → 2 → 3 → 4**, with database module 5 already in the base. Partial installations intentionally expose only the available functionality. The full feature set requires all four add-ons.
5. Review diffs, record actual results and stage the meaningful groups in `COMMIT_PLAN.md`. Use only the contributor’s real identity. Make commits only when authorized by the member; push only when a destination and authorization are supplied. Preserve secrets, Git metadata and applied migrations; no force push or fabricated activity.

Read this release’s `COMMIT_PLAN.md` for the owned payload groups. Shared foundation files are already installed; do not create filler commits for identical files.

Use `reports/DAILY_REPORT_TEMPLATE.md`, `TWELVE_WEEK_PLAN.md` and all twelve `reports/weekly/` forms for real work. Fill the actual name and contribution log. Recompute manifests when redistributing changed source; do not ignore checksum failures. Root `backend/docs/testing/TEST_RESULTS.md` records release checks and their limits.

## Release 1.1 update

Read `RELEASE_1.1_CONTEXT.md` and `backend/docs/RELEASE_1.1.md` before importing or explaining this module. Use only the supplied 1.1 foundation and 1.1 bundles. Build with `verify`; document real results. The payload contains only owned feature files; shared account/location assets and the core are already in the foundation.
