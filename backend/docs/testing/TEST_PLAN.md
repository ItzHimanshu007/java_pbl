# Historical 1.0 release record

For current release verification, read `TEST_RESULTS_1.1.md`. The following records the earlier release.

# Verification plan

Run the root Maven verification against disposable H2 data and a separately configured disposable MySQL database. Core integration tests are in backend module 3 (foundation/reporting/insights); staff workflow tests are in backend module 4. Tests reset records, so never point them at real user data.

Check the running executable WAR for actual JSP compilation, authentication cookies/CSRF, report create/edit/private image, all three dashboards and queues, admin officer creation/assignment/rejection, officer start/progress/repair, citizen reopen/confirm, map suggestions/support, scope statistics and CSV. MockMvc alone does not compile the JSPs.

Inspect phone-width and desktop pages in a browser, check horizontal overflow/navigation/form usability, map initialization and the comparison slider. A width simulation is recorded as such, not a physical-device or load benchmark.

Extract the base and import all four shipped payloads in order. Build the base and complete imported app. Check that missing prerequisites, changed payload bytes and existing-file conflicts stop before copying. Compare assembled source hashes to the complete repository.

Record checks actually executed, environment/results and remaining limitations in TEST_RESULTS.md and supporting JSON evidence.
