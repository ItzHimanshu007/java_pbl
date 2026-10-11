# Software Requirements Specification (SRS)
## For CivicPulse — Municipal Issue Reporting and Resolution Platform
### Version 1.1.0 | Academic & Production Specification

---

## Table of Contents
1. **Introduction**
   - 1.1 Purpose
   - 1.2 Document Conventions
   - 1.3 Intended Audience and Reading Suggestions
   - 1.4 Product Scope
   - 1.5 References
2. **Overall Description**
   - 2.1 Product Perspective
   - 2.2 Product Functions
   - 2.3 User Classes and Characteristics
   - 2.4 Operating Environment
   - 2.5 Design and Implementation Constraints
   - 2.6 User Documentation
   - 2.7 Assumptions and Dependencies
3. **External Interface Requirements**
   - 3.1 User Interfaces
   - 3.2 Hardware Interfaces
   - 3.3 Software Interfaces
   - 3.4 Communications Interfaces
4. **System Features & Functional Requirements**
   - 4.1 Module 1: Authentication & Account Preferences (Citizen, Officer, Admin)
   - 4.2 Module 2: Citizen Issue Reporting, Camera Capture & Location Engine
   - 4.3 Module 3: Community Feed, Proximity Matching & Interactive Map
   - 4.4 Module 4: Administrative Dispatch, Routing & Officer Provisioning
   - 4.5 Module 5: Field Officer Resolution Workflow & Repair Proof
   - 4.6 Module 6: Citizen Verification (Closure vs Reopening) & Audit Trail
   - 4.7 Module 7: Analytics, CSV Export & Data Governance
5. **Non-Functional Requirements (NFRs)**
   - 5.1 Performance Requirements
   - 5.2 Safety and Integrity Requirements
   - 5.3 Security Requirements
   - 5.4 Software Quality Attributes (Reliability, Availability, Maintainability, Usability)
6. **Database & Data Storage Requirements**
   - 6.1 Relational Schema & Integrity Constraints
   - 6.2 Binary Large Object (BLOB) Canonical Image Storage
   - 6.3 Audit Logging & Immutability Rules
7. **Verification & Acceptance Criteria**

---

## 1. Introduction

### 1.1 Purpose
This Software Requirements Specification (SRS) defines the complete functional and non-functional requirements for **CivicPulse (Version 1.1.0)**. CivicPulse is a municipal problem reporting, field-dispatch tracking, and community verification platform. It provides citizens, department officers, and municipal administrators with an end-to-end transparent workflow for reporting, repairing, and auditing public civic infrastructure failures (e.g., damaged roads, defunct streetlights, uncollected garbage, broken water mains, and clogged drainage networks).

### 1.2 Document Conventions
- **IEEE Std 830-1998 / ISO/IEC/IEEE 29148:2018** standards are adhered to for formatting and structure.
- Priority levels are indicated as:
  - **[MANDATORY]**: Core requirement without which the system cannot function.
  - **[HIGH]**: Crucial feature essential for complete operational workflow.
  - **[MEDIUM]**: Important enhancement for usability, performance, or localized operations.
- Requirements are tagged with unique IDs: `FR-<MODULE>-<ID>` for Functional Requirements and `NFR-<CATEGORY>-<ID>` for Non-Functional Requirements.

### 1.3 Intended Audience and Reading Suggestions
- **Academic Evaluators & Project Reviewers**: Focus on Sections 1, 2, 4, and 7 to assess design principles, requirements rigor, and validation criteria.
- **System Architects & Software Engineers**: Focus on Sections 3, 4, 5, and 6 for architecture patterns, API signatures, security mechanisms, and database constraints.
- **Quality Assurance & Testers**: Focus on Sections 4, 5, and 7 for acceptance criteria, boundary testing, and test coverage.

### 1.4 Product Scope
CivicPulse replaces opaque, paper-based, or manual grievance registers with a responsive Java-based web application. Key innovations in Version 1.1 include:
1. **Evidence-Based Repair**: Mandates original photograph evidence from citizens and before/after repair proof photos from assigned department officers.
2. **Citizen-Driven Verification**: Issues cannot be closed by municipal workers alone; only the reporting citizen can mark the issue `CLOSED` or `REOPEN` it if the repair is inadequate.
3. **Spatial Intelligence & Deduplication**: Real-time reverse-geocoding, Leaflet maps, 250m proximity candidate detection, and explicit duplicate linking where distinct citizen reports elevate community priority.
4. **Bilingual Accessibility & Preferences**: Native Hindi/English toggle with UI persistence and light/dark theme stored per user profile.
5. **Immutable Reporting with Recoverable Deletion**: Submitted reports cannot be edited or tampered with. Owners may move reports to a recoverable trash folder, preserving full timeline audit trails.

