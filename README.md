# Construction Progress Dashboard 🏗️

A collaborative, real-time web application designed for civil construction and infrastructure projects. Site engineers can log daily tasks, progress milestones, and site events, while project managers monitor high-level KPIs, searchable status indicators, progress drill-downs, and critical alerts for overdue tasks.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [MVP Features](#mvp-features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Local Development & Build Instructions](#local-development--build-instructions)
- [Project Directory Structure](#project-directory-structure)
- [Branching Strategy Summary](#branching-strategy-summary)
- [Commit Message Convention](#commit-message-convention)
- [DevOps Roadmap (Weeks 4 - 15)](#devops-roadmap-weeks-4---15)

---

## 📖 Project Overview

On construction sites, lack of immediate visibility into daily site activities leads to project delays, cost overruns, and miscommunication between field engineers and project management. 

The **Construction Progress Dashboard** bridges this gap:
- **Field Engineers**: Rapidly log progress events, materials utilized, and task completions on the ground.
- **Project Managers & Executives**: Access a consolidated web dashboard featuring real-time health checks, overdue task notifications, searchable logs, and milestone drill-downs.

---

## 🚀 MVP Features

1. **Task & Event Logging**: Simple web interface for site engineers to log site events with timestamps, task descriptions, and progress percentages (`/tasks`, `/tasks/new`).
2. **Searchable Executive Dashboard**: Filterable and searchable dashboard displaying active jobs, milestones, and site locations (`/dashboard`).
3. **Summary Indicators & KPI Cards**: Key performance indicators including completed tasks, in-progress activities, blocked items, and delayed activities (`/dashboard`).
4. **Status Drill-Down**: In-depth inspection view for individual site task categories (`/status`, `/status/{status}`).
5. **Overdue Task Alert View**: Automated flagging of tasks that exceed their target completion deadlines or stall without updates (`/alerts`).
6. **System Health Endpoint**: Built-in REST endpoint (`/health`) for health checks and automated CI/CD synthetic monitoring.

---

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

---

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Language** | Java 17 (LTS) | Modern, LTS release of Java |
| **Framework** | Spring Boot 3.3.4 | Core enterprise web application framework |
| **Packaging** | WAR (Web Application Archive) | Tomcat 10 compatible servlet deployment & standalone execution |
| **Build Tool** | Apache Maven 3.9+ | Managed via included Maven Wrapper (`./mvnw`) |
| **UI & Templating** | Thymeleaf + Bootstrap 5 (CDN) | Dynamic server-side rendering with responsive CSS |
| **Persistence** | Spring Data JPA / Hibernate | Object-relational mapping and repository abstraction |
| **Database (Dev)** | H2 In-Memory Database | Fast, zero-configuration embedded database for local dev & testing |
| **Database (Prod)** | MySQL Connector/J | Production-ready JDBC driver for enterprise MySQL instances |
| **Security** | Spring Security | Role-based authentication (Field Engineer vs. Manager) - *Phase 2* |

---

## ⚙️ Prerequisites

Before running the application locally, ensure you have the following installed:
- **Java Development Kit (JDK)**: Version 17 or higher (`java -version`)
- **Git**: Version 2.x+ (`git --version`)
- **Web Browser**: Modern browser (Chrome, Firefox, Edge, Safari)
- *(Optional)* **Apache Tomcat 10+**: Only required if deploying the WAR archive to an external servlet container.

> **Note**: Apache Maven is **not** required to be pre-installed globally; the repository provides the Maven Wrapper (`./mvnw` on Unix / `mvnw.cmd` on Windows).

---

## 💻 Local Development & Build Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd DevOps
```

### 2. Configure Environment Variables (Optional)
The application externalises all core configuration via `src/main/resources/application.properties` with fallback defaults:

| Variable | Default Value | Description |
|---|---|---|
| `PORT` | `8080` | Application HTTP listening port |
| `DB_URL` | `jdbc:h2:mem:constructiondb;...` | JDBC database URL |
| `DB_USER` | `sa` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |
| `H2_CONSOLE_ENABLED` | `true` | Enable/disable `/h2-console` web console |

### 3. Build and Package the Application
Run Maven clean package to compile, run tests, and package the WAR archive:
- **Linux / macOS**:
  ```bash
  ./mvnw clean package
  ```
- **Windows (PowerShell / Command Prompt)**:
  ```powershell
  .\mvnw.cmd clean package
  ```
The build produces `target/dashboard.war`.

### 4. Run the Application
You can run the application directly using the Spring Boot Maven plugin:
- **Linux / macOS**:
  ```bash
  ./mvnw spring-boot:run
  ```
- **Windows**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

Alternatively, run the packaged WAR file directly:
```bash
java -jar target/dashboard.war
```

### 5. Access the Application
- **Home Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Health Check API**: [http://localhost:8080/health](http://localhost:8080/health) *(returns `{"status":"UP"}`)*
- **H2 Web Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) *(JDBC URL: `jdbc:h2:mem:constructiondb`)*

---

## 📁 Project Directory Structure

```text
.
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md        # Standardized bug reporting template
│   │   ├── feature_request.md   # Feature proposal template
│   │   ├── task.md              # Project backlog task template
│   │   └── config.yml           # Issue configuration (blank issues disabled)
│   └── pull_request_template.md # PR submission checklist & verification
├── .mvn/
│   └── wrapper/                 # Maven wrapper binaries and properties
├── docs/
│   └── BRANCHING.md             # Repository branching model & merge policies
├── src/
│   ├── main/
│   │   ├── java/com/example/dashboard/
│   │   │   ├── controller/      # REST & MVC web controllers (HomeController, HealthController)
│   │   │   ├── service/         # Business logic layer (empty skeleton with .gitkeep)
│   │   │   ├── repository/      # Spring Data JPA repositories (empty skeleton with .gitkeep)
│   │   │   ├── model/           # JPA domain entities (empty skeleton with .gitkeep)
│   │   │   └── ConstructionDashboardApplication.java # SpringBootServletInitializer main
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf HTML views (index.html)
│   │       └── application.properties # Externalised application settings
│   └── test/
│       └── java/com/example/dashboard/
│           └── HealthControllerTest.java # Smoke test verifying /health endpoint
├── .gitignore                   # Comprehensive ignore rules
├── CONTRIBUTING.md              # Contribution workflow & guidelines
├── mvnw                         # Maven wrapper shell script (Unix)
├── mvnw.cmd                     # Maven wrapper batch script (Windows)
├── pom.xml                      # Project Object Model & dependencies
└── README.md                    # Project documentation
```

---

## 🌿 Branching Strategy Summary

This project follows a Gitflow-inspired branching strategy:
- **`main`**: Production-ready, stable releases. No direct commits allowed.
- **`develop`**: Integration branch for ongoing sprint deliverables.
- **`feature/<issue>-<description>`**: New functional additions branched from `develop`.
- **`bugfix/<issue>-<description>`**: Non-critical fixes branched from `develop`.
- **`hotfix/<description>`**: Critical production hotfixes branched from `main`.
- **`release/<version>`**: Release preparation branched from `develop` and merged into `main` and `develop`.

For full details, review [docs/BRANCHING.md](docs/BRANCHING.md).

---

## ✍️ Commit Message Convention

We enforce the [Conventional Commits](https://www.conventionalcommits.org/) specification:
```text
<type>(<scope>): <subject in present tense>
```

- **Types**:
  - `feat`: A new user-facing feature
  - `fix`: A bug fix
  - `docs`: Documentation updates only
  - `chore`: Maintenance, build configuration, or dependency updates
  - `test`: Adding or correcting tests
  - `refactor`: Code changes that neither fix bugs nor add features
  - `ci`: CI/CD pipeline changes
- **Example**: `feat(health): add health check endpoint and home page`

---

## 🗺️ DevOps Roadmap (Weeks 4 - 15)

| Week | Milestone / Topic | Key Deliverables & Tooling |
|:---:|---|---|
| **Week 4** | **Git & Repository Initialization** | Application skeleton, WAR packaging, branching policy, issue/PR templates, smoke tests |
| **Week 5** | **Testing Framework & Unit Tests** | JUnit 5, Mockito service testing, JPA repository slice tests |
| **Week 6** | **Continuous Integration (CI) with Jenkins** | Jenkinsfile declarative pipeline, automated checkout, build & test stages |
| **Week 7** | **Static Code Analysis & Quality Gates** | SonarQube / SpotBugs integration, code quality gates, test coverage thresholds |
| **Week 8** | **End-to-End (E2E) Testing with Selenium** | Selenium WebDriver UI automation tests, headless Chrome test execution in CI |
| **Week 9** | **Docker Containerization** | Multi-stage Dockerfile, Tomcat 10 container packaging, lightweight base images |
| **Week 10** | **Multi-Container Environments** | Docker Compose orchestration combining application WAR and dedicated MySQL 8 container |
| **Week 11** | **Continuous Delivery (CD) Pipelines** | Automated artifact archiving, deployment triggers to staging environment |
| **Week 12** | **Configuration Management** | Infrastructure automation using Ansible / Puppet playbooks for server provisioning |
| **Week 13** | **Monitoring, Logging & Observability** | Spring Boot Actuator, Prometheus metrics export, log aggregation |
| **Week 14** | **Security & Vulnerability Scanning** | OWASP Dependency-Check, container security scanning (Trivy), credentials audit |
| **Week 15** | **Production Release & Project Retrospective** | Zero-downtime release deployment, project showcase, and post-mortem report |

---

## 🗓️ Week 5 — Feature 1: Task / Progress Event Entry & Task List

**Delivered in:** `feature/5-task-entry-form` (merged into `develop`).  
**Closes:** Issue [#5 — Task/event entry form and task list](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/5)

### What was built

| Layer | Details |
|---|---|
| **Models** | `Project` (id, name, location, startDate, endDate) and `Task` (id, project, title, status enum, percentComplete 0–100, dueDate, remarks, lastUpdated) with full Bean Validation |
| **Repositories** | `ProjectRepository` and `TaskRepository` (Spring Data JPA) |
| **Service** | `TaskService` — `createTask`, `getAllTasks`, `getTaskById`, `getAllProjects` |
| **Controllers** | `TaskController` — `GET /tasks` (list), `GET /tasks/new` (form), `POST /tasks` (validate → save or redisplay) |
| **Templates** | `tasks/list.html` (table with status badges, progress bars, overdue flag), `tasks/form.html` (validated Bootstrap form), `fragments/navbar.html` (shared nav) |
| **Seed Data** | `DataInitializer` (dev/default profile) seeds 2 projects and 5 tasks (one overdue, one completed, one blocked) |
| **Tests** | `TaskServiceTest` (Mockito, 4 tests) + `TaskControllerTest` (MockMvc, 5 tests) + existing `HealthControllerTest` — **10 / 10 green** |

---

## 🗓️ Week 7 — MVP Release v1.0.0: Authentication, RBAC, Task Updates & Project Management

**Delivered in:** `feature/25-login-roles` and `feature/26-status-update-admin` (parallel development, conflict resolved, merged into `develop`).  
**Closes:** Issue [#25 — Login and role-based access](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/25) and Issue [#26 — Task status update and admin project management](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/26).

### 🎯 MVP Status: **100% Complete (Release v1.0.0)**

### Complete MVP Web Application Routes

| Method | URL | Required Role | Description |
|---|---|---|---|
| `GET` | `/` | Authenticated | Welcome and home page |
| `GET` | `/login` | Public | Custom form login view |
| `POST` | `/login` | Public | Spring Security authentication handler |
| `POST` | `/logout` | Authenticated | Secure session termination |
| `GET` | `/dashboard` | `ADMIN`, `MANAGER` | Executive Dashboard with KPI cards and multi-criteria filter form |
| `GET` | `/tasks` | `ADMIN`, `MANAGER`, `ENGINEER` | Full list of all construction tasks with status badges and edit links |
| `GET` | `/tasks/new` | `ADMIN`, `MANAGER`, `ENGINEER` | Task / site event entry form with Bean Validation |
| `POST` | `/tasks` | `ADMIN`, `MANAGER`, `ENGINEER` | Submit and validate new task; redirect with flash feedback |
| `GET` | `/tasks/{id}/edit` | `ADMIN`, `MANAGER`, `ENGINEER` | Form to update task status, percent completion, and remarks |
| `POST` | `/tasks/{id}/edit` | `ADMIN`, `MANAGER`, `ENGINEER` | Process task update, auto-complete rule, and record auditing user |
| `GET` | `/status` | `ADMIN`, `MANAGER` | Status drill-down category overview with live task counts |
| `GET` | `/status/{status}` | `ADMIN`, `MANAGER` | Filtered tasks by status (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`, `delayed`) |
| `GET` | `/alerts` | `ADMIN`, `MANAGER` | High-priority exception dashboard for overdue and blocked tasks |
| `GET` | `/admin/projects` | `ADMIN` | List all construction project sites |
| `GET` | `/admin/projects/new` | `ADMIN` | Create new construction project site form |
| `POST` | `/admin/projects` | `ADMIN` | Save new construction project |
| `GET` | `/admin/projects/{id}/edit`| `ADMIN` | Edit construction project metadata and dates |
| `POST` | `/admin/projects/{id}/edit`| `ADMIN` | Update construction project details |
| `GET` | `/health` | Public | Application health and synthetic monitoring endpoint (JSON) |
| `GET` | `/h2-console` | Public | In-memory H2 database console (local dev profile) |

### How to Run the Application Locally

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

Access the application in your browser:
- **Login**: http://localhost:8080/login *(Use `admin`/`admin123`, `manager`/`manager123`, or `engineer`/`engineer123`)*
- **Dashboard**: http://localhost:8080/dashboard
- **Task List**: http://localhost:8080/tasks
- **New Task Form**: http://localhost:8080/tasks/new
- **Status Drill-Down**: http://localhost:8080/status
- **Alert View**: http://localhost:8080/alerts
- **Admin Projects**: http://localhost:8080/admin/projects
- **Health Check**: http://localhost:8080/health

---

## 🧪 Running UI Tests (Selenium E2E)

The project includes an automated end-to-end (E2E) testing suite powered by **Selenium 4**, **JUnit 5**, and **Headless Chrome**, automating 5 critical user journeys across authentication, role-based authorization, task creation, status updates, search filters, and exception drill-downs.

### 1. Default Build (Fast Unit & Integration Tests)
By default, standard builds do **not** run slow browser E2E tests, allowing standard CI pipelines to remain fast:
```bash
# Unit & Controller/Service integration tests only (E2E excluded)
./mvnw clean test
./mvnw clean package
```

### 2. Execute Selenium E2E Tests
To run the automated Selenium user journey suite locally, activate the `selenium` Maven profile:
```bash
# Run all 5 critical user journey tests (Headless Chrome)
./mvnw -B -Pselenium test

# Windows PowerShell:
.\mvnw.cmd -B -Pselenium test
```

### 3. Execution Options & System Properties

| Property | Default Value | Description | Example |
|---|---|---|---|
| `-Pselenium` | *(none)* | Maven profile activating E2E tests tagged `@Tag("e2e")` | `./mvnw -B -Pselenium test` |
| `-Dheadless=false` | `true` | Runs Chrome in visible (headed) GUI mode for visual inspection | `./mvnw -B -Pselenium test -Dheadless=false` |
| `-Dbase.url` | *(Embedded Port)* | Targets a pre-deployed server instance (e.g. Tomcat/staging) | `./mvnw -B -Pselenium test -Dbase.url=http://localhost:8080` |
| `-Ddemo.failure=true` | `false` | Executes the deliberate failure verification test | `./mvnw -B -Pselenium test "-Ddemo.failure=true"` |
| `-De2e.manager.password` | `manager123` | Overrides test account passwords | `./mvnw -B -Pselenium test -De2e.manager.password=customPass` |

### 4. HTML Surefire Test Report
Generate and view the HTML Surefire test report:
```bash
# Generate report after running tests
./mvnw -B -Pselenium surefire-report:report-only

# Windows PowerShell:
.\mvnw.cmd -B -Pselenium surefire-report:report-only
```
- Report output location: `target/site/surefire-report.html`

### 5. Failure Screenshot Mechanism
The test framework includes a JUnit 5 `ScreenshotOnFailureExtension` (`AfterTestExecutionCallback` & `TestWatcher`). Whenever any UI test fails:
- An automatic full-page PNG screenshot is captured to `target/screenshots/<ClassName>_<methodName>_<timestamp>.png`.
- The failure log records the exact page URL, document title, and failure cause.
- Permanent evidence files and reports are preserved under `docs/evidence/week9/`.

