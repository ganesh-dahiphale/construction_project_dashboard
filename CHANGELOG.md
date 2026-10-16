# Changelog

All notable changes to the **Construction Progress Dashboard** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Admin User Management (`/admin/users`)**:
  - Central administrative registry to create, view, update, and delete staff accounts.
  - Role restriction: admin can only register staff as `ENGINEER` or `MANAGER`; `ADMIN` role is rejected server-side.
  - User model extensions: `fullName`, unique `email`, validated `username`, employment `jobStatus` (`ACTIVE`, `ON_LEAVE`, `INACTIVE`), and audit timestamps (`createdAt`, `lastUpdated`, `createdBy`).
  - Account lifecycle controls: dynamic status change, profile edits, password resets (min 8 chars with letter and digit), and account deletion with Bootstrap confirmation modal.
  - Server-side guards: administrators cannot delete, deactivate, or change the role of their own account; the last active administrator cannot be removed or deactivated; administrator accounts are read-only.
  - Login access control: only accounts with `ACTIVE` status can authenticate; non-active users are rejected with `"Account is inactive or on leave"`.
  - Session invalidation: deactivating or deleting a user immediately terminates their active sessions via `SessionRegistry`.
  - Data integrity: task records preserve historical `updatedBy` references without foreign key deletion cascade.
- **Persistent Database Storage**:
  - Default development and container runtime database switched to file-based H2 (`jdbc:h2:file:./data/dashboard;AUTO_SERVER=false`).
  - Idempotent `DataInitializer` that prevents seed duplication on restarts.
  - Volume mount `-v construction-dashboard-data:/app/data` added to Docker runtime and Jenkins deployment pipeline.
  - Persistent `/opt/construction-dashboard/data` managed by Ansible `app_dirs` role and mounted in systemd service unit.
- **Automated Quality Testing**:
  - `UserManagementServiceTest`: 14 unit tests validating all guards, validation rules, password constraints, and task safety.
  - `AdminUserSecurityTest`: 12 MockMvc tests verifying role-based authorization, 403 enforcement, CSRF requirements, and disabled login rejection.
  - `AdminUserManagementJourneyTest`: Selenium Journey J6 validating end-to-end admin user creation, engineer login, status changes, deletion, and non-admin restrictions.
  - Updated `docs/TEST_PLAN.md` with Journey J6 specifications.

## [v1.1.0] - 2026-10-15 — Final End-to-End Release (CI/CD, Selenium Gate, Docker & Ansible)

### Added
- **Ansible Configuration Management & Continuous Deployment (`ansible/`)**:
  - Full automated provisioning and deployment pipeline with modular Ansible roles: `common`, `docker`, `app_user`, `app_dirs`, `firewall`, `app_config`, and `deploy`.
  - Atomic zero-downtime container deployment supervised by `systemd` unit (`construction-dashboard.service`).
  - Automated deployment rollback protection: health verification gate triggers immediate atomic rollback to previous image on HTTP `/health` or UI version check failure.
  - State tracking in `/opt/construction-dashboard/releases/` via `current.txt`, `previous.txt`, and append-only `history.log`.
  - Manual operator rollback playbook (`ansible/playbooks/rollback.yml`) and status inspection playbook (`status.yml`).
  - Production security controls: dedicated non-privileged service user (`appdeploy`), restricted file permissions (0600 on secrets), and strict UFW firewall configuration (22/tcp SSH, 8082/tcp App).
- **Jenkins Pipeline Ansible Deployment & Verification Stages**:
  - Added `Provision and Deploy (Ansible)` stage executing `provision.yml` with dynamic release parameters.
  - Added `Verify Node` stage running `healthcheck.yml` and testing external HTTP health endpoints from build agent.
  - Automatic promotion of verified releases to `:stable` Docker tag.
  - Automated archival of `deploy-info.txt`, `ansible-deploy.log`, and `ansible-healthcheck.log`.
  - Workstation deployment runners (`scripts/deploy-node.sh` and `scripts/deploy-node.ps1`) for hybrid CI/CD environments.
- **Selenium Automated Quality Gate**:
  - Headless Chrome end-to-end browser testing validating mission-critical user journeys.
  - Quality gate blockade pattern preventing defective releases from proceeding to Docker packaging or server deployment.
- **Production Audit & Idempotency**:
  - Verified strict idempotency (`changed=0`) across consecutive Ansible provisioning runs.