### 1.5 References
- IEEE Std 830-1998: Recommended Practice for Software Requirements Specifications.
- Spring Boot 3.5 Framework Reference Documentation.
- RFC 7519: JSON Web Token (JWT) Standard.
- OpenStreetMap & Photon Geocoding API v1 Specifications.
- Leaflet.js v1.9 Interactive Mapping Library Documentation.

---

## 2. Overall Description

### 2.1 Product Perspective
CivicPulse is an integrated monolithic web application packaged as an executable Web Application Archive (WAR), executed on an embedded Apache Tomcat 10 container with Spring Boot 3.5 and JSP/JSTL rendering. It interfaces directly with an underlying MySQL 8 relational database managed through Flyway schema migrations, and integrates asynchronously with the Komoot Photon Geocoding engine.

```
+-------------------------------------------------------------------------------+
|                             CLIENT DEVICES                                    |
|   Modern Web Browsers (Desktop, Tablet, Mobile) / HTML5 / CSS3 / ES6 / Leaflet  |
+---------------------------------------+---------------------------------------+
                                        | HTTPS / REST / JSP Requests
                                        v
+-------------------------------------------------------------------------------+
|                       CIVICPULSE MONOLITHIC RUNTIME (WAR)                     |
|  +-------------------------------------------------------------------------+  |
|  | Spring Security Filter Chain (JWT HttpOnly Cookie, CSRF Token Filter)   |  |
|  +-------------------------------------------------------------------------+  |
|  | Spring MVC Controllers (Auth, Account, Complaints, Admin, Resolution)  |  |
|  +-------------------------------------------------------------------------+  |
|  | Service Layer (SubmissionService, AdministrationService, Resolution)    |  |
|  +-------------------------------------------------------------------------+  |
|  | Data Access Layer (Spring JDBC NamedParameterJdbcTemplate, HikariCP)     |  |
|  +-------------------------------------------------------------------------+  |
+-----------------------------------+-------------------+-----------------------+
                                    |                   |
               Internal JDBC (HikariCP)                 | HTTPS (Cached/Throttled)
                                    v                   v
                     +------------------------+  +----------------------+
                     | MySQL 8 Database       |  | Photon Geocoder API  |
                     | (Flyway V001 - V600)   |  | (OpenStreetMap data) |
                     +------------------------+  +----------------------+
```

### 2.2 Product Functions
- **Citizen Experience**: Self-registration, multi-language/theme preference management, GPS/camera-enabled issue reporting, community map feed exploration, upvoting/supporting community issues, and repair validation.
- **Field Officer Operations**: Department-filtered work queues, status updates (`IN_PROGRESS`), field progress notes, and repair photo capture/upload for resolution submission.
- **Municipal Administration**: System-wide complaints management, automated or manual dispatch to officers, rejection of spurious grievances with audit notes, officer lifecycle provisioning, and tabular/CSV operational analytics.

### 2.3 User Classes and Characteristics
| User Class | Description | Technical Literacy | Key Permissions |
|---|---|---|---|
| **Citizen (`CITIZEN`)** | Residents of the municipal area reporting local grievances. | Basic / General Public | Create reports, delete/restore own reports, view public feed, support issues, close/reopen own reports. |
| **Field Officer (`OFFICER`)** | Field repair engineers or department personnel (Roads, Sanitation, etc.). | Moderate | View assigned department tickets, update progress, upload repair evidence, mark resolved. |
| **Administrator (`ADMIN`)** | Municipal commissioners, ward supervisors, or system admins. | High | View all tickets, assign/reassign tickets, reject tickets, register/enable officers, export audit data. |

