# Historical 1.0 release record

For current release verification, read `TEST_RESULTS_1.1.md`. The following records the earlier release.

# Actual verification — CivicPulse 1.1.0, three-folder layout

Date: 2026-10-10. Windows, Java 17.0.20.1, Maven 3.9.11, Spring Boot 3.5.16, embedded Tomcat 10.1.55. Test profiles used disposable data only.

## Automated application tests

The complete app passed **52 tests on H2** and **52 tests on isolated MySQL 8.0.46**, with zero failures, errors or skips. MySQL validated V001 and applied V100/V200/V300/V400 from the relocated database resource directory. Final code assembled from the delivered base and four payloads also passed all 52 tests and packaged successfully.

| Suite | Count | Checks |
|---|---|---|
| Foundation | 19 | JWT/cookie login, BCrypt/registration, role boundaries, CSRF, token tampering/expiry, disabled access, logout revocation, account edits |
| Reporting | 11 | Field validation, private access, canonical image uploads/limits, status/edit constraints, pagination and filters |
| Administration | 7 | Officer provisioning, department/enabled rules, assignment/rejection and access management |
| Resolution | 7 | Assigned-officer actions, mandatory evidence, reopen/confirm, preserved timestamps and stale actions |
| Insights | 8 | Category/distance/status matching including dateline, invalid coordinates, unique support, safe DTOs, scoped CSV/counts, closure averages and export escaping |

The separate foundation passed 19 tests, packaged and ran independently. After frontend modules 1/2, packaging passed; after core backend module 3, 38 tests and packaging passed; after backend module 4, all 52 passed. No new backend behavior was added by the reorganization.

## Running executable WAR

**64 checks passed** on the final complete demo WAR using real response cookies and redirects. They compiled all feature JSPs through embedded Tomcat, loaded assets from both frontend roots, created/edit a photo report, provisioned/assigned an officer, posted progress, submitted repair evidence, reopened and repaired again, confirmed closure, rejected a second report with a reason, checked private detail/image denial, nearby safe summaries, idempotent support, role-scoped CSV and logout. Explicit report-fragment UTF-8 checks passed. The separate foundation WAR passed 20 baseline HTTP checks on port 8082.

Detailed names/results: `WAR_SMOKE_RESULTS.json`, `BASE_WAR_SMOKE_RESULTS.json`. `AUTOMATED_RESULTS.json` records suite totals.

## Module import and source preservation

The actual importer was tested against supplied bundles in disposable copies. It rejected missing prerequisites, changed baseline configuration, changed payload bytes and conflicting local files before copying; repository snapshots stayed unchanged for each rejection. All four modules imported successfully in order; repeating an identical import succeeded. Staged builds passed. See `MODULE_IMPORT_RESULTS.json`.

All **109 original source/resource files** were checked against the pre-move snapshot. Java, configuration, migrations, assets and JSP pages retained their bytes. Four reporting JSP fragments gained explicit UTF-8 directives after the browser exposed garbled punctuation; no feature/state/route changed. `RESTRUCTURE_FILE_MAP.json` records original and final hashes. A private placeholder keeps frontend 2's WAR source directory present in the runnable base.

Git content filters were checked on 258 snapshot files with `core.autocrlf=true`; filtered and raw Git hashes matched. `.gitattributes` preserves delivered bytes so clone/checkout line-ending changes do not break module manifest validation. No staging, commits or Git object writes were used. See `GIT_BYTE_RESULTS.json`.

## Browser and responsive checks

All three role logins and pages were inspected in the browser. At 360 × 800, report intake, citizen repair details, administrator dashboard/officer management and officer dashboard/assigned detail had document width equal to scroll width (345 pixels with the scrollbar), with no horizontal overflow. The phone navigation menu opened/closed. Nearby suggestions showed two example reports at 0 and 175 metres. Keyboard End changed the repair slider from 50 to 100 and updated its image clip.

At 1366 × 900 the community map had 18 loaded tiles and five report markers, no captured JavaScript errors, and document/scroll widths both 1351. Desktop officer detail was also rendered. Current screenshots are under `../screenshots/`; detailed observations are in `BROWSER_RESULTS.json`.

## Limits

Phone checks used desktop-browser viewport simulation, not physical phones. The device-location permission prompt, public hosting, load benchmarks, macOS/Linux and external Tomcat deployment were not tested. Normal MySQL migrations/JDBC were verified by integration tests; the normal-profile packaged WAR was not browser-tested. Maps depend on internet tiles, with list/coordinate fallback. Every test reset only its explicitly isolated demonstration/test database.
