# Release 1.1 verification

- Complete H2 clean schema: **65 tests passed**, zero failures/errors/skips; seven migrations applied.
- Complete MySQL 8.0.46: **65 tests passed**, zero failures/errors/skips. The existing V400 schema upgraded with V500/V600; old migrations validated unchanged.
- Imported complete app: **65 tests passed** and executable WAR packaged successfully. Foundation: **26 tests passed**; after core backend module 3: **45 tests passed**. Both frontend modules packaged successfully before feature backend imports.
- Final complete executable WAR: **69 real HTTP/JSP workflow checks passed**. **32 further checks passed** for all roles' Hindi/English pages, dark/light preferences, signed-out cookies, real Photon address results and invalid-location handling.
- Independent owner foundation WAR: **25 HTTP checks passed**, including all roles' expanded Account, Hindi/dark persistence, local shared assets and location validation.
- Importer rejected missing prerequisites, changed foundation, tampered payload and conflicting local work before copying; valid sequential/repeated imports succeeded.
- Browser review confirmed real address suggestions and a real JPEG preview. At 390 × 844, Feed/Account/report form had no horizontal overflow; Hindi navigation/controls and map filtering worked. Screenshots and detailed records are included.

Tests cover profile ownership/validation/CSRF, address-service parsing/cache/failures, immutable reports, owner delete/restore and hidden evidence, exact-issue distinct citizen thresholds, nearby scopes and public progress privacy, plus authentication, officer administration, assignment, repairs, confirmation, reopening and CSV access.

Physical device camera/GPS permission and capture require a real-device check; these were not exercised. Automated tests use fictional coordinates and test images. Browser checks simulate a phone viewport. External address/map providers require internet. Normal MySQL migrations/JDBC were tested; the normal-profile WAR was not deployed or browser-tested. No public deployment, production load benchmark or non-Windows host test was performed. Prior result files describe the historical 1.0 release.