### 2.4 Operating Environment
- **Server OS**: Linux (Ubuntu 22.04 LTS recommended), macOS Sonoma+, or Windows Server 2022.
- **Java Runtime**: OpenJDK 17 or Eclipse Temurin 17 (LTS).
- **Database Engine**: MySQL 8.0+ (InnoDB storage engine, UTF-8 unicode encoding) / H2 2.2+ (MySQL Mode) for ephemeral testing.
- **Client Web Browsers**: Chromium 110+, Mozilla Firefox 110+, Apple Safari 16+, Microsoft Edge 110+.

### 2.5 Design and Implementation Constraints
- **C1 - JSP Assembly Model**: Due to embedded Tomcat rendering JSPs from `/WEB-INF/views/`, the project must be compiled as an executable WAR.
- **C2 - Zero Frontend Framework Overhead**: Vanilla JavaScript, custom CSS tokens, Bootstrap 5 CSS, and Leaflet JS must be used; no Node.js/npm runtime build step is permitted in production deployment.
- **C3 - Relational BLOB Storage**: Images must be stored as canonical PNG `MEDIUMBLOB` directly within the database to maintain referential integrity with transaction-bound complaint records.
- **C4 - Geocoder Throttling**: External Photon geocoding calls must be globally throttled to a maximum of 1 request/second and cached for 5 minutes in-memory to prevent rate-limiting.

### 2.6 User Documentation
- Contextual in-app tooltips and bilingual help text in English and Hindi.
- Detailed administrative setup manual (`backend/docs/SETUP.md`).
- API and integration specifications (`BASE_CONTRACT.json`).

### 2.7 Assumptions and Dependencies
- Device location capture relies on browser Geolocation API (`navigator.geolocation`) requiring an active HTTPS context or `localhost`.
- Camera capture relies on HTML5 file input with `capture="environment"`.
- Internet access is assumed for downloading OpenStreetMap tiles and querying the Photon geocoding endpoint.

---

## 3. External Interface Requirements

### 3.1 User Interfaces
- **UI-1 Layout Structure**: Responsive grid built with Bootstrap 5 and custom CSS stylesheets (`app.css`, `maps.css`, `reporting.css`, `refinements.css`). Fluid layouts support screen resolutions from 360px (mobile) to 4K ultra-wide monitors.
- **UI-2 Theme Engine**: Supports three themes: `light`, `dark`, and `system` (media query `prefers-color-scheme`). Persisted via `account_profiles` table and `civic_theme` HTTP cookie.
- **UI-3 Bilingual Support**: Dynamic localization in English (`en`) and Hindi (`hi`). Translations provided via Java Resource Bundles (`ValidationMessages_hi.properties`) and client-side dictionary (`translations.js`).
- **UI-4 Interactive Map Widget**: Leaflet 1.9 map with OpenStreetMap tiles, custom marker clusters, draggable location pins, and 75-meter visual issue radius circles.
- **UI-5 Before/After Comparison Slider**: Custom interactive image split-view widget comparing citizen original damage photos with officer repair photos side-by-side.

### 3.2 Hardware Interfaces
- **HW-1 Camera Support**: Front and rear hardware cameras accessed through browser device APIs for real-time photo capture.
- **HW-2 GPS Receiver**: Device hardware GPS/GLONASS receivers queried for coordinates (Latitude, Longitude) with accuracy thresholds.

### 3.3 Software Interfaces
- **SI-1 Photon Geocoding Service**:
  - Endpoint: `GET https://photon.komoot.io/api/?q={query}&lat={lat}&lon={lon}&limit=5`
  - Reverse Geocoding: `GET https://photon.komoot.io/reverse?lat={lat}&lon={lon}`
- **SI-2 Relational Database**: MySQL 8 via JDBC (HikariCP connection pool, Flyway database migration engine).

### 3.4 Communications Interfaces
- **CI-1 Protocol**: HTTP/1.1 and HTTP/2 over TLS 1.3 (HTTPS).
- **CI-2 Cookies**: Secure, `HttpOnly`, `SameSite=Lax` cookies for session tracking (`civic_jwt`) and CSRF defense (`XSRF-TOKEN`).

---

## 4. System Features & Functional Requirements

### 4.1 Module 1: Authentication & Account Preferences

#### 4.1.1 Description
Provides secure authentication, role-based authorization, JWT cookie management, and user profile customization.

