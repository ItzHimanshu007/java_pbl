# Implementation and team plan

The small Java web app contains citizen intake/tracking, admin routing, officer repairs, citizen verification, private evidence/history, community support, nearby suggestions, repair comparison and basic scoped statistics/CSV. One login page serves three roles. There are no paid AI services, microservices, chat, complex municipal integrations or separate frontend toolchains.

The final implementation is organized by technical layer: frontend modules 1/2, backend modules 3/4, database module 5. The source ownership table and install order are in `MODULE_INTEGRATION.md`; exact class/view/SQL responsibility and explanation questions are inside each module.

Efficient integration sequence: owner reviews/builds the foundation and database; import citizen/community UI; import staff/verification UI; import reporting/community backend; import staff workflow backend; build and test the assembled WAR. The root build has the needed source/resource mappings before any feature import.

For a genuine twelve-week course timetable, use each module's planned objectives and blank daily/weekly forms: requirements and contracts first, then schema/code review and local setup, validation and permission checks, integration, history/edge cases, maps/privacy, full journey/responsiveness, regression fixes and final explanation. Actual work dates/results/commits are completed by each member as the work occurs.

Completion checks include authentication/cross-role access, report creation/immutability/deletion/upload limits, department assignment, required repair evidence, citizen close/reopen, private images, support uniqueness, matching distances, scoped exports, running-WAR JSP compilation, phone-width layouts and base-plus-payload assembly. See `testing/TEST_RESULTS.md` for actual results; a planned check is not a claim that it passed.

## Release 1.1

See `RELEASE_1.1.md` for current profile/theme/Hindi, immutable report deletion, camera/location, nearby Feed and priority-map behaviour. The release-specific verification is in `testing/TEST_RESULTS_1.1.md`; earlier testing records describe the prior release.