---

## [v1.0.0] - 2026-10-05 — Production-Ready MVP Release

### Added
- **Spring Security & Role-Based Access Control (RBAC)**:
  - Form-based login (`GET /login`) with Bootstrap 5 styled view and validation feedback.
  - User domain entity with BCrypt password hashing and `Role` enumeration (`ADMIN`, `MANAGER`, `ENGINEER`).
  - Strict route authorization hierarchy:
    - `ADMIN`: Full system access, project management (`/admin/**`), task operations, and executive dashboards.
    - `MANAGER`: Executive dashboard (`/dashboard`), status drill-downs (`/status/**`), alerts (`/alerts`), and task workflows.
    - `ENGINEER`: Task creation, viewing, and progress updates (`/tasks/**`).
    - Public: `/health`, `/login`, static assets (`/css/**`, `/js/**`, `/images/**`).
  - Configurable dev seed credentials via environment variables (`DEV_ADMIN_PASSWORD`, `DEV_MANAGER_PASSWORD`, `DEV_ENGINEER_PASSWORD`).
  - Active user indicator and secure CSRF-protected logout in global navigation.
- **Task Status & Progress Updates (`GET /tasks/{id}/edit`, `POST /tasks/{id}/edit`)**:
  - Live task editing form allowing updates to execution status, percent complete (0–100%), and ground remarks.
  - Automatic business rule: changing status to `COMPLETED` automatically forces `percentComplete` to 100%.
  - Auditing integration: tracks `lastUpdated` timestamp and sets `updatedBy` to authenticated principal username.
  - Edit quick-action links added across task list, dashboard table, and status drill-down views.
- **Project Site Management (`GET /admin/projects`, `GET /admin/projects/new`, `POST /admin/projects`, `GET/POST /admin/projects/{id}/edit`)**:
  - Dedicated admin interface for creating and updating construction site definitions and timelines.
- **Friendly Custom Error Views**:
  - Custom branded error templates for HTTP 400 (Bad Request), 403 (Access Denied), 404 (Resource Not Found), and 500 (Internal Server Error).
- **Comprehensive Edge Case & Security Testing Suite**:
  - 58 automated unit, controller, security MockMvc, and edge-case tests (100% green).
  - Externalized environment configuration (`PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`).
  - Standalone and Tomcat 10 compatible WAR archive distribution.

---

## [v0.2.0] - 2026-10-05 — MVP Feature Baseline

### Added
- **Executive Progress Dashboard (`GET /dashboard`)**:
  - Real-time KPI summary indicator cards: Total Tasks, Completed, In Progress, Blocked, Delayed Tasks (`dueDate < today` & uncompleted), and Average Completion Rate.
  - Multi-criteria search and filtering: keyword search (matching title or remarks case-insensitively), project selector, status selector, and due date range filter (`dueDateFrom` / `dueDateTo`).
  - Dynamic Spring Data JPA Specification query with sorted results table.
- **Status Drill-Down (`GET /status` and `GET /status/{status}`)**:
  - Dedicated overview page indexing all status categories with real-time task counts.
  - Drill-down views for `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`, and `delayed` tasks.
- **Alert & Exception View (`GET /alerts`)**:
  - High-priority exception dashboard tracking overdue tasks with calculated "Days Overdue".
  - Dedicated blocked site activities table with impediment/reason tracking.
  - Empty-state banner when zero alerts are active.
- **Task Entry & Event Logging (`GET /tasks`, `GET /tasks/new`, `POST /tasks`)**:
  - Full CRUD entry form with Bean Validation, flash feedback, and responsive Bootstrap 5 styling.
- **Testing & Quality Assurance**:
  - 27 automated tests covering domain validation, service calculations, and MockMvc controller endpoints.

### Changed
- Refactored `fragments/navbar.html` to integrate Home, Dashboard, Tasks, Status, and Alerts links into a cohesive navigation bar.
- Updated `README.md` with complete MVP feature documentation, route directory, and local execution guides.

---

## [v0.1.0] - 2026-10-05 — Initial Skeleton & CI/CD Baseline

### Added
- Project skeleton initialized with Spring Boot 3.3.4, Java 17, Maven Wrapper, and WAR packaging.
- System health endpoint (`GET /health`) returning JSON status and timestamp.
- GitHub issue and pull request templates, and branch protection policy documentation.