#### 4.1.2 Functional Requirements
- **FR-AUTH-01 [MANDATORY]**: Citizens can register by providing Display Name, Email, and Password. Role defaults strictly to `CITIZEN`.
- **FR-AUTH-02 [MANDATORY]**: Passwords must be hashed using BCrypt before persistence.
- **FR-AUTH-03 [MANDATORY]**: Authentication issues a cryptographically signed HMAC-SHA256 JWT stored in an `HttpOnly`, `SameSite=Lax` cookie.
- **FR-AUTH-04 [MANDATORY]**: Login session IDs (`token_id` UUID) must be stored in `login_sessions` table. Logging out must mark the session revoked immediately.
- **FR-AUTH-05 [HIGH]**: Users can update personal profiles (Date of Birth, Phone, Address, City, Postal Code, Neighbourhood, Home Coordinates, Language preference, Theme preference).
- **FR-AUTH-06 [HIGH]**: Profile updates must lock the user row to prevent race conditions during concurrent updates.

---

### 4.2 Module 2: Citizen Issue Reporting & Location Engine

#### 4.2.1 Description
Enables citizens to capture civic problems with photographs, precise coordinates, address lookup, and category selection.

#### 4.2.2 Functional Requirements
- **FR-REP-01 [MANDATORY]**: The system shall provide 5 default grievance categories: Roads, Streetlights, Garbage, Water supply, Drainage.
- **FR-REP-02 [MANDATORY]**: Every complaint must generate a unique, non-guessable alphanumeric tracking reference (e.g., `CP-2026-X89F2A`).
- **FR-REP-03 [MANDATORY]**: Latitude must be validated within [-90.0, 90.0] and Longitude within [-180.0, 180.0].
- **FR-REP-04 [HIGH]**: Photo uploads must accept PNG/JPEG formats, validate a maximum size of 5 MB, and re-encode images into canonical PNG format before database storage.
- **FR-REP-05 [HIGH]**: Real-time address suggestions must be provided via the Photon Geocoder, automatically cached for 5 minutes.
- **FR-REP-06 [MANDATORY]**: Submitted reports are **immutable**. Citizens cannot alter titles, descriptions, or locations post-submission.
- **FR-REP-07 [HIGH]**: Citizens may perform a recoverable soft-delete on their reports (`deleted_at` timestamp), moving them to the "Deleted Reports" tab, with the ability to restore them at any time.

---

### 4.3 Module 3: Community Feed, Proximity Matching & Interactive Map

#### 4.3.1 Description
Promotes community awareness, prevents duplicate submissions, and prioritizes civic issues based on community engagement.

#### 4.3.2 Functional Requirements
- **FR-COM-01 [MANDATORY]**: The Community Feed shall display public issues filtered by geographic radius (1 km to 50 km, default 5 km) based on the user's home location or GPS.
- **FR-COM-02 [HIGH]**: Proximity calculation uses a bounding-box query combined with the Haversine trigonometric formula:
  $$d = 2R \cdot \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)}\right)$$
- **FR-COM-03 [HIGH]**: During submission, if an active issue in the same category exists within 250 meters, the system presents the top 5 nearest issues as potential matches.
- **FR-COM-04 [HIGH]**: A citizen reporting an existing issue within 150 meters may explicitly link their complaint to an existing `issue_root_id`.
- **FR-COM-05 [HIGH]**: Priority is calculated by distinct non-deleted reporting citizens:
  - **High Priority (Red)**: $\ge 5$ distinct reporting citizens.
  - **Medium Priority (Yellow)**: $2 - 4$ distinct reporting citizens.
  - **Low Priority (Green)**: $1$ reporting citizen (or any resolved/closed issue).
- **FR-COM-06 [HIGH]**: Citizens can "Support" any community report once. Supported counts increment community ranking.
- **FR-COM-07 [MANDATORY]**: Community feeds must sanitize data: personal names, contact info, staff notes, and private attachments must be excluded from public view.

---

### 4.4 Module 4: Administrative Dispatch & Officer Provisioning

#### 4.4.1 Description
Gives ward administrators complete command and control over municipal issues, departmental routing, and staff management.

