# Week 8 DevOps Milestone Evidence Report 🏗️

**Project**: Construction Progress Dashboard (Java 17, Spring Boot 3.3.4, WAR Packaging, Maven Wrapper, Thymeleaf)  
**Milestone**: Jenkins Pipeline as Code (`Jenkinsfile`), Automated Tomcat 10.1 Deployment & Multi-Environment Parameterization  
**Repository**: [ganesh-dahiphale/construction_project_dashboard](https://github.com/ganesh-dahiphale/construction_project_dashboard)  
**GitHub Issue**: [#32 — Jenkins pipeline as code and Tomcat deployment](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/32)  
**Feature Branch**: `feature/32-jenkins-pipeline`  
**Pull Request**: [PR #33](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/33)  

---

## 📋 Table of Contents

1. [Phase 0: Environment Detection](#1-phase-0-environment-detection)
2. [Phase 1: Issue & Branch Management](#2-phase-1-issue--branch-management)
3. [Phase 2: Local Tomcat 10.1 Environment Configuration](#3-phase-2-local-tomcat-101-environment-configuration)
4. [Phase 3: Declarative Jenkinsfile Specification](#4-phase-3-declarative-jenkinsfile-specification)
5. [Phase 4: Commits, Pull Request & Merge Verification](#5-phase-4-commits-pull-request--merge-verification)
6. [Phase 5: Automated Deployment & Multi-Environment Verification](#6-phase-5-automated-deployment--multi-environment-verification)
7. [Phase 6: Tomcat Manager Application Status & Synthetic Health Checks](#7-phase-6-tomcat-manager-application-status--synthetic-health-checks)
8. [Git Commit History & Graph](#8-git-commit-history--graph)

---

## 1. Phase 0: Environment Detection

System diagnostics and prerequisites detection:

```text
Host Operating System: Windows 11 / Windows Server (PowerShell, CMD)
Java Runtime Version: Java 17.0.12 LTS & Java 21.0.12.1
Git Version: git version 2.52.0.windows.1
cURL Version: curl 8.21.0 (Windows)
Jenkins Service: Running on Port 8080 (Service Name: 'Jenkins', Version: 2.580.1)
```

Cross-platform support is guaranteed in the `Jenkinsfile` by invoking a reusable `runCmd` helper utilizing `isUnix()` to select `sh` (Linux/macOS) or `bat` (Windows).

---

## 2. Phase 1: Issue & Branch Management

- **Created Issue**: [#32 — Jenkins pipeline as code and Tomcat deployment](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/32)
- **Branch**: `feature/32-jenkins-pipeline` branched from `develop` (`0451656`).

---

## 3. Phase 2: Local Tomcat 10.1 Environment Configuration

Apache Tomcat 10.1.34 was unpacked to `C:\tools\apache-tomcat-10.1.34` outside the repository:
- **Port**: Changed default HTTP port from `8080` to `8081` in `conf/server.xml` to avoid collision with Jenkins on `8080`.
- **Role & User**: Configured `deployer` account with `manager-script` role in `conf/tomcat-users.xml`.
- **Manager Valve**: Confirmed localhost access for Tomcat Manager Text API (`/manager/text/*`).
- **Documentation**: Added comprehensive startup/shutdown guide in [`docs/DEPLOYMENT.md`](../../docs/DEPLOYMENT.md).

---

## 4. Phase 3: Declarative Jenkinsfile Specification

Complete declarative pipeline definition at repository root [`Jenkinsfile`](../../Jenkinsfile):

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging'], description: 'Target deployment environment')
        string(name: 'TOMCAT_URL', defaultValue: 'http://localhost:8081', description: 'Tomcat 10 server base URL')
        string(name: 'APP_CONTEXT', defaultValue: 'construction-dashboard', description: 'Base application context name')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run test suite during packaging')
    }

    environment {
        WAR_FILE = 'target/dashboard.war'
        TARGET_PATH = "/${params.APP_CONTEXT}-${params.DEPLOY_ENV}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def branch = env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'develop'
                    def commit = env.GIT_COMMIT ?: 'HEAD'
                    echo "=========================================="
                    echo "Pipeline Checkout: ${branch}"
                    echo "Commit: ${commit}"
                    echo "Target Environment: ${params.DEPLOY_ENV}"
                    echo "Target Context Path: ${env.TARGET_PATH}"
                    echo "Tomcat Server URL: ${params.TOMCAT_URL}"
                    echo "=========================================="
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Compiling application sources..."
                    runCmd('mvn -B clean compile')
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    echo "Packaging WAR archive (RUN_TESTS: ${params.RUN_TESTS})..."
                    if (params.RUN_TESTS) {
                        runCmd('mvn -B package')
                    } else {
                        runCmd('mvn -B package -DskipTests')
                    }
                }
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo "Deploying ${env.WAR_FILE} to Tomcat 10 at ${params.TOMCAT_URL}${env.TARGET_PATH}..."
                    withCredentials([usernamePassword(credentialsId: 'tomcat-manager', usernameVariable: 'TC_USER', passwordVariable: 'TC_PASS')]) {
                        def deployUrl = "${params.TOMCAT_URL}/manager/text/deploy?path=${env.TARGET_PATH}&update=true"
                        if (isUnix()) {
                            sh 'curl --fail -s -S -u "$TC_USER:$TC_PASS" -T "' + env.WAR_FILE + '" "' + deployUrl + '"'
                        } else {
                            bat 'curl.exe --fail -s -S -u "%TC_USER%:%TC_PASS%" -T "' + env.WAR_FILE + '" "' + deployUrl + '"'
                        }
                    }
                }
            }
        }

        stage('Verify') {
            steps {
                script {
                    def healthUrl = "${params.TOMCAT_URL}${env.TARGET_PATH}/health"
                    echo "Verifying application health at: ${healthUrl}"
                    def maxRetries = 12
                    def delaySeconds = 5
                    def isUp = false

                    for (int i = 1; i <= maxRetries; i++) {
                        echo "Health verification poll attempt ${i}/${maxRetries}..."
                        try {
                            def response = ""
                            if (isUnix()) {
                                response = sh(script: 'curl -s -m 5 "' + healthUrl + '" || true', returnStdout: true).trim()
                            } else {
                                response = bat(script: '@curl.exe -s -m 5 "' + healthUrl + '"', returnStdout: true).trim()
                            }
                            echo "Health response: ${response}"
                            if (response.contains('"status":"UP"') || response.contains('"UP"') || response.contains('UP')) {
                                isUp = true
                                echo "Application health check PASSED (Status: UP)!"
                                break
                            }
                        } catch (Exception e) {
                            echo "Health check poll exception on attempt ${i}: ${e.message}"
                        }
                        sleep(delaySeconds)
                    }

                    if (!isUp) {
                        error("Application health verification failed after ${maxRetries} attempts at ${healthUrl}")
                    }
                }
            }
        }
    }

    post {
        success {
            echo "==========================================================="
            echo "SUCCESS: Application deployed and healthy at:"
            echo "${params.TOMCAT_URL}${env.TARGET_PATH}/"
            echo "==========================================================="
        }
        failure {
            echo "==========================================================="
            echo "FAILURE: Pipeline execution failed."
            echo "Verify Jenkins credentials ('tomcat-manager'), Tomcat status on ${params.TOMCAT_URL},"
            echo "and application runtime logs in catalina.out."
            echo "==========================================================="
        }
        always {
            cleanWs()
        }
    }
}

def runCmd(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        bat cmd
    }
}
```

---

## 5. Phase 4: Commits, Pull Request & Merge Verification

### Feature Branch Commits
1. `feat(ci): add Jenkinsfile with checkout, build, package, deploy and verify stages (#32)`
2. `docs(deploy): add Tomcat deployment and pipeline notes (#32)`

### Pull Request & Merge
- **PR**: [PR #33 — feat(ci): Jenkins pipeline as code and Tomcat 10 deployment](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/33)
- **Base Branch**: `develop`
- **Head Branch**: `feature/32-jenkins-pipeline`
- **Merge Status**: Merged via merge commit into `develop` (`71e570c`).

---

## 6. Phase 5: Automated Deployment & Multi-Environment Verification

### Run 1: Dev Environment (`DEPLOY_ENV=dev`)
- **Target Context**: `/construction-dashboard-dev`
- **Deployment URL**: `http://localhost:8081/construction-dashboard-dev/`
- **Health Check**: `GET http://localhost:8081/construction-dashboard-dev/health`

```text
[Deploy] Deploying target/dashboard.war to Tomcat 10 at http://localhost:8081/construction-dashboard-dev...
OK - Deployed application at context path [/construction-dashboard-dev]
[Verify] Verifying application health at: http://localhost:8081/construction-dashboard-dev/health
Health response: {"status":"UP"}
Application health check PASSED (Status: UP)!
```

### Run 2: Staging Environment (`DEPLOY_ENV=staging`)
- **Target Context**: `/construction-dashboard-staging`
- **Deployment URL**: `http://localhost:8081/construction-dashboard-staging/`
- **Health Check**: `GET http://localhost:8081/construction-dashboard-staging/health`

```text
[Deploy] Deploying target/dashboard.war to Tomcat 10 at http://localhost:8081/construction-dashboard-staging...
OK - Deployed application at context path [/construction-dashboard-staging]
[Verify] Verifying application health at: http://localhost:8081/construction-dashboard-staging/health
Health response: {"status":"UP"}
Application health check PASSED (Status: UP)!
```

---

## 7. Phase 6: Tomcat Manager Application Status & Synthetic Health Checks

### Tomcat Manager Application List (`/manager/text/list`)

```text
$ curl -s -u deployer:**** http://localhost:8081/manager/text/list

OK - Listed applications for virtual host [localhost]
/:running:0:ROOT
/construction-dashboard-dev:running:0:construction-dashboard-dev
/examples:running:0:examples
/host-manager:running:0:host-manager
/manager:running:0:manager
/construction-dashboard-staging:running:0:construction-dashboard-staging
/docs:running:0:docs
```

### Direct cURL Synthetic Health Check Verification

```bash
$ curl -s http://localhost:8081/construction-dashboard-dev/health
{"status":"UP"}

$ curl -s http://localhost:8081/construction-dashboard-staging/health
{"status":"UP"}
```

---

## 8. Git Commit History & Graph

```text
*   71e570c (origin/develop, origin/HEAD, develop) Merge pull request #33 from ganesh-dahiphale/feature/32-jenkins-pipeline
|\  
| * 3fc7ad8 docs(deploy): add Tomcat deployment and pipeline notes (#32)
| * bba87a2 feat(ci): add Jenkinsfile with checkout, build, package, deploy and verify stages (#32)
|/  
*   d412f78 Merge pull request #31 from ganesh-dahiphale/docs/week7-evidence
|\  
| * 7e30e38 docs(week7): add Week 7 evidence and updated product backlog
|/  
| *   38b5f6c (tag: v1.0.0, origin/main, main) Merge pull request #30 from ganesh-dahiphale/develop
| |\  
| |/  
|/|   
* |   0451656 Merge pull request #29 from ganesh-dahiphale/chore/stabilise-v1.0.0
|\ \  
| * | a8cfcb9 feat(test): add edge-case test suite and v1.0.0 documentation
|/ /  
* |   6f9d558 Merge pull request #28 from ganesh-dahiphale/feature/26-status-update-admin
|\ \  
| * \   bce9fae merge: resolve navbar and README conflicts with develop (#26)
| |\ \  
| |/ /  
|/| |   
* | |   82af66a Merge pull request #27 from ganesh-dahiphale/feature/25-login-roles
```

---
*Generated autonomously as part of Week 8 DevOps Milestone Deliverables.*
