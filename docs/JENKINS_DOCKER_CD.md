# Jenkins-Docker Continuous Deployment Pipeline Guide 🐳🚀

**Project:** Construction Progress Dashboard  
**Milestone:** Week 12 – Automated Container CD, Registry Publishing & Quality Gate Deployment  

---

## 1. Overview & Architecture

The Week 12 pipeline extends our continuous integration workflow into **Continuous Deployment (CD)**. After passing compilation, unit tests, artifact packaging, and the headless Selenium end-to-end quality gate, the pipeline automatically:
1. Builds a lean, production-grade Docker image using `Dockerfile.runtime` from the **exact tested WAR artifact**.
2. Tags the image with a deterministic, immutable version tag: `${APP_VERSION}-b${BUILD_NUMBER}-${SHORT_SHA}`.
3. Publishes the image to a container registry (**Docker Hub** or a **Local Registry** at `localhost:5000`).
4. Performs a zero-downtime container replacement on host port `8082`.
5. Verifies container health on `/health` and validates the version string on the web interface.
6. Automatically rolls back to the previous running container if health verification fails.
7. Promotes verified images with the `:stable` tag for future rollbacks and deployments.

---

## 2. Docker Image Tagging & Versioning Scheme

The pipeline implements an enterprise-grade tagging strategy:

| Tag Type | Format / Example | Purpose |
| :--- | :--- | :--- |
| **Immutable Build Tag** | `${APP_VERSION}-b${BUILD_NUMBER}-${SHORT_SHA}`<br>*(e.g., `0.0.1-SNAPSHOT-b12-a1b2c3d`)* | Unique, traceable tag tying the container directly to the Git commit and Jenkins build execution. |
| **Latest Pointer** | `:latest` | Pointers updated on every build to reference the freshest image in the registry. |
| **Promoted Stable Tag** | `:stable` | Assigned **only after** container passes both `/health` HTTP check and UI version validation. |

---

## 3. Supported Registry Modes

The pipeline parameter `REGISTRY_MODE` allows switching seamlessly between cloud and local registry environments:

### Mode A: Docker Hub (`REGISTRY_MODE=dockerhub`)
- **Repository Pattern**: `${DOCKER_USER}/construction-dashboard`
- **Authentication**: Managed securely via Jenkins credential ID `dockerhub-credentials`.
- **Security**: Password injected strictly via `--password-stdin` using single-quote shell interpolation, preventing secrets from leaking into logs or process tables.
- **Logout**: Enforced in a `finally` block to prevent lingering credentials on build agents.

### Mode B: Local Registry (`REGISTRY_MODE=local`)
- **Repository Pattern**: `localhost:5000/construction-dashboard`
- **Authentication**: Direct unauthenticated push to the local private registry.
- **Setup Script**:
  * Windows PowerShell: `.\scripts\local-registry.ps1`
  * Linux / Bash: `./scripts/local-registry.sh`
- Starts official `registry:2` container on port 5000 with `--restart unless-stopped`.

---

## 4. Pipeline Stages & Execution Flow

```text
[Checkout] ──> [Build] ──> [Unit Tests] ──> [Package WAR] ──> [Selenium E2E Gate]
                                                                        │
        ┌───────────────────────────────────────────────────────────────┘
        ▼
[Docker Build] (Dockerfile.runtime)
        ▼
[Docker Push] (Tag: version-b# & latest)
        ▼
[Deploy Container] (Port 8082, Dev Profile)
        ▼
[Verify Container] ──(Pass)──> [Promote] (:stable) ──> [Archive deploy-info.txt]
        │
     (Fail)
        ▼
[Automatic Rollback to Previous Image]
```

### Stage Details

1. **Checkout**: Checks out Git commit, parses `APP_VERSION` via Maven, computes 7-character commit SHA, and derives `IMAGE_TAG` and `IMAGE_REPO`.
2. **Build & Unit Tests**: Compiles application sources and runs JUnit 5 test suite (59 unit tests).
3. **Package**: Generates `target/dashboard.war` executable archive.
4. **Selenium Tests**: Executes headless Chrome Selenium suite against the packaged application as a blocking quality gate.
5. **Docker Build**: Executes `docker build -f Dockerfile.runtime` using the pre-tested WAR artifact, setting metadata labels (`version`, `revision`, `build`).
6. **Docker Push**: Pushes `${IMAGE_TAG}` and `:latest` to Docker Hub or the local registry, printing the resulting image digest.
7. **Deploy Container**: Records current running image ID as `PREVIOUS_IMAGE`, pulls the fresh image, stops/removes old container, and starts a fresh container with `-e APP_VERSION=${IMAGE_TAG}` and `-e SPRING_PROFILES_ACTIVE=dev` on port 8082.
8. **Verify Container**: Polls container health status up to 90 seconds until `healthy`, validates `http://localhost:8082/health` returns `UP`, and confirms the login page contains the version text. If verification fails, automatically rolls back to `PREVIOUS_IMAGE`.
9. **Promote**: Tags the verified image as `:stable` and pushes to the registry.
10. **Post Actions**: Prunes dangling image layers (`docker image prune -f`) and publishes `deploy-info.txt` artifact.

---

## 5. Deployment Artifact: `deploy-info.txt`

Every pipeline run archives an audit record with full deployment metadata:
```text
Deployment Information - Week 12
-----------------------------------------------------------
Application Name: Construction Progress Dashboard
Deploy Target:    docker
Registry Mode:    local
Image Repository: localhost:5000/construction-dashboard
Image Tag:        0.0.1-SNAPSHOT-b1-a1b2c3d
Image Digest:     sha256:...
Git Commit:       a1b2c3d
Build Number:     #1
Access URL:       http://localhost:8082
Health URL:       http://localhost:8082/health
Timestamp:        2026-10-14 15:10:00
```