#### 4.4.2 Functional Requirements
- **FR-ADM-01 [MANDATORY]**: Admins can view all municipal reports with filtering by Status, Department, Category, and keyword search.
- **FR-ADM-02 [MANDATORY]**: Admins can assign an unassigned report (`REPORTED`) to a specific department and officer with a mandatory dispatch note. Transitions state to `ASSIGNED`.
- **FR-ADM-03 [HIGH]**: Admins can reassign a ticket to another officer if the ticket is still in `ASSIGNED` status.
- **FR-ADM-04 [MANDATORY]**: Admins can reject invalid/out-of-scope grievances by supplying a mandatory rejection reason. Transitions state to `REJECTED`.
- **FR-ADM-05 [HIGH]**: Admins can provision Field Officer accounts by specifying Full Name, Email, Password, and Department.
- **FR-ADM-06 [HIGH]**: Admins can toggle officer access (`enabled = true/false`). Disabling an officer immediately invalidates active sessions.
- **FR-ADM-07 [MANDATORY]**: Every administrative decision is logged in `administration_events` table with actor ID, timestamp, and action note.

---

### 4.5 Module 5: Field Officer Resolution & Repair Proof

#### 4.5.1 Description
Empowers field officers to manage assigned tasks, log repair progress, and provide verifiable proof of work.

#### 4.5.2 Functional Requirements
- **FR-RES-01 [MANDATORY]**: Officers can only view complaints assigned to their unique user ID (`assigned_officer_id = current_user.id`).
- **FR-RES-02 [MANDATORY]**: Officer clicks "Start Work" to transition ticket from `ASSIGNED` to `IN_PROGRESS` with an optional note.
- **FR-RES-03 [HIGH]**: Officer can add ongoing work notes to inform administrators and citizens of progress.
- **FR-RES-04 [MANDATORY]**: To resolve a ticket, the officer must submit a resolution note and upload a mandatory/optional repair evidence photo (`purpose = 'REPAIR'`). Transitions state to `RESOLVED` and records `resolved_at`.

---

### 4.6 Module 6: Citizen Verification & Audit Trail

#### 4.6.1 Description
Guarantees accountability by requiring the citizen who originally logged the issue to verify and close it.

#### 4.6.2 Functional Requirements
- **FR-VER-01 [MANDATORY]**: When an issue is `RESOLVED`, the owning citizen is presented with an interactive before/after photo slider comparing their original upload with the officer's repair photo.
- **FR-VER-02 [MANDATORY]**: If satisfied, the citizen clicks "Confirm Resolution", which transitions status to `CLOSED` and records `closed_at`.
- **FR-VER-03 [MANDATORY]**: If the repair is defective or incomplete, the citizen can click "Reopen Issue" with a mandatory feedback note. This transitions state back to `IN_PROGRESS`.
- **FR-VER-04 [MANDATORY]**: Reopening preserves all previous history, event logs, original photos, and previous repair photos.
- **FR-VER-05 [MANDATORY]**: Every status change appends a row to `complaint_events` with `actor_id`, `from_status`, `to_status`, `action`, and `note`.

---

### 4.7 Module 7: Analytics, CSV Export & Data Governance

#### 4.7.1 Description
Provides operational insights, SLA performance metrics, and CSV reporting with security hardening.

#### 4.7.2 Functional Requirements
- **FR-ANA-01 [HIGH]**: Dashboards display total complaints count, breakdowns by status, and category distributions scoped by role:
  - Admin: All community reports.
  - Officer: Own assigned reports.
  - Citizen: Own reported items.
- **FR-ANA-02 [HIGH]**: System computes Mean Time to Resolution (MTTR) measuring elapsed hours from initial citizen submission (`created_at`) to citizen confirmation (`closed_at`).
- **FR-ANA-03 [HIGH]**: Admins and Officers can export ticket lists as CSV files.
- **FR-ANA-04 [MANDATORY]**: CSV exports must neutralize spreadsheet formula injection attacks (CSV Injection / CWE-1236) by prepending a single quote (`'`) to cells starting with `=`, `+`, `-`, or `@`.

---

## 5. Non-Functional Requirements (NFRs)

### 5.1 Performance Requirements
- **NFR-PERF-01**: Average response time for web pages must not exceed 800 milliseconds under a concurrent load of 100 active sessions.
- **NFR-PERF-02**: Geocoding query responses must be returned within 300 milliseconds when cached.
- **NFR-PERF-03**: Image loading from database BLOB must stream via HTTP 200 with `no-store` cache control within 500 milliseconds for a 2 MB image.
- **NFR-PERF-04**: Database connection pool (HikariCP) is tuned with a maximum pool size of 8 connections, ensuring minimal thread starvation.

