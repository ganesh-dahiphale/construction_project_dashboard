# End-to-End Test Plan: Construction Progress Dashboard 🧪

**Document Version**: 1.0.0  
**Target Milestone**: Week 9 — Selenium WebDriver Automation for Critical User Journeys  
**Author**: DevOps & QA Engineering Team  
**Repository**: [ganesh-dahiphale/construction_project_dashboard](https://github.com/ganesh-dahiphale/construction_project_dashboard)  

---

## 1. Objective

The objective of this end-to-end (E2E) testing suite is to provide automated regression testing for the **Construction Progress Dashboard** web application across all core user journeys. Tests run against headless Google Chrome in local and CI/CD environments, ensuring that user authentication, role-based authorization, task lifecycle management, executive metrics, search capabilities, and exception alerts function properly from an end-user perspective.

---

## 2. Scope

### In Scope
- Verification of 5 critical end-to-end user journeys (J1 through J5).
- Cross-role authorization and access control boundary checks (`ADMIN`, `MANAGER`, `ENGINEER`).
- Form validations, CSRF handling, flash messaging, and page navigation flows.
- Automated screenshot-on-failure capture for triage and defect reporting.
- Isolated Maven test execution profile (`selenium`) preventing interference with unit/integration testing pipelines.

### Out of Scope
- Performance, stress, and load testing (scheduled for later release milestones).
- Cross-browser matrix testing (Firefox, Safari) — Google Chrome is the standardized baseline browser.

---

## 3. Environment & Tools

| Component | Technology / Specification | Purpose |
|---|---|---|
| **Language & Runtime** | Java 17 LTS | Standard execution runtime |
| **Framework & Runner** | JUnit 5 (Jupiter) | Test lifecycle, assertions, extensions |
| **Automation Tool** | Selenium WebDriver 4.25+ (`selenium-java`) | Browser automation & DOM interaction |
| **Browser Engine** | Google Chrome (Headless `--headless=new`) | Default execution engine |
| **Driver Management** | Selenium Manager (Built-in) | Automatic ChromeDriver resolution |
| **Build & Execution** | Apache Maven 3.9+ via `./mvnw` | Parameterized execution via `-Pselenium` |
| **Design Pattern** | Page Object Model (POM) | Clean separation of page locators & test logic |
| **Reporting** | Maven Surefire Report Plugin | Standardized HTML test execution reports |

---

## 4. Entry and Exit Criteria

### Entry Criteria
- The application WAR compiles and packages cleanly (`./mvnw clean package`).
- All 58 unit, controller, and security slice tests pass (100% green).
- Google Chrome is installed on the host machine or CI container.
- Test seed dataset is populated (via `DataInitializer` or in-memory H2 database).

### Exit Criteria
- 100% passing rate across all 5 automated critical user journeys.
- Failure screenshot mechanism proven and operational without side effects on clean runs.
- HTML test report generated and accessible in `target/site/surefire-report.html`.
- Standard Maven build (`./mvnw clean package`) remains unaffected by E2E test exclusions.

---

## 5. Test Data Management Approach

- **Configuration File**: `src/test/resources/e2e-testdata.properties` provides default credentials and search terms matching `README.md` documented seed data.
- **Dynamic Overrides**: Credentials and base URLs can be overridden via Java System properties (e.g. `-Dbase.url=http://localhost:8081/construction-dashboard-dev`, `-De2e.engineer.password=...`).
- **Data Isolation**: Generated test tasks utilize timestamp-suffixed titles (e.g., `Task-E2E-1728148900`) to avoid naming collisions across multiple test runs.

---

## 6. Critical User Journeys Matrix (J1 – J5)

| ID | Journey Title | Preconditions | Steps | Test Data | Expected Result | Priority |
|:---:|---|---|---|---|---|:---:|
| **J1** | **Login and Role Access Control** | Dev seed users initialized in database | 1. Open `/login`<br>2. Submit invalid credentials<br>3. Verify error alert<br>4. Login as `engineer`<br>5. Attempt to navigate to `/dashboard` & `/admin/projects`<br>6. Login as `manager` & `admin` | Usernames: `engineer`, `manager`, `admin`<br>Password: `*123`<br>Invalid: `wrongpass` | Invalid login displays error alert; Engineer receives HTTP 403 Forbidden on `/dashboard` and `/admin/projects`; Manager and Admin access appropriate pages | **P0 (Critical)** |
| **J2** | **Task Entry & Form Validation** | Engineer user authenticated | 1. Navigate to `/tasks/new`<br>2. Submit empty form<br>3. Verify validation feedback<br>4. Fill required fields (unique title, project, status, percent, due date)<br>5. Click Save | Title: `E2E Foundation Inspection [timestamp]`<br>Percent: `35`<br>Status: `IN_PROGRESS` | Form redisplays with field validation errors on empty submission; Valid submission redirects to `/tasks` with success flash message | **P0 (Critical)** |
| **J3** | **Task Progress & Status Update** | Existing task present on `/tasks` | 1. Authenticate as `engineer`<br>2. Navigate to `/tasks`<br>3. Click Edit on task<br>4. Change status to `COMPLETED`<br>5. Submit form<br>6. Verify task row in `/tasks` | Status: `COMPLETED`<br>Remarks: `Sign-off completed by QA` | Auto-completion business rule forces progress to 100%; Task status badge updates to `Completed` with 100% bar | **P0 (Critical)** |
| **J4** | **Executive Dashboard & Search Filtering** | Manager user authenticated, tasks exist | 1. Navigate to `/dashboard`<br>2. Inspect KPI summary cards<br>3. Filter by keyword<br>4. Filter by status dropdown<br>5. Reset filters | Keywords: `Foundation`, `NonExistentKeyword`<br>Status: `IN_PROGRESS` | KPI summary cards display positive non-blank counts; Results table filters accurately matching criteria; Total tasks card reflects accurate system state | **P1 (High)** |
| **J5** | **Status Drill-Down & Alert Exceptions** | Overdue task seeded (`dueDate < today`), Manager logged in | 1. Navigate to `/dashboard` and click Delayed indicator (or `/status/delayed`)<br>2. Verify delayed tasks table<br>3. Navigate to `/alerts`<br>4. Verify overdue and blocked task alert tables | Seeded overdue task: `Structural Framing Inspection` | Only overdue tasks shown in `/status/delayed`; `/alerts` page displays overdue warning badges and days overdue calculation | **P1 (High)** |

---

## 7. Defect & Failure Triage

When a test failure occurs:
1. The `ScreenshotOnFailureExtension` captures the full-screen browser viewport and saves a PNG image to `target/screenshots/<Class>_<Method>_<timestamp>.png`.
2. The current browser URL, page title, and DOM state are logged to the standard test output.
3. Screenshots are attached to test reports for rapid defect isolation and remediation.
