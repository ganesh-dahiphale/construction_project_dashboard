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

## 📝 Task Updates & Project Administration

- **Progress Updates (`/tasks/{id}/edit`)**: Update task execution status, percentage completion (0-100%), and progress notes. Automatic business rule: setting status to `COMPLETED` automatically forces `percentComplete` to 100%. Tracks `lastUpdated` and `updatedBy`.
- **Project Site Management (`/admin/projects`)**: Create, inspect, and update construction site definitions and timelines.
- **Robust Error Handling**: Dedicated, user-friendly error views for 400 Bad Request, 403 Forbidden, 404 Not Found, and 500 Internal Error.

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

## 🗓️ Week 6 — MVP Baseline: Executive Dashboard, Status Drill-Down & Alert View

**Delivered in:** `feature/7-searchable-dashboard` and `feature/8-drilldown-alerts` (parallel development, conflict resolved, merged into `develop`).  
**Closes:** Issue [#7 — Searchable dashboard with summary indicators](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/7) and Issue [#8 — Status drill-down and alert/exception view](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/8).

### Complete MVP Web Application Routes

| Method | URL | Description |
|---|---|---|
| `GET` | `/` | Welcome and home page |
| `GET` | `/dashboard` | Executive Dashboard with KPI cards and multi-criteria filter form |
| `GET` | `/tasks` | Full list of all construction tasks with status badges and progress |
| `GET` | `/tasks/new` | Task / site event entry form with Bean Validation |
| `POST` | `/tasks` | Submit and validate new task; redirect with flash feedback |
| `GET` | `/status` | Status drill-down category overview with live task counts |
| `GET` | `/status/{status}` | Filtered tasks by status (`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `BLOCKED`, `delayed`) |
| `GET` | `/alerts` | High-priority exception dashboard for overdue and blocked tasks |
| `GET` | `/health` | Application health and synthetic monitoring endpoint (JSON) |
| `GET` | `/h2-console` | In-memory H2 database console (local dev profile) |

### How to Run the Application Locally

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

Access the application in your browser:
- **Dashboard**: http://localhost:8080/dashboard
- **Task List**: http://localhost:8080/tasks
- **New Task Form**: http://localhost:8080/tasks/new
- **Status Drill-Down**: http://localhost:8080/status
- **Alert View**: http://localhost:8080/alerts
- **Health Check**: http://localhost:8080/health