### 5.2 Safety and Integrity Requirements
- **NFR-SAFE-01**: Database transactions are managed using `@Transactional` with row-level locks (`SELECT ... FOR UPDATE`) during status transitions to prevent race conditions and double-dispatch.
- **NFR-SAFE-02**: Recoverable soft-deletion ensures no grievance record or associated legal evidence is destroyed prematurely.

### 5.3 Security Requirements
- **NFR-SEC-01**: All incoming requests that alter state (`POST`, `PUT`, `DELETE`) must validate a synchronized CSRF token.
- **NFR-SEC-02**: Passwords must be hashed using BCrypt with a work factor of 10.
- **NFR-SEC-03**: All SQL interactions must use parameterized queries via `NamedParameterJdbcTemplate` to completely eliminate SQL Injection vulnerabilities.
- **NFR-SEC-04**: Direct object references (IDOR) on report photos and details are prevented by validating user ownership or authorized staff role before serving content.
- **NFR-SEC-05**: User-uploaded file path data is discarded; files are decoded in-memory and stored with auto-incremented database identifiers to prevent Path Traversal attacks.

### 5.4 Software Quality Attributes
- **Usability**: Fully bilingual interface (English and Hindi), high contrast dark and light modes, accessible form controls, and mobile-friendly touch targets ($\ge 44 \times 44\text{ px}$).
- **Maintainability**: Clear modular separation into 5 modules (Module 1: Shared UI; Module 2: Staff UI; Module 3: Core Backend & Security; Module 4: Staff Workflows; Module 5: Database Schema & Migrations).
- **Portability**: Packaged as a standard executable WAR, capable of running on any Java 17 runtime environment with zero platform-specific binary dependencies.

---

## 6. Database & Data Storage Requirements

### 6.1 Relational Schema & Integrity Constraints
The schema is managed by Flyway migrations (`V001` through `V600`):
- `departments`: Pre-seeded with 5 municipal bodies.
- `users`: Stores credentials, display names, roles (`CITIZEN`, `OFFICER`, `ADMIN`), and optional department foreign key.
- `login_sessions`: Session tokens with expiration timestamps and revocation flags.
- `account_profiles`: One-to-one user profiles with language, theme, and optional home coordinates.
- `categories`: Pre-seeded grievance categories mapped to departments.
- `complaints`: Core ticket table with GPS coordinates, status constraint check, issue root self-reference, and deletion timestamp.
- `complaint_events`: Audit trail capturing every lifecycle state change.
- `attachments`: Stores PNG BLOBs with purpose constraint (`ORIGINAL`, `REPAIR`).
- `complaint_supports`: Composite primary key `(complaint_id, citizen_id)` enforcing single upvote per citizen.
- `administration_events`: Audit log for administrative assignments and officer access toggling.

---

## 7. Verification & Acceptance Criteria

| ID | Feature | Verification Procedure | Expected Outcome |
|---|---|---|---|
| **TC-01** | Citizen Registration | Submit registration form with unique email and password. | Account created, redirected to login, default role is CITIZEN. |
| **TC-02** | Report Submission | Submit issue with title, category, GPS coordinates, and 2 MB photo. | Reference generated, status is REPORTED, photo stored in attachments. |
| **TC-03** | Immutable Editing | Attempt HTTP GET/POST to `/citizen/reports/{id}/edit`. | HTTP 409 Conflict returned with explicit error message. |
| **TC-04** | Admin Reassignment | Admin assigns ticket to Sanitation officer with note. | Status changes to ASSIGNED, event logged in complaint_events. |
| **TC-05** | Officer Resolution | Officer starts work, then submits resolution with repair photo. | Status changes to RESOLVED, repair image uploaded with REPAIR purpose. |
| **TC-06** | Citizen Reopening | Citizen views resolved ticket, inputs grievance note, clicks Reopen. | Status reverts to IN_PROGRESS, past events and both photos retained. |
| **TC-07** | Duplicate Priority | 5 distinct citizens submit reports linking to same issue root. | Issue badge dynamically displays Red / High Priority in Feed. |
| **TC-08** | CSV Neutralization | Export CSV containing a title starting with `=CMD|' /C CALC'!A0`. | Exported cell begins with `'=` preventing formula execution. |
