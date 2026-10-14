# DevOps Viva & Oral Examination Comprehensive Preparation Guide 🎓

This preparation guide contains **50 likely oral examination (viva voce) questions and concise, authoritative answers** tailored specifically to the **Construction Progress Dashboard** project. The questions span Agile practices, Git workflows, Maven tooling, Jenkins declarative pipelines, Selenium quality gates, Docker containerization, Ansible configuration management, idempotency, automated rollback, system security, and architectural design trade-offs ("Why X over Y").

---

## 📋 Table of Topics

1. [Agile Planning & Requirements Management (Q1 – Q4)](#1-agile-planning--requirements-management)
2. [Git Version Control & Branching Workflows (Q5 – Q9)](#2-git-version-control--branching-workflows)
3. [Maven & Build Automation (Q10 – Q13)](#3-maven--build-automation)
4. [Continuous Integration with Jenkins (Q14 – Q18)](#4-continuous-integration-with-jenkins)
5. [Automated Testing & Selenium Quality Gates (Q19 – Q23)](#5-automated-testing--selenium-quality-gates)
6. [Containerization with Docker (Q24 – Q28)](#6-containerization-with-docker)
7. [Configuration Management with Ansible (Q29 – Q33)](#7-configuration-management-with-ansible)
8. [Idempotency & Configuration Drift (Q34 – Q37)](#8-idempotency--configuration-drift)
9. [Automated Rollback & System Resilience (Q38 – Q41)](#9-automated-rollback--system-resilience)
10. [Application, Container & Host Security (Q42 – Q45)](#10-application-container--host-security)
11. [Design Choices & Trade-off Justifications ("Why X over Y") (Q46 – Q48)](#11-design-choices--trade-off-justifications)
12. [System Limitations & Future Enhancements (Q49 – Q50)](#12-system-limitations--future-enhancements)

---

## 1. Agile Planning & Requirements Management

#### Q1: How did you apply Agile methodology in this project?
> **Answer**: We organized development into weekly iterative sprints (Weeks 4–15). Requirements were captured as user stories in a Product Backlog (`docs/BACKLOG.md`) with explicit Acceptance Criteria, mapped to GitHub Issues, and developed on isolated feature branches before merging via peer-reviewed Pull Requests.

#### Q2: What was the definition of "Done" (DoD) for a feature?
> **Answer**: A feature was "Done" when: (1) code adhered to standards, (2) unit and integration tests passed with zero failures, (3) documentation was updated, (4) changes were merged into `develop` via PR, and (5) the CI pipeline successfully compiled and verified the artifact.

#### Q3: How did you track progress and prevent scope creep?
> **Answer**: We maintained prioritized issues with GitHub labels (`devops`, `enhancement`, `bug`). In Week 7, we frozen our MVP baseline at `v1.0.0` (Sprint 2) before proceeding to automation, containerization, and infrastructure in Weeks 8–15.

#### Q4: How did user feedback from site engineers shape the product?
> **Answer**: Early feedback highlighted that field engineers could not reliably calculate aggregate project percentages on site. We implemented an automated domain rule: marking a task status as `COMPLETED` automatically locks `percentComplete` to 100% and computes overdue status dynamically based on system date.

---

## 2. Git Version Control & Branching Workflows

#### Q5: Describe your branching model and why you chose Gitflow over trunk-based development.
> **Answer**: We adopted Gitflow with two perpetual branches: `main` (stable production releases) and `develop` (integration baseline). Ephemeral branches (`feature/*`, `chore/*`, `docs/*`) branched from `develop`. Gitflow provided strict isolation for parallel feature teams and protected production while students learned release promotion.

#### Q6: How did you resolve the merge conflict between Feature A and Feature B in Week 6?
> **Answer**: Features #7 (Dashboard) and #8 (Alerts) branched concurrently from commit `c90f7c7`. Both modified `navbar.html` and `README.md`. When Feature B was merged after Feature A, Git flagged conflicting blocks. We resolved it manually by retaining both navigation links, testing locally, and committing the merge resolution.

#### Q7: Why are all pull requests squash-merged or non-fast-forward merged into `develop`?
> **Answer**: We used merge commits (`--merge`) to preserve the topological audit trail and exact branching history of feature development, ensuring that git history reflects which issue each commit originated from.

#### Q8: What commit message convention did you follow?
> **Answer**: We enforced Conventional Commits (`feat(...)`, `fix(...)`, `ci(...)`, `docs(...)`, `chore(...)`) with mandatory issue cross-references ending in `(#N)`. This enabled automated changelog generation and automatic closing of GitHub issues.

#### Q9: Why is force-pushing (`git push --force`) strictly prohibited?
> **Answer**: Force-pushing overwrites remote commit history, which can orphan colleagues' commits, corrupt shared integration branches (`develop`, `main`), and destroy immutable audit records required for compliance and grading.

---

## 3. Maven & Build Automation

#### Q10: What is the purpose of the Maven Wrapper (`./mvnw`)?
> **Answer**: The Maven Wrapper guarantees deterministic builds across diverse developer workstations and CI servers by automatically downloading and using the exact Maven version declared in `.mvn/wrapper/maven-wrapper.properties` without requiring global system installation.

#### Q11: How do Maven profiles separate unit testing from end-to-end testing?
> **Answer**: The default profile executes fast JUnit 5 unit and MockMvc integration tests during `mvn test`. The slow, browser-dependent Selenium suite is sequestered inside a `-Pselenium` profile, ensuring daily development builds remain fast while CI gates can selectively activate full E2E journeys.

#### Q12: Why is the application packaged as a WAR rather than an executable JAR?
> **Answer**: Packaging as a WAR (`dashboard.war`) supported dual deployment models: standalone containerized execution via Spring Boot's embedded Tomcat engine, as well as deployment into external enterprise servlet containers (Apache Tomcat 10) evaluated in Week 8.

#### Q13: What does `mvn clean compile` do under the hood?
> **Answer**: `clean` deletes the `target/` directory to eliminate stale `.class` files. `compile` invokes `maven-compiler-plugin` to parse Java source files in `src/main/java` and compile them into bytecode in `target/classes`.

---

## 4. Continuous Integration with Jenkins

#### Q14: Explain the difference between Declarative and Scripted Jenkins pipelines.
> **Answer**: Declarative pipelines use a structured, opinionated syntax (`pipeline { stages { stage { steps { ... } } } }`) with built-in validation, directives (`options`, `parameters`, `post`), and cleaner error handling. Scripted pipelines rely on Groovy script execution (`node { ... }`), offering raw programming flexibility at the expense of readability and maintainability. We chose Declarative.

#### Q15: What stages exist in your Jenkins pipeline?
> **Answer**: 10 stages: (1) Checkout, (2) Compile, (3) Unit Tests, (4) Package WAR, (5) Selenium Quality Gate, (6) Docker Build, (7) Docker Push, (8) Ansible Provision & Deploy, (9) Verify Node, and (10) Promote to `:stable`.

#### Q16: How does the pipeline generate immutable, traceable Docker image tags?
> **Answer**: The checkout stage extracts the Maven POM version, Jenkins build number, and shortened Git commit SHA to construct tags formatted as `<version>-b<build>-<sha>` (e.g., `0.0.1-SNAPSHOT-b3-73fa1bc`). This links every container image directly to its source commit.

#### Q17: What post-build actions are configured in the Jenkinsfile?
> **Answer**: The `post { always { ... } failure { ... } }` blocks archive `deploy-info.txt`, Ansible execution logs, and JUnit test results (`junit '**/surefire-reports/*.xml'`), while capturing failure notifications if any stage aborts.

#### Q18: What is the disadvantage of SCM polling versus GitHub Webhooks?
> **Answer**: SCM polling introduces a 1–2 minute delay between commit and build execution, while periodically polling GitHub servers unnecessarily. Webhooks provide instantaneous, event-driven build triggers upon `push` events.

---

## 5. Automated Testing & Selenium Quality Gates

#### Q19: What constitutes a "Quality Gate" in CI/CD?
> **Answer**: A Quality Gate is an automated threshold that software must satisfy before proceeding to downstream stages. In our pipeline, Stage 5 executes automated browser tests; if any assertion or UI interaction fails, the build halts, preventing flawed containers from being built, pushed, or deployed.

#### Q20: What critical user journeys do your Selenium tests validate?
> **Answer**: Three core journeys: (1) Authentication & Role-Based Access (validating login, navbar display, and role restrictions), (2) Task Creation & Field Validation (adding tasks, checking automatic completion logic), and (3) Executive Dashboard & Status Filters (searching tasks and verifying alert views).

#### Q21: Why do Selenium tests run in headless mode in CI?
> **Answer**: Headless mode (`--headless=new`) runs the browser engine in memory without spawning a graphical desktop window. CI runners and server daemons operate in headless environments without physical monitors or X11 displays.

#### Q22: How does the Page Object Model (POM) improve test maintainability?
> **Answer**: POM abstracts web page UI elements and interactions into dedicated classes (e.g., `LoginPage`, `DashboardPage`, `TaskFormPage`). If an HTML selector or button changes, modifications are confined to the page object class without breaking test scripts.

#### Q23: How did you demonstrate that your quality gate blocks broken releases?
> **Answer**: In Week 10, we intentionally injected a defect into the UI that broke task creation assertions. Jenkins Build #2 executed Stage 5, caught the assertion failure, aborted the build with an unstable status, and skipped Docker Build and Deploy stages, preventing defective deployment.

---

## 6. Containerization with Docker

#### Q24: What is a multi-stage Docker build and why did you use it?
> **Answer**: Multi-stage builds use separate `FROM` instructions in a single Dockerfile. Stage 1 (builder) uses a full Maven/JDK image to compile code. Stage 2 (runtime) copies only the final `.war` into a lightweight JRE base image. This shrinks final image size, eliminates build tools from production, and reduces attack surface.

#### Q25: Why does the Docker container run as a non-root user?
> **Answer**: Running as root inside a container creates severe security risks: if an attacker achieves container escape through a kernel vulnerability, they gain root privileges on the host OS. We created `appuser` (UID 10001) with least privilege.

#### Q26: What is the difference between Docker tags `:latest` and `:stable`?
> **Answer**: `:latest` points to the most recent container successfully built and pushed from the CI pipeline. `:stable` is updated only after the container has been deployed to the target node, passed synthetic `/health` checks, and passed live verification.

#### Q27: How does Docker Layer Caching accelerate CI build times?
> **Answer**: Docker caches image layers. In our Dockerfile, copying dependencies (`pom.xml`) occurs before copying source code (`src/`). If dependencies do not change, Docker reuses cached Maven dependency layers, cutting compilation times from minutes to seconds.

#### Q28: How does the container pass health information to the host?
> **Answer**: The application exposes an HTTP probe `/health` returning `{"status":"UP"}`. Dockerfile defines `HEALTHCHECK --interval=30s --timeout=5s CMD curl -f http://localhost:8080/health || exit 1`.

---

## 7. Configuration Management with Ansible

#### Q29: What is Configuration Management and why use Ansible over shell scripts?
> **Answer**: Configuration Management automates server state definition and maintenance. Unlike imperative shell scripts that blindly execute commands regardless of state, Ansible uses declarative YAML to enforce desired system state idempotently across fleets of servers.

#### Q30: Explain Ansible's agentless architecture.
> **Answer**: Ansible requires no background daemon or custom agent software on target nodes. It connects over standard OpenSSH using Python and SSH keys, executing tasks and removing temporary modules upon completion. This minimizes resource overhead and operational maintenance.

#### Q31: What Ansible roles did you create and what are their responsibilities?
> **Answer**: Six modular roles: (1) `common` (packages, timezone), (2) `docker` (official Docker engine, daemon config), (3) `app_user` (system user `appdeploy`), (4) `app_dirs` (`/opt/construction-dashboard`), (5) `firewall` (UFW rules), (6) `app_config` (environment file and systemd unit), and (7) `deploy` (container pull, health check, rollback).

#### Q32: Why did you manage the container via systemd instead of plain `docker run -d`?
> **Answer**: systemd provides operating system level process supervision (`Restart=always`), automatic startup on node reboot, centralized logging via `journalctl`, and reliable lifecycle hooks (`systemctl start/stop/restart`) integrated with host monitoring.

#### Q33: How does Ansible handle task variables and environment configuration?
> **Answer**: Host and group variables are organized in `group_vars/app_servers.yml`, allowing environmental separation (ports, image names, memory limits) from playbook logic without hardcoding configurations inside roles.

---

## 8. Idempotency & Configuration Drift

#### Q34: What is idempotency and why is it essential in DevOps?
> **Answer**: Idempotency is the property where executing an operation multiple times produces the exact same outcome without unintended side effects. In DevOps, running an Ansible playbook against an already-configured server results in `changed=0`, ensuring infrastructure stability without disruptive re-installations.

#### Q35: How did you prove that your Ansible automation is idempotent?
> **Answer**: We ran `ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml` twice consecutively. The first run applied changes (`changed=20`). The second run reported `ok=37 changed=0 failed=0`, confirming zero unnecessary modifications.

#### Q36: What is configuration drift and how does Ansible detect and remediate it?
> **Answer**: Drift occurs when unauthorized manual changes or crashes alter server configuration away from the intended baseline. In Week 14, we deliberately stopped the service and modified configuration files; re-running Ansible detected the drift, restored the files, restarted the container, and converged the node back to baseline.

#### Q37: What is the purpose of Ansible check mode (`--check --diff`)?
> **Answer**: Check mode performs a dry run against target nodes, inspecting current state against desired state and outputting unified diffs of proposed changes without committing any modifications to disk.

---

## 9. Automated Rollback & System Resilience

#### Q38: How does your deployment role implement automated rollback?
> **Answer**: The `deploy` role wraps deployment tasks inside an Ansible `block` / `rescue` structure. After deploying a new container tag, it polls `/health` for up to 25 retries. If health checks fail, control immediately transfers to `rescue`, which restores the `previous.txt` release tag, restarts systemd, verifies health, and writes a rollback audit entry in `history.log`.

#### Q39: What files track release state on the target node?
> **Answer**: Under `/opt/construction-dashboard/releases/`:
> - `current.txt`: Currently active image tag.
> - `previous.txt`: Previously verified stable image tag.
> - `history.log`: Append-only chronological audit log of all deployments and rollbacks.

#### Q40: What happens if a node unexpectedly reboots?
> **Answer**: The systemd service `construction-dashboard.service` has `WantedBy=multi-user.target` enabled on boot. Upon OS reboot, systemd automatically launches Docker and restarts the container within ~22 seconds without manual operator intervention.

#### Q41: How do you perform an operator-controlled manual rollback?
> **Answer**: By executing `ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/rollback.yml -e confirm_rollback=yes`. It swaps the tags in `current.txt` and `previous.txt`, redeploys the prior container, and verifies health.

---

## 10. Application, Container & Host Security

#### Q42: How does Spring Security protect the application against CSRF attacks?
> **Answer**: Spring Security enables the Synchronizer Token Pattern. For every mutating HTTP POST request, Thymeleaf injects a hidden `_csrf` token generated per session. Requests lacking a matching token are rejected with HTTP 403 Forbidden.

#### Q43: How are passwords secured in the database?
> **Answer**: Passwords are never stored in plaintext. They are encrypted using the BCrypt strong adaptive hashing algorithm (`BCryptPasswordEncoder` with strength 10), incorporating unique salts to prevent rainbow table attacks.

#### Q44: What host firewall rules are enforced on the target node?
> **Answer**: UFW (Uncomplicated Firewall) enforces a default-deny incoming policy (`default deny incoming`) while explicitly permitting traffic only on port `22/tcp` (OpenSSH management) and port `8082/tcp` (application web traffic).

#### Q45: How did you ensure secrets were not leaked into the Git repository?
> **Answer**: (1) Credentials, SSH keys, and tokens are stored in `.gitignore`. (2) Jenkins uses Jenkins Credentials Store IDs (`cpd-ansible-ssh`, `dockerhub-credentials`) with `withCredentials`. (3) We audited the repo using `git grep -nEi "password|token|secret|BEGIN .*PRIVATE KEY"` to verify zero credential exposure.

---

## 11. Design Choices & Trade-off Justifications

#### Q46: Why did you choose Docker over deploying directly to a VM with Tomcat?
> **Answer**: Docker encapsulates application binaries, Java runtime, system libraries, and configuration into an immutable container artifact. Direct VM deployment risks "it works on my machine" drift due to mismatched JDK updates or conflicting host packages. Containers guarantee environment parity from local laptop to production.

#### Q47: Why did you choose Ansible over Terraform or Puppet?
> **Answer**: Terraform excels at provisioning cloud infrastructure (VPCs, EC2 instances), whereas Ansible excels at host-level configuration management and application deployment. Compared to Puppet or Chef, Ansible is agentless, lightweight, uses human-readable YAML, and requires only SSH.

#### Q48: Why did you choose Selenium WebDriver over Cypress or Playwright?
> **Answer**: Selenium WebDriver has mature, native Java bindings (`selenium-java`) that seamlessly integrate into the Maven build lifecycle and surefire test reports. It allowed writing both unit tests and browser tests in Java within the same repository without introducing Node.js tooling into the CI agent.

---

## 12. System Limitations & Future Enhancements

#### Q49: What is the primary architectural limitation of the current deployment?
> **Answer**: The application runs under the `dev` profile using an in-memory H2 database on a single node. Container recreation clears runtime data, and the single server constitutes a Single Point of Failure (SPOF). Production requires a persistent PostgreSQL database and multi-node clustering.

#### Q50: What would be your first three production enhancements if given another sprint?
> **Answer**:
> 1. **PostgreSQL & Liquibase**: Deploy persistent database with automated schema migrations.
> 2. **Nginx Reverse Proxy with TLS/HTTPS**: Enforce encrypted transport via Let's Encrypt certificates.
> 3. **Prometheus & Grafana Observability**: Instrument Spring Boot Actuator with Micrometer to capture real-time telemetry, JVM health, and error alerts.
