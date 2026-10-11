# Module 4 explanation

## Purpose and implementation

Officer provisioning and access, department assignment/rejection, transactional repair progress, evidence submission and citizen verification/reopening.

AdministrationService requires an administrator and hashes new officer passwords with BCrypt. Assignment locks the report and officer, checks enabled access and department agreement, then writes status and timeline in one transaction. A REPORTED report may be rejected with a reason; an ASSIGNED report may be reassigned before work starts. Officers cannot be disabled while active or unverified reports remain assigned. ResolutionService allows only the assigned officer to start work, post updates or resolve with a valid photo. Only the original reporter can confirm closure or reopen a RESOLVED report with a reason. Reopening preserves past photos/events; a later repair becomes the comparison image. CLOSED preserves both resolved_at and closed_at. Workspace/detail hooks add role-specific view models to the core backend.

## Concrete ownership and commit groups

1. **Administrator DTOs, validation and officer provisioning** — `src/main/java/com/civicpulse/administration/{Department,Officer,OfficerForm,AdministrationService}.java`.
2. **Department routing, review and assignment transactions** — `AdministrationController.java, AdministrationWorkspace.java; remaining AdministrationService review commands`.
3. **Repair progress, required evidence and citizen verification routes** — `src/main/java/com/civicpulse/resolution`.
4. **Workflow tests, demo department binding and module handover** — `src/test/java/com/civicpulse/{AdministrationIntegrationTest,ResolutionIntegrationTest}.java; DemoOfficerDepartment.java; module documentation`.

## Explanation practice

- Which exact state transitions are allowed?
- Why cannot an officer close their own repair?
- What happens to evidence when a citizen reopens?
- Why must an assigned officer belong to the chosen department?
- How do authorization, row locks and transactions work together?

## Context for reports

The twelve-week plan supplies objectives only. Fill daily entries from actual work and summarize their evidence in weekly forms. Include design decisions, source reviews, fixes, checks and explanations you can demonstrate. No commit date, author or past student activity is invented.

Release 1.1 responsibilities, contribution groups and weekly planning: see `RELEASE_1.1_CONTEXT.md`. For the implemented features, `backend/docs/RELEASE_1.1.md` is authoritative.
