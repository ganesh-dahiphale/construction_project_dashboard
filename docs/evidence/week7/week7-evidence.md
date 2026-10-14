# Week 7 DevOps Milestone Evidence Report 🏗️

**Project**: Construction Progress Dashboard (Java 17, Spring Boot 3.3.4, Maven, Thymeleaf, Spring Data JPA, H2)  
**Milestone**: Complete MVP Functionality, Parallel Feature Branches, Merge Conflict Resolution, Stabilisation & Production Release `v1.0.0`  
**Repository**: [ganesh-dahiphale/construction_project_dashboard](https://github.com/ganesh-dahiphale/construction_project_dashboard)  
**Release Tag**: [`v1.0.0`](https://github.com/ganesh-dahiphale/construction_project_dashboard/releases/tag/v1.0.0)  

---

## 📋 Table of Contents

1. [Phase 1: Issues & Parallel Feature Branches](#1-phase-1-issues--parallel-feature-branches)
2. [Phase 2: Feature C — Login & Role-Based Access (US-01)](#2-phase-2-feature-c--login--role-based-access-us-01)
3. [Phase 3: Feature D — Task Status Updates & Admin Project Management (US-03, US-08)](#3-phase-3-feature-d--task-status-updates--admin-project-management-us-03-us-08)
4. [Phase 4: Pull Requests, Conflict Creation & Merge Resolution](#4-phase-4-pull-requests-conflict-creation--merge-resolution)
5. [Phase 5: Stabilisation & Edge-Case Verification](#5-phase-5-stabilisation--edge-case-verification)
6. [Phase 6: Production Release v1.0.0 & Release Artifacts](#6-phase-6-production-release-v100--release-artifacts)
7. [Phase 7: Product Backlog Synchronization (B-01 to B-14)](#7-phase-7-product-backlog-synchronization-b-01-to-b-14)
8. [Automated Test Suite Summary](#8-automated-test-suite-summary)
9. [Git Commit History & Graph](#9-git-commit-history--graph)
10. [Application Route Directory & Credentials](#10-application-route-directory--credentials)

---

## 1. Phase 1: Issues & Parallel Feature Branches

Two feature issues were created via `gh issue create` with user stories and acceptance criteria, branched in parallel from the common `develop` base commit (`0a33707`):

### Created Issues

| Identifier | Issue # | Title | Issue URL | Branch Name |
|---|---|---|---|---|
| **Feature C** | #25 | Login and role-based access (Admin, Manager, Engineer) | [Issue #25](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/25) | `feature/25-login-roles` |
| **Feature D** | #26 | Task status update and admin project management | [Issue #26](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/26) | `feature/26-status-update-admin` |

---

## 2. Phase 2: Feature C — Login & Role-Based Access (US-01)

### Changes Implemented
- Configured **Spring Security 6** with form-based authentication and BCrypt password encryption (`SecurityConfig.java`).
- Created `User` domain entity, `Role` enum (`ADMIN`, `MANAGER`, `ENGINEER`), and `UserRepository`.
- Implemented `CustomUserDetailsService` querying database users.
- Seeded default dev accounts with configurable environment variables:
  - `admin` (`DEV_ADMIN_PASSWORD`, default `admin123`) -> `ADMIN`
  - `manager` (`DEV_MANAGER_PASSWORD`, default `manager123`) -> `MANAGER`
  - `engineer` (`DEV_ENGINEER_PASSWORD`, default `engineer123`) -> `ENGINEER`
- Added custom Bootstrap 5 styled login page (`/login`) with error banners and logout redirection (`login.html`).
- Added user badge and CSRF-protected logout button in `navbar.html`.
- Implemented MockMvc security tests in `SecurityTest.java` (anonymous redirects, form login, role 403 vs 200).

### Feature C Commits
- `feat(model): user entity and roles (#25)`
- `feat(security): login and access rules (#25)`
- `feat(web): login page and navbar user block (#25)`
- `test(security): role tests (#25)`
- `docs(readme): security section (#25)`

---

## 3. Phase 3: Feature D — Task Status Updates & Admin Project Management (US-03, US-08)

### Changes Implemented
- Added task update endpoints (`GET /tasks/{id}/edit`, `POST /tasks/{id}/edit`) with validation and business rules:
  - Setting status to `COMPLETED` automatically forces `percentComplete` to 100%.
  - Added auditing fields `updatedBy` and `lastUpdated` timestamp.
- Added edit action links to every task row across `/tasks`, `/dashboard`, and `/status/{status}`.
- Added project management endpoints for administrators (`GET /admin/projects`, `GET /admin/projects/new`, `POST /admin/projects`, `GET/POST /admin/projects/{id}/edit`).
- Created friendly error templates (`error/400.html`, `error/403.html`, `error/404.html`, `error.html`).
- Created unit and controller test suites (`ProjectServiceTest.java`, `AdminProjectControllerTest.java`, and edit tests in `TaskControllerTest.java`).

### Feature D Commits
- `feat(service): status update rules (#26)`
- `feat(web): edit task page (#26)`
- `feat(web): admin project pages (#26)`
- `fix(web): error pages (#26)`
- `test(task): update tests (#26)`
- `docs(readme): task updates section (#26)`

---

## 4. Phase 4: Pull Requests, Conflict Creation & Merge Resolution

### Step 1: Feature C Merged to Develop
PR #27 was raised, reviewed, and merged into `develop`:
- **PR URL**: [PR #27](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/27)
- **Review Comment**: *"LGTM! Spring Security configuration and RBAC controls are well-structured and properly covered by MockMvc tests."*
- **Merge Method**: Merge commit with branch deletion.

### Step 2: Feature D PR Conflict Detection
PR #28 was raised from `feature/26-status-update-admin` into `develop`:
- **PR URL**: [PR #28](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/28)
- **GitHub Mergeable Status**:
```json
{
  "mergeStateStatus": "DIRTY",
  "mergeable": "CONFLICTING",
  "title": "feat(tasks): task status updates and admin project management"
}
```

### Step 3: Local Merge & Pre-Resolution Conflict Markers

When merging `develop` into `feature/26-status-update-admin` locally:

```text
$ git merge origin/develop
Auto-merging README.md
CONFLICT (content): Merge conflict in README.md
Auto-merging src/main/resources/templates/fragments/navbar.html
CONFLICT (content): Merge conflict in src/main/resources/templates/fragments/navbar.html
Automatic merge failed; fix conflicts and then commit the result.
```

#### Unmerged Files (`git diff --name-only --diff-filter=U`)
```text
README.md
src/main/resources/templates/fragments/navbar.html
```

#### `navbar.html` Conflict Section (Before Resolution)
```html
<<<<<<< HEAD
                <li class="nav-item ms-lg-2 mt-2 mt-lg-0">
                    <a class="btn btn-outline-info btn-sm fw-semibold" th:href="@{/admin/projects}">
                        <i class="bi bi-gear-wide-connected me-1"></i>Admin
                    </a>
=======
                <li class="nav-item ms-lg-3 mt-2 mt-lg-0" sec:authorize="isAuthenticated()">
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge bg-secondary text-light px-2 py-1">
                            <i class="bi bi-person-circle text-warning me-1"></i>
                            <span sec:authentication="name">User</span>
                        </span>
                        <form th:action="@{/logout}" method="post" class="d-inline m-0">
                            <button type="submit" class="btn btn-outline-danger btn-sm py-1 px-2" title="Logout">
                                <i class="bi bi-box-arrow-right me-1"></i>Logout
                            </button>
                        </form>
                    </div>
>>>>>>> origin/develop
                </li>
```

#### `README.md` Conflict Section (Before Resolution)
```markdown
<<<<<<< HEAD
## 📝 Task Updates & Project Administration

- **Progress Updates (`/tasks/{id}/edit`)**: Update task execution status, percentage completion (0-100%), and progress notes. Automatic business rule: setting status to `COMPLETED` automatically forces `percentComplete` to 100%. Tracks `lastUpdated` and `updatedBy`.
- **Project Site Management (`/admin/projects`)**: Create, inspect, and update construction site definitions and timelines.
- **Robust Error Handling**: Dedicated, user-friendly error views for 400 Bad Request, 403 Forbidden, 404 Not Found, and 500 Internal Error.
=======
## 🔐 Security & Role-Based Access

The application enforces Spring Security with role-based access control (RBAC):
- **ADMIN**: Full system access including project management (`/admin/**`), task operations, and executive dashboards.
- **MANAGER**: Access to Executive Dashboard (`/dashboard`), Status Drill-Down (`/status/**`), Alerts (`/alerts`), and task workflows.
- **ENGINEER**: Access to view, create, and update tasks (`/tasks/**`).
- **Public**: Health check (`/health`), login page (`/login`), and static assets.

### Development Default Credentials

| Role | Username | Default Password | Environment Variable |
|---|---|---|---|
| Administrator | `admin` | `admin123` | `DEV_ADMIN_PASSWORD` |
| Project Manager | `manager` | `manager123` | `DEV_MANAGER_PASSWORD` |
| Site Engineer | `engineer` | `engineer123` | `DEV_ENGINEER_PASSWORD` |
>>>>>>> origin/develop
```

### Step 4: Resolved Code

#### Resolved `navbar.html`
```html
                <li class="nav-item ms-lg-2 mt-2 mt-lg-0" sec:authorize="hasRole('ADMIN')">
                    <a class="btn btn-outline-info btn-sm fw-semibold" th:href="@{/admin/projects}">
                        <i class="bi bi-gear-wide-connected me-1"></i>Admin
                    </a>
                </li>
                <li class="nav-item ms-lg-3 mt-2 mt-lg-0" sec:authorize="isAuthenticated()">
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge bg-secondary text-light px-2 py-1">
                            <i class="bi bi-person-circle text-warning me-1"></i>
                            <span sec:authentication="name">User</span>
                        </span>
                        <form th:action="@{/logout}" method="post" class="d-inline m-0">
                            <button type="submit" class="btn btn-outline-danger btn-sm py-1 px-2" title="Logout">
                                <i class="bi bi-box-arrow-right me-1"></i>Logout
                            </button>
                        </form>
                    </div>
                </li>
```

#### Resolved `README.md` (Security First, then Task Updates)
```markdown
## 🔐 Security & Role-Based Access

The application enforces Spring Security with role-based access control (RBAC):
- **ADMIN**: Full system access including project management (`/admin/**`), task operations, and executive dashboards.
- **MANAGER**: Access to Executive Dashboard (`/dashboard`), Status Drill-Down (`/status/**`), Alerts (`/alerts`), and task workflows.
- **ENGINEER**: Access to view, create, and update tasks (`/tasks/**`).
- **Public**: Health check (`/health`), login page (`/login`), and static assets.

### Development Default Credentials

| Role | Username | Default Password | Environment Variable |
|---|---|---|---|
| Administrator | `admin` | `admin123` | `DEV_ADMIN_PASSWORD` |
| Project Manager | `manager` | `manager123` | `DEV_MANAGER_PASSWORD` |
| Site Engineer | `engineer` | `engineer123` | `DEV_ENGINEER_PASSWORD` |

---

## 📝 Task Updates & Project Administration

- **Progress Updates (`/tasks/{id}/edit`)**: Update task execution status, percentage completion (0-100%), and progress notes. Automatic business rule: setting status to `COMPLETED` automatically forces `percentComplete` to 100%. Tracks `lastUpdated` and `updatedBy`.
- **Project Site Management (`/admin/projects`)**: Create, inspect, and update construction site definitions and timelines.
- **Robust Error Handling**: Dedicated, user-friendly error views for 400 Bad Request, 403 Forbidden, 404 Not Found, and 500 Internal Error.
```

### Step 5: Feature Integration & Merge Commit
- Updated `TaskController.java` to set `updatedBy` from authenticated `Principal.getName()`.
- Verified `/admin/**` restriction in `SecurityConfig.java` and `SecurityTest.java`.
- Commit Hash: `bce9fae` (`merge: resolve navbar and README conflicts with develop (#26)`).
- Pushed and merged PR #28 to `develop`.

---

## 5. Phase 5: Stabilisation & Edge-Case Verification

On branch `chore/stabilise-v1.0.0`:
- Created `EdgeCasesTest.java` with 5 automated edge case validations:
  1. Empty search queries in dashboard returns matching items or empty results without exception.
  2. Overdue boundary dates (`dueDate == today` is NOT overdue, `dueDate == yesterday` IS overdue unless completed).
  3. Progress boundaries at 0% and 100%, and status auto-coercion.
  4. AlertService null-safety on zero alerts.
  5. Unauthorized role access boundaries (Engineer blocked from admin & dashboard, Manager blocked from admin, anonymous redirected to login).
- Externalized configuration via environment variables in `application.properties` (`PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`).
- Verified WAR packaging (`mvn clean package` producing `target/dashboard.war`).
- Updated `CHANGELOG.md` with full `v1.0.0` notes.
- Merged via PR #29 into `develop`.

---

## 6. Phase 6: Production Release v1.0.0 & Release Artifacts

- Merged `develop` into `main` via PR #30 ([PR #30](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/30)).
- Tagged release:
```text
$ git tag -n
v0.2.0          MVP baseline: entry, dashboard, indicators, drill-down, alerts
v1.0.0          MVP complete: entry, update, dashboard, indicators, drill-down, alerts, login
```
- Published GitHub Release: [v1.0.0 MVP Release](https://github.com/ganesh-dahiphale/construction_project_dashboard/releases/tag/v1.0.0)
- Attached release artifact: `dashboard.war`

---

## 7. Phase 7: Product Backlog Synchronization (B-01 to B-14)

Updated [`docs/BACKLOG.md`](../../docs/BACKLOG.md) reflecting **9 / 14 (64.3%) items Done**:

| Item | Title | Status |
|---|---|---|
| **B-01** | Initialize application skeleton & CI/CD baseline | **Done** |
| **B-02** | Task entry form and domain model | **Done** |
| **B-03** | Searchable dashboard, status drill-down & alert view | **Done** |
| **B-04** | Task status update & completion rules | **Done** |
| **B-05** | Spring Security & Role-Based Access Control | **Done** |
| **B-06** | Automated CI verification & build pipeline | **Done** |
| **B-07** | Edge-case test suite (58 tests) & code quality | **Done** |
| **B-08** | Standalone & Tomcat 10 WAR packaging | **Done** |
| **B-09** | Externalized environment variables & configuration | **Done** |
| **B-10 – B-14** | Advanced DevOps (IaC, CD, Vault, Prometheus, Trivy) | **To Do** |

---

## 8. Automated Test Suite Summary

Total Tests: **58 / 58 Passing (100% Green)**

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.dashboard.EdgeCasesTest (5 tests) - OK
[INFO] Running com.example.dashboard.HealthControllerTest (1 test) - OK
[INFO] Running com.example.dashboard.controller.AdminProjectControllerTest (6 tests) - OK
[INFO] Running com.example.dashboard.controller.AlertControllerTest (4 tests) - OK
[INFO] Running com.example.dashboard.controller.DashboardControllerTest (7 tests) - OK
[INFO] Running com.example.dashboard.controller.HomeControllerTest (1 test) - OK
[INFO] Running com.example.dashboard.controller.StatusControllerTest (7 tests) - OK
[INFO] Running com.example.dashboard.controller.TaskControllerTest (9 tests) - OK
[INFO] Running com.example.dashboard.security.SecurityTest (7 tests) - OK
[INFO] Running com.example.dashboard.service.AlertServiceTest (4 tests) - OK
[INFO] Running com.example.dashboard.service.DashboardServiceTest (5 tests) - OK
[INFO] Running com.example.dashboard.service.ProjectServiceTest (5 tests) - OK
[INFO] Running com.example.dashboard.service.TaskServiceTest (7 tests) - OK
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 58, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 9. Git Commit History & Graph

```text
*   38b5f6c (tag: v1.0.0, origin/main, main) Merge pull request #30 from ganesh-dahiphale/develop
|\  
| *   0451656 (origin/develop, origin/HEAD, develop) Merge pull request #29 from ganesh-dahiphale/chore/stabilise-v1.0.0
| |\  
| | * a8cfcb9 feat(test): add edge-case test suite and v1.0.0 documentation
| |/  
| *   6f9d558 Merge pull request #28 from ganesh-dahiphale/feature/26-status-update-admin
| |\  
| | *   bce9fae merge: resolve navbar and README conflicts with develop (#26)
| | |\  
| | |/  
| |/|   
| * |   82af66a Merge pull request #27 from ganesh-dahiphale/feature/25-login-roles
| |\ \  
| | * | 026fcf9 docs(readme): security section (#25)
| | * | 600d62a test(security): role tests (#25)
| | * | 23e544d feat(web): login page and navbar user block (#25)
| | * | 7e69a15 feat(security): login and access rules (#25)
| | * | 4168024 feat(model): user entity and roles (#25)
| |/ /  
| | * 624b701 docs(readme): task updates section (#26)
| | * f7ccc1d test(task): update tests (#26)
| | * 9650db9 fix(web): error pages (#26)
| | * 19faf0a feat(web): admin project pages (#26)
| | * 2b8d442 feat(web): edit task page (#26)
| | * 6a0b985 feat(service): status update rules (#26)
| |/  
| *   0a33707 Merge pull request #24 from ganesh-dahiphale/docs/week6-evidence
| |\  
| | * 77ee6d4 docs(week6): add Week 6 evidence and product backlog
| |/  
* | 802931f (tag: v0.2.0) Merge pull request #12 from ganesh-dahiphale/develop
```

---

## 10. Application Route Directory & Credentials

### Default Development Credentials

| Role | Username | Password | Environment Variable | Permissions |
|---|---|---|---|---|
| **Administrator** | `admin` | `admin123` | `DEV_ADMIN_PASSWORD` | Full system, `/admin/projects`, task updates, dashboard, alerts |
| **Project Manager** | `manager` | `manager123` | `DEV_MANAGER_PASSWORD` | `/dashboard`, `/status/**`, `/alerts`, task viewing & entry |
| **Site Engineer** | `engineer` | `engineer123` | `DEV_ENGINEER_PASSWORD` | `/tasks/**` (task viewing, creation, progress updates) |

---
*Generated autonomously as part of Week 7 DevOps Milestone Deliverables.*
