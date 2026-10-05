# Changelog

All notable changes to the **Construction Progress Dashboard** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
