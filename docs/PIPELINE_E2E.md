# End-to-End CI/CD Pipeline & Ansible Deployment Specification 🚀

**Project:** Construction Progress Dashboard  
**Milestone:** Week 15 – Final End-to-End CI/CD Workflow  
**Reference Issue:** #58  

---

## 1. Overview & Pipeline Philosophy

The Construction Progress Dashboard CI/CD pipeline implements a fully automated, production-grade DevOps workflow bridging application development, continuous integration, containerization, configuration management, automated deployment, and verification.

Every code change committed to GitHub triggers an automated execution path:
1. **Source Control Management**: Git commit on `develop` or feature branches triggers Jenkins polling / webhook.
2. **Deterministic Build & Compilation**: Java 17 + Spring Boot 3 compiled with Maven.
3. **Automated Unit & Integration Tests**: JUnit 5 and Spring Boot Test suite.
4. **WAR Packaging**: Production web application archive (`target/dashboard.war`).
5. **Selenium E2E Quality Gate**: Headless Chrome browser journeys validating core user workflows (Login, Project Creation, Task Assignment). A test failure immediately halts the pipeline.
6. **Docker Build & Push**: Multi-stage container packaging (`Dockerfile.runtime`) tagged with immutable `${APP_VERSION}-b${BUILD_NUMBER}-${SHORT_SHA}`, published to container registry.
7. **Ansible Provisioning & Deployment**: Ansible playbook execution (`ansible/playbooks/provision.yml`) targeting Ubuntu node (`cpd-target`), configuring environment, pull-based container restart via systemd service `construction-dashboard.service`.
8. **Automated Health Check & UI Verification**: HTTP `/health` polling and live UI version verification.
9. **Automatic Rollback**: On health check or UI verification failure, the Ansible deploy role or Jenkins pipeline rolls back the node to the previous healthy container image.
10. **Promotion & Artifact Archival**: Promotion of verified image to `:stable`, archival of `deploy-info.txt`, test reports, and execution logs.

---

## 2. Pipeline Execution Flow

```text
[ Git Commit ] ──> [ Poll SCM (H/2 * * * *) ]
                          │
                          ▼
                  [ 1. Checkout ]
                          │
                          ▼
                  [ 2. Build / Compile ]
                          │
                          ▼
               [ 3. Unit & Integration Tests ] ──(Fail)──> [ Abort & Alert ]
                          │ (Pass)
                          ▼
                  [ 4. Package WAR ]
                          │
                          ▼
            [ 5. Selenium E2E Quality Gate ] ──(Fail)──> [ Quality Gate Blocked ]
                          │ (Pass)
                          ▼
               [ 6. Docker Build & Push ]
                          │
                          ▼
        [ 7. Provision & Deploy (Ansible) ]
                          │
                          ▼
                 [ 8. Verify Node ] ───────────(Fail)──> [ Auto-Rollback to Previous ]
                          │ (Pass)
                          ▼
              [ 9. Promote Image (:stable) ]
                          │
                          ▼
         [ 10. Archival & Notifications ]
```

---

## 3. Pipeline Parameters & Configuration

| Parameter | Choices / Default | Purpose |
| :--- | :--- | :--- |
| `DEPLOY_TARGET` | `ansible` (default), `docker`, `tomcat` | Defines deployment target runtime: remote node via Ansible, local Docker container, or standalone Tomcat. |
| `REGISTRY_MODE` | `dockerhub`, `local` (default: `dockerhub`) | Target Docker registry (`ganesh-dahiphale/construction-dashboard` vs `localhost:5000/construction-dashboard`). |
| `TARGET_HOST` | `127.0.0.1` | Remote node host IP/DNS for Ansible execution and external verification. |
| `HOST_PORT` | `8082` | Exposed application host port. |
| `RUN_TESTS` | `true` (boolean) | Execute JUnit unit tests. |
| `RUN_SELENIUM` | `true` (boolean) | Enforce Selenium E2E quality gate before packaging and deployment. |

---

## 4. Ansible Remote Node Deployment

The pipeline executes `ansible/playbooks/provision.yml` with dynamic release parameters:
```bash
ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml \
    -e release_tag=${IMAGE_TAG} \
    -e docker_image=${IMAGE_REPO}
```

### Security & Credential Isolation:
- **SSH Authentication**: Uses Jenkins credential `cpd-ansible-ssh` (bound via `withCredentials` / `sshUserPrivateKey`).
- **Secret Protection**: SSH keys are stored in Jenkins credentials vault, never echoed to console logs.
- **Strict Configuration**: Host key checking is handled cleanly with `ANSIBLE_HOST_KEY_CHECKING=False` for lab virtualization.

### Deployment Idempotency:
- All Ansible roles (`common`, `docker`, `app_user`, `app_dirs`, `firewall`, `app_config`, `deploy`) are designed to be idempotent.
- Running the playbook multiple times against an unchanged target results in `changed=0`, ensuring safe re-execution.

---

## 5. Automated Rollback & Fault Tolerance

Fault tolerance is implemented at two defense layers:
1. **Ansible Role Block/Rescue**: The `deploy` role executes container updates within an Ansible `block`. If the health check or UI verification tasks fail, the `rescue` section triggers automatic fallback to the previous image tag stored in `/opt/construction-dashboard/releases/previous.txt`.
2. **Jenkins Post-Verification Stage**: The `Verify Node` stage tests the live endpoint independently from the Jenkins executor. If the application does not return HTTP 200 `UP` or the UI does not show `IMAGE_TAG`, the Jenkins pipeline aborts and marks the build as failed.

---

## 6. Workstation Deployment Runners (Fallback)

For hybrid environments where the Jenkins build agent runs in an isolated Windows service context and Ansible runs in WSL2 or Linux, documented workstation runners are provided:
- **Bash / Linux / WSL**: `scripts/deploy-node.sh`
- **PowerShell (Windows)**: `scripts/deploy-node.ps1`

Both runners execute the identical Ansible automation, execute verification checks, and emit the standardized `deploy-info.txt` audit trail.
