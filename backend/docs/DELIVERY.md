# Delivery packages

Use the files labelled **Three-Folder** for this layout. Earlier foundation/feature-split packages are superseded and retained only as historical outputs.

- `CivicPulse-Complete-Three-Folder-1.1.0.zip`: the assembled source, all five module contexts/reports and manifests; build/run immediately.
- `CivicPulse-Team-Kit-Three-Folder-1.1.0.zip`: `foundation/` (owner's runnable base, database module 5 included) and four drop-in folders `module 1`, `module 2`, `module 3`, `module 4`, plus START_HERE.md. Begin here for separate teammate import commits.
- `CivicPulse-Base-Three-Folder-1.1.0.zip`: the same owner base separately.
- `civicpulse-three-folder-1.1.0.war`: prebuilt app; run with Java 17 and the demo profile or configured MySQL settings.

Extract before building. Source ZIPs exclude Git metadata, build output, runtime caches, local data and credentials. They do not include the JDK/MySQL server or fake Git history. The working repository's existing Git state is preserved; no commits or remote changes were made.

Root `SOURCE_MANIFEST.json` records source snapshot hashes. `BASE_CONTRACT.json` records the shared baseline contract. Each drop-in manifest lists source-relative payload paths and hashes and prior module requirements. Module 5 is already installed in the base; the four teammate add-ons import to frontend 1/2 and backend 3/4. All five modules include a daily template and twelve weekly forms with blank actual-work fields.

## Release 1.1

See `RELEASE_1.1.md` for current profile/theme/Hindi, immutable report deletion, camera/location, nearby Feed and priority-map behaviour. The release-specific verification is in `testing/TEST_RESULTS_1.1.md`; earlier testing records describe the prior release.
