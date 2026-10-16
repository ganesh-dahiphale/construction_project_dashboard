# Feature Evidence: ADMIN-Only User Management and Persistence (#68)

## 1. Executive Summary

This document provides formal DevOps verification and end-to-end evidence for the **ADMIN-only User Management** feature implemented for the **Construction Progress Dashboard** (Java 17, Spring Boot 3, Docker, Jenkins CI/CD, Ansible).

The feature allows administrators to manage operational personnel accounts (`ENGINEER` and `MANAGER`), update job statuses (`ACTIVE`, `ON_LEAVE`, `INACTIVE`), edit profiles, reset passwords, and securely remove staff accounts with full session termination and database persistence across container restarts.

- **GitHub Issue**: [#68](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/68)
- **Target Branch**: `develop`
- **Jenkins CI/CD Pipeline Job**: `construction-dashboard-pipeline`
- **Verified Successful Build**: [Build #11](http://localhost:8080/job/construction-dashboard-pipeline/11/) (`SUCCESS`)
- **Deployed Container Verification**: `http://localhost:8082/` (Status: Healthy / UP, Version: `3.3.4-b11-9f8a79d`)

---

## 2. GitHub Issues & Pull Requests

| ID | Title / Purpose | Branch | Status | URL |
|---|---|---|---|---|
| **Issue #68** | Feature: Admin User Management and Persistence | - | Open (Resolution Complete) | [Issue #68](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/68) |
| **PR #69** | feat(users): admin user management, persistence and security (#68) | `feature/68-user-management` -> `develop` | Merged | [PR #69](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/69) |
| **PR #70** | Fix Windows cmd redirection syntax in Jenkinsfile | `fix/jenkinsfile-bat-redirection` -> `develop` | Merged | [PR #70](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/70) |
| **PR #71** | Remove unconfigured tools block in Jenkinsfile | `fix/jenkinsfile-remove-tools-block` -> `develop` | Merged | [PR #71](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/71) |
| **PR #72** | Add Docker and Chrome to PATH in Jenkinsfile and use mvnw.cmd | `fix/jenkinsfile-path-and-maven-wrapper` -> `develop` | Merged | [PR #72](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/72) |
| **PR #73** | Fix dynamic environment variable overrides in Jenkinsfile | `fix/jenkinsfile-dynamic-env-vars` -> `develop` | Merged | [PR #73](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/73) |
| **PR #74** | Use @NonCPS methods for regex parsing in Jenkinsfile | `fix/jenkinsfile-noncps-parsing` -> `develop` | Merged | [PR #74](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/74) |
| **PR #75** | Fix form staleness in E2E dashboard search | `fix/e2e-dashboard-search-staleness` -> `develop` | Merged | [PR #75](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/75) |

---

## 3. Requirements Mapping Table

| Requirement ID | Requirement Description | Implementation Component | Test Verification | Verification Result |
|---|---|---|---|---|
| **REQ-01** | Admin registers staff accounts as `ENGINEER` or `MANAGER`; `ADMIN` strictly forbidden | `AdminUserController.registerUser()`, `UserCreateRequest` validation | `AdminUserControllerSecurityTest`, `AdminUserManagementJourneyTest` | **PASSED** (Admin role rejected on input; Engineer/Manager created) |
| **REQ-02** | Extended User profile: `fullName`, unique `email`, unique `username` (3-30 chars, valid regex), `role`, `jobStatus`, `createdAt`, `lastUpdated`, `createdBy` | `User.java` entity, `JobStatus.java` enum, `UserRepository.java` | `UserServiceTest`, `AdminUserControllerSecurityTest` | **PASSED** (Field validation, audit timestamps, uniqueness enforced) |
| **REQ-03** | Status controls login: non-active accounts (`ON_LEAVE`, `INACTIVE`) cannot log in (`UserDetails.enabled = false`) with error message | `CustomUserDetailsService.java`, `SecurityConfig.java` (`CustomAuthenticationFailureHandler`) | `LoginSecurityTest`, `AdminUserManagementJourneyTest` | **PASSED** (Login fails with "Account is inactive or on leave") |
| **REQ-04** | Status changes or deletion invalidate active sessions immediately | `UserService.invalidateUserSessions()`, `SessionRegistry` integration | `UserServiceTest`, MockMvc integration | **PASSED** (Active sessions destroyed via SessionRegistry) |
| **REQ-05** | Server-side self-protection guard: admin cannot delete, deactivate, or demote self | `UserService.java` validation checks (`currentUser.getUsername()`) | `UserServiceTest.cannotDeactivateSelf()`, `cannotDeleteSelf()` | **PASSED** (Exceptions thrown and surfaced in UI) |
| **REQ-06** | Last active admin protection: system prevents removing/deactivating last active admin | `UserService.countActiveAdmins()`, `UserService.java` guards | `UserServiceTest.cannotDemoteOrDeactivateLastAdmin()` | **PASSED** (Last admin protected) |
| **REQ-07** | Admin accounts read-only in user management screen | `AdminUserController.java`, `users/index.html` Thymeleaf logic | `AdminUserControllerSecurityTest`, `AdminUsersPage.java` | **PASSED** (Admin accounts display "Protected" badge; edit/delete hidden) |
| **REQ-08** | Task integrity preserved on user deletion (no cascades, audit text preserved) | `UserService.deleteUser()`, `Task.java` audit trail retention | `UserServiceTest.taskAuditPreservedOnUserDelete()` | **PASSED** (Foreign keys decoupled, audit remarks preserved) |
| **REQ-09** | UI with Bootstrap modal delete confirmation, CSRF on all POST mutating actions, `data-testid` on all controls | `templates/admin/users/*.html`, Bootstrap 5 modals | `AdminUserManagementJourneyTest` (Selenium E2E) | **PASSED** (Modal confirmed, CSRF validated, all test IDs present) |
| **REQ-10** | Persistent database across container restarts (`jdbc:h2:file:./data/dashboard`), volume mount `/app/data` | `application.properties`, `Dockerfile.runtime`, `Jenkinsfile` | Automated Phase 4 Docker persistence test | **PASSED** (Data survived container deletion and recreation) |

---

## 4. Access-Control Matrix

| Endpoint / Action | HTTP Method | Anonymous | ENGINEER | MANAGER | ADMIN | Verification Method |
|---|---|---|---|---|---|---|
| `/admin/users` | GET | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 200 OK | `AdminUserControllerSecurityTest` |
| `/admin/users/new` | GET | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 200 OK | `AdminUserControllerSecurityTest` |
| `/admin/users` | POST | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 302 Redirect (`/admin/users`) | `AdminUserControllerSecurityTest` |
| `/admin/users/{id}/edit` | GET | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 200 OK | `AdminUserControllerSecurityTest` |
| `/admin/users/{id}` | POST | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 302 Redirect (`/admin/users`) | `AdminUserControllerSecurityTest` |
| `/admin/users/{id}/status` | POST | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 302 Redirect (`/admin/users`) | `AdminUserControllerSecurityTest` |
| `/admin/users/{id}/reset-password` | POST | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 302 Redirect (`/admin/users`) | `AdminUserControllerSecurityTest` |
| `/admin/users/{id}/delete` | POST | 302 (Redirect -> `/login`) | 403 Forbidden | 403 Forbidden | 302 Redirect (`/admin/users`) | `AdminUserControllerSecurityTest` |

---

## 5. Server-Side Guard Rules & Validation

1. **Self-Action Prevention**:
   - Deleting self: Throws `IllegalStateException("Administrators cannot delete their own account.")`.
   - Modifying own status to `ON_LEAVE` or `INACTIVE`: Throws `IllegalStateException("Administrators cannot deactivate or change the status of their own account.")`.
   - Demoting self: Admin role cannot be modified.

2. **Last Active Administrator Protection**:
   - An active administrator cannot be deactivated or removed if `countActiveAdmins() <= 1`.

3. **Role Boundary Enforcement**:
   - The registration DTO `UserCreateRequest` and update DTO `UserEditRequest` validate that `role` belongs strictly to `{ENGINEER, MANAGER}`. Attempting to assign `ADMIN` role via craft POST requests is rejected with HTTP 400.

4. **Status-Gated Login**:
   - `CustomUserDetailsService` maps `jobStatus == JobStatus.ACTIVE` to `UserDetails.isEnabled()`.
   - Non-active accounts trigger Spring Security `DisabledException`, handled by `CustomAuthenticationFailureHandler` to display:
     `"Account is inactive or on leave. Please contact your system administrator."`

5. **Session Termination**:
   - When an account is deactivated (`jobStatus != ACTIVE`) or deleted, `UserService.invalidateUserSessions(username)` iterates over `SessionRegistry.getAllPrincipals()`, finds matching principals, and calls `expireNow()` on all active sessions.

---

## 6. Docker Persistence Proof

The automated proof script (`scratch/prove_persistence.ps1`) executed end-to-end verification of database persistence across container destruction and recreation:

```text
=================================================================
PHASE 4 DOCKER PERSISTENCE PROOF
Date: 2026-10-16 11:11:11
=================================================================

[STEP 1] Building Docker runtime image test-persist:latest...
#1 DONE 0.0s
#2 DONE 0.1s
#3 resolve docker.io/library/eclipse-temurin:17-jre
#4 DONE 0.0s
#5 RUN groupadd -r appgroup && useradd -r -g appgroup -d /app -s /sbin/nologin appuser && mkdir -p /app/data && chown -R appuser:appgroup /app
#6 WORKDIR /app
#7 COPY --chown=appuser:appgroup target/dashboard.war /app/app.war
#8 RUN chown -R appuser:appgroup /app
#9 naming to docker.io/library/test-persist:latest done

[STEP 2] Running initial container cpd-persist-test with volume construction-dashboard-data...
Command: docker run -d --name cpd-persist-test -p 8089:8080 -v construction-dashboard-data:/app/data -e SPRING_PROFILES_ACTIVE=dev test-persist:latest
Container ID: 4cfd6b00cb0f947fcb1c97236054f9af28a7cc912f72142cbbca50b1a57b7027

Waiting for container to become healthy on http://localhost:8089/health...
Health check poll 5: UP

[STEP 3] Authenticating as admin and registering user 'persist_eng'...
Retrieved login CSRF: -jyVxu_-...
Admin login response HTTP status: HTTP/1.1 302 
Retrieved /admin/users/new CSRF: cI0UIviw...
User registration POST response: HTTP/1.1 302 
VERIFIED: 'persist_eng' found in admin users table!
Pre-destruction login attempt for 'persist_eng' status: HTTP/1.1 302 

[STEP 4] Destroying the container: docker rm -f cpd-persist-test...
Removed container: cpd-persist-test

[STEP 5] Starting FRESH container cpd-persist-test-fresh with SAME volume...
Command: docker run -d --name cpd-persist-test-fresh -p 8089:8080 -v construction-dashboard-data:/app/data -e SPRING_PROFILES_ACTIVE=dev test-persist:latest
Fresh Container ID: bd81592e7263112d05e0ed7a3fbb3abdb675f3b603b05ff142361c633a0882a8

Waiting for fresh container to become healthy...
Fresh container health check poll 5: UP

[STEP 6] Authenticating as 'persist_eng' on FRESH container...
Fresh container login response: HTTP/1.1 302 
VERIFIED: 'persist_eng' successfully authenticated and accessed /tasks on the brand new container!

[STEP 7] Cleaning up test container: docker rm -f cpd-persist-test-fresh...
SUCCESS: Docker persistence verified completely across container destruction and recreation!
=================================================================
```

---

## 7. Test Suite Summaries

### Unit and Integration Tests
- **Total Tests Executed**: 104
- **Passed**: 104 (100%)
- **Failures / Errors**: 0
- **Surefire Reports**: `target/surefire-reports/*.xml`

### MockMvc Security Suite
- **Endpoint Tests**: 12
- **Passed**: 12 (100%)
- Covers all role matrix permutations and CSRF verification on mutating POST endpoints.

### Selenium E2E Test Suite (Headless Quality Gate)
- **Total Journeys**: 12
- **Passed**: 12
- **Errors / Failures**: 0
- **Highlights**:
  - `AdminUserManagementJourneyTest`: Admin logs in, registers a new engineer, verifies listing in staff table, updates job status to `ON_LEAVE`, verifies login rejection for inactive user, resets password, and performs modal delete.
  - `SearchDashboardJourneyTest`: Search and multi-criteria filters verified with form staleness synchronization.

---

## 8. Jenkins CI/CD Pipeline Build Verification

The pipeline build was executed and verified via the Jenkins REST API with complete parameterization:

- **Jenkins URL**: `http://localhost:8080/job/construction-dashboard-pipeline/11/`
- **Build Status**: **SUCCESS**
- **Duration**: 200.2 seconds
- **Parameters**:
  - `REGISTRY_MODE`: `local`
  - `DEPLOY_TARGET`: `docker`
  - `HOST_PORT`: `8082`
  - `CONTAINER_NAME`: `construction-dashboard`
  - `RUN_TESTS`: `true`
  - `RUN_SELENIUM`: `true`

### Pipeline Stages Summary

| Stage Name | Execution Details | Status |
|---|---|---|
| **Checkout** | Cloned `develop` branch, extracted `APP_VERSION` (3.3.4) and `SHORT_SHA` (9f8a79d) | **SUCCESS** |
| **Build** | Compiled application sources with `./mvnw.cmd -B clean compile` | **SUCCESS** |
| **Unit Tests** | Ran 104 unit and integration tests; recorded surefire test results | **SUCCESS** |
| **Package** | Packaged WAR artifact `target/dashboard.war` | **SUCCESS** |
| **Selenium Tests** | Headless Chrome E2E quality gate; all 12 journeys passed | **SUCCESS** |
| **Docker Build** | Built image `localhost:5000/construction-dashboard:3.3.4-b11-9f8a79d` using `Dockerfile.runtime` | **SUCCESS** |
| **Docker Push** | Pushed versioned tag and `:latest` to local registry | **SUCCESS** |
| **Deploy Container** | Stopped existing container, started new container with volume mount `construction-dashboard-data:/app/data` on port 8082 | **SUCCESS** |
| **Verify Container** | Polled container health status until `healthy`; verified `/health` returned UP; verified UI version text `3.3.4-b11-9f8a79d` | **SUCCESS** |
| **Promote** | Tagged and pushed `localhost:5000/construction-dashboard:stable` | **SUCCESS** |

---

## 9. Live Application Verification

- **Base URL**: `http://localhost:8082/`
- **Health Endpoint**: `http://localhost:8082/health` -> `{"status":"UP"}`
- **Login Page UI**: Displays `Version: 3.3.4-b11-9f8a79d - Final Release Demo`
- **Container Name**: `construction-dashboard`
- **Mounted Volume**: `construction-dashboard-data:/app/data`

---

## 10. Troubleshooting and Defect Resolution Log

During the implementation and CI/CD verification process, several environment and pipeline issues were identified, diagnosed, and resolved:

1. **Windows Command Shell Syntax (`2>NUL`)**:
   - *Problem*: Inline Groovy string interpolation in `Jenkinsfile` caused parsing errors on Windows batch redirection.
   - *Fix*: Standardized batch commands and escaped redirection syntax ([PR #70](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/70)).

2. **Jenkins Tool Resolution (`tools` block)**:
   - *Problem*: The Jenkins controller had no global tool definitions named `JDK17` or `Maven3`, failing builds during pipeline setup.
   - *Fix*: Removed declarative `tools` block, leveraging the installed Java 21 runtime and project Maven wrapper `mvnw.cmd` ([PR #71](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/71)).

3. **Agent PATH and Executable Resolution**:
   - *Problem*: Docker Desktop and Chrome binaries were in user-specific locations not included in the Jenkins service default PATH.
   - *Fix*: Configured pipeline environment PATH and created a wrapper routing function `runCmd()` to invoke `.\mvnw.cmd` on Windows agents ([PR #72](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/72)).

4. **Jenkins CPS Serialization (`NotSerializableException`)**:
   - *Problem*: In Jenkins Declarative Pipelines, `java.util.regex.Matcher` cannot be serialized into CPS execution checkpoints, causing `NotSerializableException` when extracting version and git commit hash in the script block.
   - *Fix*: Extracted regex parsing logic into `@NonCPS` annotated helper methods at the end of `Jenkinsfile` ([PR #74](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/74)).

5. **Selenium Navigation Race Condition in E2E Suite**:
   - *Problem*: Rapid consecutive form submissions in `SearchDashboardJourneyTest` occasionally caused `TimeoutException` waiting for URL change because the second submit click was executed while the previous page was still finishing navigation.
   - *Fix*: Updated `DashboardPage.java` to wait for form DOM staleness (`wait.until(ExpectedConditions.stalenessOf(form))`) before polling URL, ensuring reliable test synchronization ([PR #75](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/75)).
