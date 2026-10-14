# Week 14 Evidence: Production Provisioning, Idempotency, Health Gates & Automated Rollback

**Course Project:** Construction Progress Dashboard (DevOps Course - Week 14)  
**Issue:** [#55](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/55)  
**Feature PR:** [#56](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/56)  
**Date:** October 14, 2026  

---

## 1. Executive Summary

In Week 14, the automation infrastructure established in Week 13 was expanded into a continuous deployment and resilience framework for the containerized Spring Boot 3 application (`construction-dashboard`). A clean Ubuntu target environment (`cpd-target`) was provisioned from scratch, verified across multiple versioned releases, validated for strict idempotency, and subjected to automated and manual rollback and recovery tests.

### Key Achievements
1. **Automated Clean Provisioning & Deployment**: Provisioned a clean target environment and deployed versioned application containers using the newly engineered `deploy` role.
2. **Release Tracking & Audit Trail**: Implemented persistent release state tracking via `/opt/construction-dashboard/releases/current.txt`, `previous.txt`, and append-only `history.log`.
3. **Strict Idempotency Demonstrated**: Verified zero modifications (`changed=0`) across consecutive playbook executions, verified convergence in check-mode (`--check --diff`), and demonstrated automatic reconciliation after intentional configuration drift.
4. **Live Health Gate & Atomic Automatic Rollback**: Built an Ansible `block` / `rescue` pattern with HTTP `/health` polling (up to 25 retries) and login page version verification. Successfully triggered and documented automated rollback to the previous stable release when deploying a defective test release (`broken-demo`).
5. **Operator-Controlled Manual Rollback**: Developed and verified `playbooks/rollback.yml` with explicit confirmation safeguards (`confirm_rollback=yes`).
6. **Fault & Crash Resilience**: Documented automatic systemd supervisor recovery from unexpected container termination (17 seconds downtime) and node reboot recovery (13 seconds SSH, 22 seconds application availability).
7. **Quality & Standards Compliance**: Passed `ansible-lint` with 0 failures and 0 warnings under the strict `production` profile.

---

## 2. Infrastructure & Environment Specifications

### Control Node
- **Operating System**: Ubuntu 26.04.1 LTS (WSL2 on Windows host)
- **Ansible Version**: `ansible-core 2.20.1`
- **Python Version**: Python 3.14.4
- **Linters**: `ansible-lint 26.1.1` (profile: production), `yamllint 1.37.1`
- **Installed Collections**:
  - `ansible.posix` (v2.1.0)
  - `community.docker` (v5.0.4)
  - `community.general` (v12.1.0)
- **SSH Authentication**: Dedicated keypair `~/.ssh/cpd_ansible`

### Target Node (`cpd-target`)
- **Hostname**: `cpd-target` (inventory alias `cpd-target-node`)
- **Operating System**: Ubuntu 26.04.1 LTS (Resolute Raccoon)
- **Kernel**: `6.18.40.1-microsoft-standard-WSL2`
- **Init System**: `systemd` (version 259)
- **Container Runtime**: Docker Engine CE 29.9.0 (build `f415da8`)
- **Firewall**: UFW 0.36.2 (default-deny incoming, ports `22/tcp` and `8082/tcp` allowed)
- **Application Directory**: `/opt/construction-dashboard` (user `appdeploy`, group `docker`)

---

## 3. Releases Evaluated & Deployed

| Release Identifier | Image & Tag | Commit SHA | Source / Build Context | Deployment Role / Purpose |
|---|---|---|---|---|
| **RELEASE_PREV** | `localhost:5000/construction-dashboard:0.0.1-SNAPSHOT-b1-fe3efe3` | `fe3efe3` | Jenkins Pipeline Build #1 (Week 12) | Baseline stable release for clean provisioning and rollback target |
| **RELEASE_CURR** | `localhost:5000/construction-dashboard:0.0.1-SNAPSHOT-b2-f3cac90` | `f3cac90` | Jenkins Pipeline Build #2 (Week 12) | Target active production release |
| **BROKEN_DEMO** | `localhost:5000/construction-dashboard:broken-demo` | Defective demo | Dockerfile.broken (`-Dserver.port=9999`) | Deliberate port-misconfigured image to validate automated rollback |

---

## 4. Architecture & Ansible Role Hierarchy

```text
ansible/
├── group_vars/
│   └── app_servers.yml            # Image repository, ports (8082->8080), memory limits
├── inventory/
│   └── hosts.yml                  # cpd-target-node (127.0.0.1:22)
├── playbooks/
│   ├── provision.yml              # End-to-end server provisioning & container deployment
│   ├── healthcheck.yml            # Synthetic health check & readiness audit
│   ├── rollback.yml               # Manual rollback to previous stable release
│   └── status.yml                 # Release status and history audit report
└── roles/
    ├── common/                    # Apt cache, base utilities, Asia/Kolkata timezone
    ├── docker/                    # Docker CE upstream repository, engine, daemon.json
    ├── app_user/                  # Dedicated system user (appdeploy) and docker group
    ├── app_dirs/                  # /opt/construction-dashboard/{config,logs,releases}
    ├── firewall/                  # UFW port 22 first, port 8082, default deny inbound
    ├── app_config/                # app.env and systemd unit (Restart=always)
    └── deploy/                    # Container pull, block/rescue deploy, healthcheck, audit
```

### The `deploy` Role Architecture
The deployment automation is enclosed in an Ansible `block` / `rescue` construct:
1. **Fact Gathering**: Identifies currently running release from `releases/current.txt` (`pre_deploy_tag`).
2. **Deploy Block**:
   - Updates `app.env` and `construction-dashboard.service` with target `release_tag`.
   - Restarts container service if release changed.
   - Polls `http://127.0.0.1:8082/health` every 5 seconds for up to 25 retries (125 seconds total).
   - Verifies the version badge on `http://127.0.0.1:8082/login`.
   - On success: updates `previous.txt`, writes `current.txt`, appends `DEPLOYED <tag>` to `history.log`.
3. **Rescue Block**:
   - Triggered immediately if `/health` fails or times out.
   - Reverts `app.env` and systemd unit to `pre_deploy_tag`.
   - Restarts container service with previous stable release.
   - Polls `/health` to verify service restoration.
   - Records `ROLLED BACK from <bad_tag> to <stable_tag>` in `history.log`.
   - Fails the playbook with an operator alert.

---

## 5. Provisioning & Deployment Execution

### 5.1 Clean Target Node Reset
Prior to first execution, `cpd-target` was reset to an unprovisioned state: Docker packages were uninstalled, UFW was reset, and the `/opt/construction-dashboard` directory was purged.
- **Evidence Log**: [`docs/evidence/week14/target-clean-before.txt`](./target-clean-before.txt)

### 5.2 Run 1 — Initial Provisioning & PREV Release Deployment
- **Command**:
  ```bash
  ansible-playbook -i inventory/hosts.yml playbooks/provision.yml -e release_tag=0.0.1-SNAPSHOT-b1-fe3efe3
  ```
- **Evidence Log**: [`docs/evidence/week14/provision-run1.log`](./provision-run1.log)
- **Play Recap**:
  ```text
  cpd-target-node : ok=38 changed=20 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
  ```
- **Live Health Check Verification**:
  - Log: [`docs/evidence/week14/healthcheck-1.log`](./healthcheck-1.log) (`ok=11, changed=0`)
  - Curl `/health`: `{"status":"UP"}` ([`curl-health-1.txt`](./curl-health-1.txt))
  - Curl `/login`: `Version: 0.0.1-SNAPSHOT-b1-fe3efe3` ([`curl-login-1.txt`](./curl-login-1.txt))

### 5.3 Upgrading to Current Release (RELEASE_CURR)
- **Command**:
  ```bash
  ansible-playbook -i inventory/hosts.yml playbooks/provision.yml -e release_tag=0.0.1-SNAPSHOT-b2-f3cac90
  ```
- **Evidence Log**: [`docs/evidence/week14/deploy-curr.log`](./deploy-curr.log)
- **Play Recap**:
  ```text
  cpd-target-node : ok=39 changed=8 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
  ```
- **Release Status Verification**:
  - Log: [`docs/evidence/week14/status-curr.log`](./status-curr.log)
  - `current.txt`: `0.0.1-SNAPSHOT-b2-f3cac90`
  - `previous.txt`: `0.0.1-SNAPSHOT-b1-fe3efe3`

---

## 6. Idempotency & Configuration Drift Evidence

### 6.1 Repeated Provisioning Runs (Run 2 and Run 3)
To prove strict idempotency, `provision.yml` was executed consecutively against the converged node with `release_tag=0.0.1-SNAPSHOT-b2-f3cac90`.

| Metric | Run 1 (Fresh Node) | Run 2 (Convergence) | Run 3 (Idempotency) |
|---|---|---|---|
| **Playbook Run** | `provision-run1.log` | `provision-run2.log` | `provision-run3.log` |
| **Tasks OK** | 38 | 35 | 35 |
| **Tasks Changed** | **20** | **0** | **0** |
| **Tasks Failed** | 0 | 0 | 0 |
| **Execution Time** | 1m 58s | 39s | 38s |

- **Evidence Logs**: [`provision-run2.log`](./provision-run2.log), [`provision-run3.log`](./provision-run3.log)

### 6.2 Configuration Drift & Self-Healing Reconciliation
Configuration drift was deliberately introduced on the target host:
1. File permissions on `/opt/construction-dashboard/config/app.env` were degraded from `0600` to `0644`.
2. The UFW firewall rule allowing `8082/tcp` was deleted.
- **Pre-Drift Evidence**: [`docs/evidence/week14/drift-before.txt`](./drift-before.txt)

Executing `provision.yml` immediately detected and corrected both anomalies:
- Task `app_config : Deploy application environment configuration file` repaired file mode to `0600`.
- Task `firewall : Allow application traffic on host port` re-added `8082/tcp` to UFW rules.
- **Play Recap**: `ok=35 changed=2 unreachable=0 failed=0`
- **Reconciliation Evidence**: [`docs/evidence/week14/drift-run.log`](./drift-run.log) and [`docs/evidence/week14/drift-after.txt`](./drift-after.txt)

### 6.3 Dry-Run Check Mode (`--check --diff`)
Post-convergence check mode confirmed zero planned modifications:
- **Command**: `ansible-playbook -i inventory/hosts.yml playbooks/provision.yml -e release_tag=0.0.1-SNAPSHOT-b2-f3cac90 --check --diff`
- **Play Recap**: `ok=35 changed=0 unreachable=0 failed=0`
- **Evidence Log**: [`docs/evidence/week14/check-mode.log`](./check-mode.log)

---

## 7. Rollback & Fault Recovery Testing

### 7.1 Automated Rollback Demonstration
To validate the safety gate, a defective image (`localhost:5000/construction-dashboard:broken-demo`) was deployed. The image runs Spring Boot with `-Dserver.port=9999`, preventing HTTP traffic on mapped port 8082:8080.

1. **Failure Triggered**: The deploy block attempted deployment and polled `http://127.0.0.1:8082/health`. All retries failed with `Connection refused`.
2. **Rescue Executed**:
   - Reverted configuration and systemd unit to `pre_deploy_tag` (`0.0.1-SNAPSHOT-b2-f3cac90`).
   - Restarted `construction-dashboard.service`.
   - Polled `/health` endpoint until UP.
   - Appended audit entry: `ROLLED BACK from broken-demo to 0.0.1-SNAPSHOT-b2-f3cac90`.
3. **Evidence Log**: [`docs/evidence/week14/rollback-auto.log`](./rollback-auto.log) (`ok=48 changed=6 failed=1 rescued=1`)
4. **Post-Rollback Verification**:
   - Health check: [`healthcheck-post-auto-rollback.log`](./healthcheck-post-auto-rollback.log) (`ok=11, changed=0`, status: UP)
   - Status log: [`status-post-auto-rollback.log`](./status-post-auto-rollback.log)
   - Curl audit: [`curl-post-auto-rollback.txt`](./curl-post-auto-rollback.txt)

### 7.2 Manual Operator Rollback
The operator-facing manual rollback playbook was executed to revert from `0.0.1-SNAPSHOT-b2-f3cac90` to `0.0.1-SNAPSHOT-b1-fe3efe3`:
- **Command**:
  ```bash
  ansible-playbook -i inventory/hosts.yml playbooks/rollback.yml -e confirm_rollback=yes
  ```
- **Evidence Log**: [`docs/evidence/week14/rollback-manual.log`](./rollback-manual.log)
- **Play Recap**: `ok=21 changed=5 unreachable=0 failed=0`
- **Audit Verification**: Service was restored to `RELEASE_PREV` and health confirmed UP.

The target was subsequently re-provisioned with `RELEASE_CURR` before completing recovery benchmarks.

### 7.3 Container Crash Recovery
- **Failure Simulated**: `sudo docker kill construction-dashboard`
- **Supervisor Action**: Systemd service manager detected process termination (exit status 137). Enforced `RestartSec=10s`, cleared stale container with `ExecStartPre`, and launched a new container instance (`60133556fb72`).
- **Recovery Timing**:
  - Container killed: `18:40:54 UTC`
  - `/health` returned UP: `18:41:11 UTC`
  - **Total Downtime**: **17 seconds** (10s backoff + 7s JVM/Tomcat startup)
- **Evidence Log**: [`docs/evidence/week14/recovery-container.log`](./recovery-container.log)

### 7.4 Node Reboot Recovery
- **Failure Simulated**: Target node reboot (`wsl.exe --terminate cpd-target` followed by systemd restart)
- **Recovery Timing**:
  - Reboot command initiated: `18:59:35 UTC`
  - SSH daemon available: `+13 seconds`
  - Docker engine & systemd service started: `+15 seconds`
  - Application `/health` returned UP: `18:59:57 UTC` (`+22 seconds`)
  - **Total Recovery Time**: **22 seconds**
- **Evidence Log**: [`docs/evidence/week14/recovery-reboot.log`](./recovery-reboot.log)

---

## 8. Final Environment State Audit

A complete forensic inspection of the live target node was executed and captured in [`docs/evidence/week14/final-state.txt`](./final-state.txt):
- **Hostname**: `cpd-target`
- **UFW Status**: Active, default deny incoming, `22/tcp` and `8082/tcp` allowed.
- **Docker Containers**: Container `construction-dashboard` running `localhost:5000/construction-dashboard:0.0.1-SNAPSHOT-b2-f3cac90` with port mapping `0.0.0.0:8082->8080/tcp`.
- **Systemd Units**: `docker.service` active (running), `construction-dashboard.service` active (running).
- **Active Release Pointer (`current.txt`)**: `0.0.1-SNAPSHOT-b2-f3cac90`
- **Previous Release Pointer (`previous.txt`)**: `0.0.1-SNAPSHOT-b1-fe3efe3`
- **Application Health**: `{"status":"UP"}` (HTTP 200 OK)
- **Login Version Badge**: `Version: 0.0.1-SNAPSHOT-b2-f3cac90 - Jenkins-Docker CD demo`

---

## 9. Evidence Artifacts Index

All evidence files are located in `docs/evidence/week14/`:

| File Name | Description | Key Result |
|---|---|---|
| [`target-clean-before.txt`](./target-clean-before.txt) | Pre-execution clean target state audit | No docker, no app dirs, ufw inactive |
| [`provision-run1.log`](./provision-run1.log) | Initial provisioning and PREV release deployment | `ok=38 changed=20 failed=0` |
| [`healthcheck-1.log`](./healthcheck-1.log) | Health check on initial deployment | `ok=11 changed=0`, status: UP |
| [`curl-health-1.txt`](./curl-health-1.txt) | Raw HTTP output of /health endpoint | `{"status":"UP"}` |
| [`curl-login-1.txt`](./curl-login-1.txt) | Login page version badge extraction | `Version: 0.0.1-SNAPSHOT-b1-fe3efe3` |
| [`deploy-curr.log`](./deploy-curr.log) | Deployment of CURRENT release (b2-f3cac90) | `ok=39 changed=8 failed=0` |
| [`status-curr.log`](./status-curr.log) | Release status after CURRENT deployment | current=b2-f3cac90, prev=b1-fe3efe3 |
| [`provision-run2.log`](./provision-run2.log) | Second run testing idempotency | `ok=35 changed=0 failed=0` |
| [`provision-run3.log`](./provision-run3.log) | Third run testing idempotency | `ok=35 changed=0 failed=0` |
| [`drift-before.txt`](./drift-before.txt) | State after injecting configuration drift | Mode 0644, rule 8082 deleted |
| [`drift-run.log`](./drift-run.log) | Provisioning run healing configuration drift | `ok=35 changed=2 failed=0` |
| [`drift-after.txt`](./drift-after.txt) | State after drift remediation | Mode 0600 restored, 8082 restored |
| [`check-mode.log`](./check-mode.log) | Dry-run audit (`--check --diff`) | `ok=35 changed=0 failed=0` |
| [`rollback-auto.log`](./rollback-auto.log) | Defective release deployment and automated rollback | `ok=48 changed=6 failed=1 rescued=1` |
| [`healthcheck-post-auto-rollback.log`](./healthcheck-post-auto-rollback.log) | Health check post automatic rollback | `ok=11 changed=0`, status: UP |
| [`status-post-auto-rollback.log`](./status-post-auto-rollback.log) | Release audit trail showing rollback event | ROLLED BACK from broken-demo to b2-f3cac90 |
| [`curl-post-auto-rollback.txt`](./curl-post-auto-rollback.txt) | Version badge verification post-rollback | `Version: 0.0.1-SNAPSHOT-b2-f3cac90` |
| [`rollback-manual.log`](./rollback-manual.log) | Manual operator rollback execution | `ok=21 changed=5 failed=0` |
| [`recovery-container.log`](./recovery-container.log) | Docker container crash and supervisor auto-restart | Downtime: 17 seconds |
| [`recovery-reboot.log`](./recovery-reboot.log) | Target node reboot and service auto-start | SSH: 13s, Total: 22s |
| [`healthcheck-final.log`](./healthcheck-final.log) | Final health check run on live node | `ok=11 changed=0`, status: UP |
| [`final-state.txt`](./final-state.txt) | Comprehensive post-testing target state audit | All services active, health UP |
