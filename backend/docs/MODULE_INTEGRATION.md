# Five-module team integration

| Module | Repository path | Member | Source ownership |
|---|---|---|---|
| 1 | `frontend/module 1/` | 2 | Citizen/community and shared frontend |
| 2 | `frontend/module 2/` | 3 | Staff/verification/insights frontend |
| 3 | `backend/module 3/` | 4 | Core/JWT/reporting/community/insights backend |
| 4 | `backend/module 4/` | 5 | Administration/resolution backend |
| 5 | `database/module 5/` | 1 — owner | Database schema and foundation integration |

The owner's foundation **is the base/main repository**. It includes all versioned SQL in module 5, the prepared security/account backend in module 3, shared public/auth/account pages and assets in module 1, root build/wrapper, scripts and all five members' reporting context. It runs on its own with account workspaces. Preparing this baseline does not turn the backend member's already-present files into new contribution diffs.

The four add-on payloads contain the new frontend/backend source owned by their member. Install **module 1 → module 2 → module 3 → module 4** into the same base repository. Frontend 2 reuses fragments from frontend 1; backend 3 enables reporting/community pages; backend 4 enables staff workflow. The complete source ZIP already has all of these files; use the base ZIP for sequential member-import commits.

```powershell
.\backend\scripts\install-module.ps1 -ModuleFolder 'C:\path\to\module 1'
.\mvnw.cmd verify
```

Each bundle contains `payload/`, a hash manifest, setup, role/explanation, commit plan, contribution log and reporting forms. Payload paths already start with `frontend/` or `backend/`; copy them into the repository root, preserving that prefix. The importer checks the base contract, foundation file hashes, prior module markers, payload paths/hashes and all existing-file conflicts before copying. Identical context files are accepted; changed files stop for review. It writes the installation marker inside the corresponding source module folder. No Git commands, commits or pushes are performed.

Root configuration is prepared once. Feature imports never replace another owner's build/security files or database migrations. The manifest carries checksums because the owner has not yet made the first Git commits; no revision hash is invented. All SQL versions and bytes remain identical to those before the reorganization, so Flyway checksums remain compatible. Do not edit an already-applied migration.

The repository's `.gitattributes` preserves delivered file bytes across Git checkouts, so automatic line-ending conversion does not invalidate the module checksums. The shell wrapper retains LF and the Windows wrapper retains CRLF. Regenerate manifests when redistributing actual source edits.

Use the four meaningful commit groups in each add-on's guide and the owner's three foundation groups in `backend/docs/COMMIT_PLAN.md`. Existing baseline files should be reviewed; record real fixes/changes rather than claiming unchanged files as new work. Reports supply planned objectives and blank actual-work fields for twelve weeks, to fill from genuine activity.

## Release 1.1

See `RELEASE_1.1.md` for current profile/theme/Hindi, immutable report deletion, camera/location, nearby Feed and priority-map behaviour. The release-specific verification is in `testing/TEST_RESULTS_1.1.md`; earlier testing records describe the prior release.
