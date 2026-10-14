# Tomcat 10.1 Deployment Guide & Continuous Testing Pipeline 🚀

This document describes the local Apache Tomcat 10.1 deployment environment, configuration parameters, management commands, and the automated Jenkins CI/CD continuous testing pipeline with Selenium quality gates.

---

## 1. Local Tomcat 10.1 Environment Overview

- **Tomcat Version**: Apache Tomcat 10.1.34 (Jakarta EE 10 / Servlet 6.0 compatible)
- **Install Path**: `C:\tools\apache-tomcat-10.1.34`
- **HTTP Port**: `8081` (configured in `conf/server.xml` to prevent collision with Jenkins on `8080`)
- **Manager Application**: Enabled at `http://localhost:8081/manager/text` for automated WAR deployments.
- **Manager User Role**: `manager-script` (assigned to deployment service account in `conf/tomcat-users.xml`).

---

## 2. Managing the Tomcat Service

### Starting Tomcat
To start the Apache Tomcat server in the background:
- **Windows (PowerShell / Command Prompt)**:
  ```powershell
  cd C:\tools\apache-tomcat-10.1.34\bin
  .\startup.bat
  ```
- **Linux / macOS**:
  ```bash
  cd /opt/tools/apache-tomcat-10.1.34/bin
  ./startup.sh
  ```

### Stopping Tomcat
To shut down the server gracefully:
- **Windows**:
  ```powershell
  cd C:\tools\apache-tomcat-10.1.34\bin
  .\shutdown.bat
  ```
- **Linux / macOS**:
  ```bash
  cd /opt/tools/apache-tomcat-10.1.34/bin
  ./shutdown.sh
  ```

### Verifying Tomcat Status
- **Root Page**: [http://localhost:8081/](http://localhost:8081/)
- **Manager Text API Test**:
  ```bash
  curl -s -u deployer:<password> http://localhost:8081/manager/text/list
  ```

---

## 3. Continuous Testing Jenkins Pipeline (`Jenkinsfile`)

The repository includes a declarative `Jenkinsfile` orchestrating the full build, unit testing, packaging, end-to-end quality gate, deployment, and health verification lifecycle:

### Pipeline Stages
1. **Checkout**: Checks out source code from Git SCM, logging current branch and commit SHA.
2. **Build**: Executes `mvn -B clean compile` (cross-platform using `isUnix()` helper).
3. **Unit Tests**: Executes `mvn -B test` (controlled by `RUN_TESTS`). Publishes JUnit XML test results. If unit tests fail, the build terminates immediately and deployment is aborted.
4. **Package**: Runs `mvn -B package -DskipTests` and archives the generated WAR artifact.
5. **Selenium Tests (Quality Gate)**: Executes `mvn -B -Pselenium test -Dheadless=true` (controlled by `RUN_SELENIUM`, default `true`).
   - Runs Selenium WebDriver tests across critical user journeys against headless Google Chrome.
   - Publishes JUnit XML reports.
   - Archives screenshots (`target/screenshots/*.png`), HTML test reports (`target/site/**/*.html`), and Surefire reports.
   - **Deployment Gate**: A failing test fails the stage; `Deploy` and `Verify` are skipped automatically.
6. **Deploy**: Authenticates via Jenkins credential `tomcat-manager` and uploads the WAR to Tomcat Manager Text API (`/manager/text/deploy?path=/<context>&update=true`).
7. **Verify**: Automated polling of `GET ${TOMCAT_URL}/<context>/health` (up to 12 retries, 5s intervals) ensuring synthetic health check returns `{"status":"UP"}`.

### Parameterized Settings

| Parameter | Type | Default Value | Description |
|---|---|---|---|
| `DEPLOY_ENV` | Choice | `dev` (`dev`, `staging`) | Target deployment environment suffix |
| `TOMCAT_URL` | String | `http://localhost:8081` | Base URL of the target Tomcat server |
| `APP_CONTEXT` | String | `construction-dashboard` | Base context path for web application |
| `RUN_TESTS` | Boolean | `true` | Run unit and slice test suite before packaging |
| `RUN_SELENIUM` | Boolean | `true` | Run Selenium E2E test suite as deployment quality gate |

*Target Application Context Path*: `/${APP_CONTEXT}-${DEPLOY_ENV}` (e.g., `/construction-dashboard-dev` or `/construction-dashboard-staging`).

---

## 4. Test Reporting & Quality Gate Artifacts

- **JUnit Test Reports**: Parsed and visualized natively in Jenkins build pages (`/testReport/`).
- **Failure Screenshots**: In the event of an E2E test failure, full-page screenshots are captured by `ScreenshotOnFailureExtension` and archived in Jenkins build artifacts under `target/screenshots/`.
- **HTML Surefire Reports**: Test execution summaries in `target/site/surefire-report.html` are archived and published.

---

## 5. Jenkins Credential Setup

In Jenkins:
1. Navigate to **Manage Jenkins** &rarr; **Credentials** &rarr; **System** &rarr; **Global credentials**.
2. Click **Add Credentials**:
   - **Kind**: Username with password
   - **ID**: `tomcat-manager`
   - **Username**: `deployer`
   - **Password**: *(Password configured in local `conf/tomcat-users.xml`)*
   - **Description**: Tomcat 10 Manager Script Deployment Account
