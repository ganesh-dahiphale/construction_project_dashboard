# Tomcat 10.1 Deployment Guide & Pipeline Architecture 🚀

This document describes the local Apache Tomcat 10.1 deployment environment, configuration parameters, management commands, and the automated Jenkins CI/CD pipeline.

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

## 3. Jenkins Pipeline as Code (`Jenkinsfile`)

The repository includes a declarative `Jenkinsfile` orchestrating the full build, test, package, deployment, and verification lifecycle:

### Pipeline Stages
1. **Checkout**: Checks out source code from Git SCM, logging current branch and commit SHA.
2. **Build**: Executes `mvn -B clean compile` (cross-platform using `isUnix()` helper).
3. **Package**: Runs `mvn -B package` (respecting `RUN_TESTS` parameter) and archives the generated WAR artifact.
4. **Deploy**: Authenticates via Jenkins credential `tomcat-manager` and uploads the WAR to Tomcat Manager Text API (`/manager/text/deploy?path=/<context>&update=true`).
5. **Verify**: Automated polling of `GET ${TOMCAT_URL}/<context>/health` (up to 12 retries, 5s intervals) ensuring synthetic health check returns `{"status":"UP"}`.

### Parameterized Settings

| Parameter | Type | Default Value | Description |
|---|---|---|---|
| `DEPLOY_ENV` | Choice | `dev` (`dev`, `staging`) | Target deployment environment suffix |
| `TOMCAT_URL` | String | `http://localhost:8081` | Base URL of the target Tomcat server |
| `APP_CONTEXT` | String | `construction-dashboard` | Base context path for web application |
| `RUN_TESTS` | Boolean | `true` | Whether to run test suites during packaging |

*Target Application Context Path*: `/${APP_CONTEXT}-${DEPLOY_ENV}` (e.g., `/construction-dashboard-dev` or `/construction-dashboard-staging`).

---

## 4. Jenkins Credential Setup

In Jenkins:
1. Navigate to **Manage Jenkins** &rarr; **Credentials** &rarr; **System** &rarr; **Global credentials**.
2. Click **Add Credentials**:
   - **Kind**: Username with password
   - **ID**: `tomcat-manager`
   - **Username**: `deployer`
   - **Password**: *(Password configured in local `conf/tomcat-users.xml`)*
   - **Description**: Tomcat 10 Manager Script Deployment Account
