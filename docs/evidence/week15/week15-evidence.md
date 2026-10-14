# Week 15 Evidence: Final End-to-End DevOps Workflow 🚀

**Course Project:** Construction Progress Dashboard (Java 17, Spring Boot 3, Docker, Jenkins, Ansible)  
**Milestone:** Week 15 (Final) – End-to-End CI/CD Pipeline, Automated Quality Gate, Containerization & Production Deployment  
**GitHub Issue:** [#58](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/58)  
**Pull Requests:** [#59](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/59) (Pipeline extension), [#60](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/60) (Footer UI demo), [#61](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/61) (v1.1.0 release prep), [#62](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/62) (Release merge to main)  
**GitHub Release:** [`v1.1.0`](https://github.com/ganesh-dahiphale/construction_project_dashboard/releases/tag/v1.1.0)  
**Target Node:** Ubuntu 26.04.1 LTS (`cpd-target` on `127.0.0.1:8082`)  

---

## 1. Executive Summary

Week 15 demonstrates the complete, closed-loop DevOps automation lifecycle for the Construction Progress Dashboard. A single code commit committed to GitHub flows automatically through:
1. SCM Polling / Trigger
2. Source Code Compilation (Maven + Java 17)
3. Unit and Integration Tests (JUnit 5 + MockMvc)
4. Application Packaging (WAR artifact creation)
5. Headless Selenium E2E Quality Gate (Chrome browser journeys)
6. Multi-Stage Docker Runtime Build & Registry Publishing
7. Ansible Provisioning & Automated Zero-Downtime Deployment
8. Remote Node Health Verification & Version UI Validation
9. Promotion of verified artifact to `:stable` Docker tag
10. Strict Idempotency & Automatic Audit Trail Generation

---

## 2. End-to-End Pipeline Execution Timeline

| Event / Stage | Start Time (IST) | End Time (IST) | Duration | Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Git Commit** | 01:23:55 | - | - | SCM Trigger | Commit `73fa1bc` ("chore(ui): update footer text for final release demo") |
| **Build Triggered** | 01:29:16 | - | - | SCM Poll | Build #3 scheduled automatically |
| **1. Checkout** | 01:29:16 | 01:29:16 | < 1s | **SUCCESS** | Evaluated metadata: `APP_VERSION=0.0.1-SNAPSHOT`, `IMAGE_TAG=0.0.1-SNAPSHOT-b3-73fa1bc` |
| **2. Build (Compile)** | 01:29:16 | 01:29:21 | 5.4s | **SUCCESS** | Recompiled 30 source files (`mvn -B clean compile`) |
| **3. Unit Tests** | 01:29:21 | 01:29:40 | 18.8s | **SUCCESS** | 58 tests executed across domain, repository, and controller layers; 0 failures |
| **4. Package** | 01:29:40 | 01:29:46 | 5.6s | **SUCCESS** | Packaged `target/dashboard.war` (53.84 MB) |
| **5. Selenium Quality Gate** | 01:29:46 | 01:31:14 | 88.2s | **SUCCESS** | Headless Chrome journeys (Login, Add Task, Status, Alerts, Search, Edit Task) |
| **6. Docker Build** | 01:31:14 | 01:31:22 | 8.4s | **SUCCESS** | Built image `localhost:5000/construction-dashboard:0.0.1-SNAPSHOT-b3-73fa1bc` |
| **7. Docker Push** | 01:31:22 | 01:31:25 | 3.1s | **SUCCESS** | Published versioned tag and `:latest` to local registry (digest: `sha256:61d049b2...`) |
| **8. Ansible Deploy** | 01:31:25 | 01:32:41 | 76.2s | **SUCCESS** | Executed `provision.yml`: pulled container, updated systemd service, zero-downtime swap |
| **9. Verify Node** | 01:32:41 | 01:32:47 | 5.9s | **SUCCESS** | Ran `healthcheck.yml`, confirmed HTTP `/health` = `UP`, confirmed live UI version badge |
| **10. Promote** | 01:32:47 | 01:32:50 | 2.8s | **SUCCESS** | Promoted and pushed `localhost:5000/construction-dashboard:stable` |
| **Pipeline Finish** | - | 01:32:51 | **3m 35s** | **SUCCESS** | Full lifecycle from Git commit to verified production container |

---

## 3. Production Runtime State on Target Node (`cpd-target`)

```bash
$ sudo docker ps --filter "name=construction-dashboard"
CONTAINER ID   IMAGE                                                             COMMAND                  CREATED       STATUS                 PORTS                                         NAMES
f6b0eb8ba7d8   localhost:5000/construction-dashboard:1.1.0                      "sh -c 'java $JAVA_O…"   2 hours ago   Up 2 hours (healthy)   0.0.0.0:8082->8080/tcp, [::]:8082->8080/tcp   construction-dashboard

$ sudo systemctl status construction-dashboard --no-pager
● construction-dashboard.service - Construction Progress Dashboard Container Service
     Loaded: loaded (/etc/systemd/system/construction-dashboard.service; enabled; preset: enabled)
     Active: active (running) since Thu 2026-10-15 01:37:02 IST
   Main PID: 12717 (docker)
     CGroup: /system.slice/construction-dashboard.service
             └─12717 /usr/bin/docker run --name construction-dashboard --rm -p 8082:8080 --env-file /opt/construction-dashboard/config/app.env localhost:5000/construction-dashboard:1.1.0

$ sudo cat /opt/construction-dashboard/releases/current.txt
1.1.0

$ sudo cat /opt/construction-dashboard/releases/previous.txt
0.0.1-SNAPSHOT-b3-73fa1bc

$ sudo tail -n 5 /opt/construction-dashboard/releases/history.log
2026-10-14 18:27:51 UTC MANUAL ROLLBACK from 0.0.1-SNAPSHOT-b2-f3cac90 to 0.0.1-SNAPSHOT-b1-fe3efe3
2026-10-14 18:29:29 UTC ROLLED BACK from 0.0.1-SNAPSHOT-b2-f3cac90 to 0.0.1-SNAPSHOT-b1-fe3efe3
2026-10-14 18:37:34 UTC DEPLOYED 0.0.1-SNAPSHOT-b2-f3cac90
2026-10-14 20:07:17 UTC DEPLOYED 0.0.1-SNAPSHOT-b3-73fa1bc
2026-10-14 22:41:45 UTC DEPLOYED 1.1.0
```

---

## 4. Live Workstation Verification

### HTTP Health Endpoint Verification:
```bash
$ curl -s http://127.0.0.1:8082/health
{"status":"UP"}
```

### UI Login Page Version Badge & Footer Verification:
```bash
$ curl -s http://127.0.0.1:8082/login | grep "data-testid=\"app-version\""
<span class="badge bg-secondary text-light mt-1" data-testid="app-version">Version: 1.1.0 - Final Release Demo</span>
```

---

## 5. Quality Gate Protection Demonstration

The CI/CD pipeline implements strict quality gate enforcement at Stage 5 (**Selenium Tests Quality Gate**).
- **Protection Mechanism**: The Selenium stage executes with `mvn -B -Pselenium test -Dheadless=true`. If any browser interaction journey fails (assertion error, element not found, or unexpected exception), Maven halts with exit code 1.
- **Production Safeguard**: Because subsequent stages (`Docker Build`, `Docker Push`, `Provision and Deploy (Ansible)`, and `Promote`) depend on the success of prior stages, a failing test completely prevents building the container image, pushing to the registry, or altering the remote target node.
- **Reference Evidence**: Full execution logs demonstrating this blockade are preserved in `docs/evidence/week10/`:
  - [`baseline-build-console.log`](../week10/baseline-build-console.log): 100% passing baseline build.
  - [`failed-build-console.log`](../week10/failed-build-console.log): Defective change introduced; Selenium gate failed; downstream package and deployment immediately halted.
  - [`fixed-build-console.log`](../week10/fixed-build-console.log): Defect resolved; pipeline execution resumed cleanly through completion.

---

## 6. Ansible Provisioning Idempotency (`changed=0`)

Running the Ansible provisioning automation against the live node after deployment demonstrates absolute convergence:
```text
PLAY RECAP *********************************************************************
cpd-target-node            : ok=37   changed=0    unreachable=0    failed=0    skipped=3    rescued=0    ignored=0   

PLAYBOOK RECAP *****************************************************************
Playbook run took 0 days, 0 hours, 0 minutes, 27 seconds
```
All system packages, Docker runtime configs, deployment users, directory permissions, firewall policies, systemd service definitions, and release states evaluated to zero changes (`changed=0`), demonstrating that the infrastructure is completely idempotent and safe for repeated automated execution.

---

## 7. Synthetic Health Check Summary

Execution of `ansible/playbooks/healthcheck.yml` verifies all multi-dimensional health criteria:
```text
================================================================================
                 APPLICATION HEALTH AND READINESS REPORT                        
================================================================================
 Host:              cpd-target-node (127.0.0.1)
 Service State:     active
 Container State:   running
 Endpoint /health:  UP (200 OK)
 Current Release:   1.1.0
 Disk Usage (/opt): 1%
 Memory Available:  5.8Gi
================================================================================
ok: [cpd-target-node] => {
    "changed": false,
    "msg": "HEALTH CHECK PASSED! Application is healthy, responsive, and serving traffic."
}
```

---

## 8. Deployment Information Audit (`deploy-info.txt`)

```text
Deployment Information - Week 15 Final End-to-End Release
-----------------------------------------------------------
Application Name: Construction Progress Dashboard
Deploy Target:    ansible (remote target node cpd-target)
Registry Mode:    local (localhost:5000/construction-dashboard)
Image Repository: localhost:5000/construction-dashboard
Image Tag:        1.1.0
Git Commit:       dc75c7f (dc75c7f)
Build Number:     #4
Target Node URL:  http://127.0.0.1:8082
Health URL:       http://127.0.0.1:8082/health
Result:           SUCCESS
```
