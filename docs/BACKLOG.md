# Product Backlog & DevOps Roadmap (B-01 – B-14)

This document tracks the complete product backlog and delivery roadmap for the **Construction Progress Dashboard**, mapped from the Week 2 architecture specification to active GitHub issues and deliverables.

---

## 📊 Backlog Items Summary Table

| ID | Category | Title | Target Week | Status | GitHub Issue |
|---|---|---|---|---|---|
| **B-01** | Architecture / DevOps | Initialize application skeleton, CI/CD foundation & branch policies | Week 4 | **Done** | [#1](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/1) |
| **B-02** | Feature | Task / Progress Event Entry Form & Domain Model for Site Engineers | Week 5 | **Done** | [#2](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/2), [#5](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/5) |
| **B-03** | Feature | Searchable Executive Dashboard, Status Drill-Down & Alert View | Week 6 | **Done** | [#3](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/3), [#7](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/7), [#8](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/8) |
| **B-04** | Feature | Task Progress Event Update, Status Transitions & Completion Workflow | Week 7 | **To Do** | [#13](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/13) |
| **B-05** | Feature / Security | User Authentication, Role-Based Access Control (Site Engineer vs Manager) | Week 7 | **To Do** | [#14](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/14) |
| **B-06** | DevOps / CI | Automated CI Pipeline (Build, Test, Quality Gates) with GitHub Actions | Week 7 | **To Do** | [#15](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/15) |
| **B-07** | DevOps / SAST | Static Code Analysis & SAST Security Scanning (SonarQube / SpotBugs) | Week 8 | **To Do** | [#16](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/16) |
| **B-08** | DevOps / Container | Multi-Stage Docker Containerization & Tomcat 10 Base Image | Week 9 | **To Do** | [#17](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/17) |
| **B-09** | DevOps / Container | Multi-Container Orchestration with Docker Compose & MySQL 8 | Week 10 | **To Do** | [#18](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/18) |
| **B-10** | DevOps / IaC | Automated Infrastructure Provisioning (Terraform / Ansible) | Week 11 | **To Do** | [#19](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/19) |
| **B-11** | DevOps / CD | Continuous Delivery / Deployment (CD) Pipeline to Staging Environment | Week 11 | **To Do** | [#20](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/20) |
| **B-12** | DevOps / Config | Configuration Management & Secret Orchestration (Vault / Ansible) | Week 12 | **To Do** | [#21](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/21) |
| **B-13** | DevOps / Monitoring | Application Observability, Prometheus Metrics & Micrometer | Week 13 | **To Do** | [#22](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/22) |
| **B-14** | DevOps / Release | Security Hardening, Dependency-Check, Trivy & Production Release | Week 14-15 | **To Do** | [#23](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/23) |

---

## 📈 Status Breakdown

- **Completed Items (MVP Baseline v0.2.0)**: 3 / 14 (21.4%)
  - **B-01**: Application Skeleton & Git Infrastructure
  - **B-02**: Task Entry Form & Task Listing
  - **B-03**: Executive Dashboard, Status Drill-Down & Alert View
- **Upcoming Items (Phase 2 & DevOps Pipeline)**: 11 / 14 (78.6%)
  - Application Features & Auth: B-04, B-05
  - CI/CD & Automation: B-06, B-07, B-11
  - Containerization & Multi-Container: B-08, B-09
  - Infrastructure as Code & Config: B-10, B-12
  - Observability & Security Hardening: B-13, B-14
