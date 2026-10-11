# Module 4: Administration and resolution backend developer

Member 5 · Actual name: [fill] · Repository path: `backend/module 4/`

Officer provisioning and access, department assignment/rejection, transactional repair progress, evidence submission and citizen verification/reopening.

The owner supplies a runnable foundation and your drop-in payload. Review and integrate your assigned code under your own account.

Development ownership and application login roles are different: the app has Citizen, Officer and Administrator logins. Work with the other members on shared contracts. The foundation contains prepared core security/shared-UI files inside modules 3/1, so teammates do not replace the owner’s baseline on import. Module 3 maintains core backend code after integration; module 1 maintains shared frontend code after integration.

1. **Administrator DTOs, validation and officer provisioning** — `src/main/java/com/civicpulse/administration/{Department,Officer,OfficerForm,AdministrationService}.java`.
2. **Department routing, review and assignment transactions** — `AdministrationController.java, AdministrationWorkspace.java; remaining AdministrationService review commands`.
3. **Repair progress, required evidence and citizen verification routes** — `src/main/java/com/civicpulse/resolution`.
4. **Workflow tests, demo department binding and module handover** — `src/test/java/com/civicpulse/{AdministrationIntegrationTest,ResolutionIntegrationTest}.java; DemoOfficerDepartment.java; module documentation`.

Understand and demonstrate your module. Reports describe actual review, integration, fixes, tests, learning and commits, including coding-agent assistance. Future activity is not prefilled.

Release 1.1 responsibilities, contribution groups and weekly planning: see `RELEASE_1.1_CONTEXT.md`. For the implemented features, `backend/docs/RELEASE_1.1.md` is authoritative.
