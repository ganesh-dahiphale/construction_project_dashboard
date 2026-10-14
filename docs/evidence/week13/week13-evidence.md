# Week 13 Evidence: Ansible Server Configuration & Node Provisioning

**Course Project:** Construction Progress Dashboard (DevOps Course - Week 13)  
**Issue:** [#52](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/52)  
**Feature PR:** [#53](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/53)  
**Date:** October 14, 2026  

---

## 1. Executive Summary

In Week 13, all server prerequisites for the Spring Boot 3 application (`construction-dashboard`) were codified into a modular Ansible role hierarchy. An isolated Ubuntu target node was provisioned and completely prepared for automated application container deployment in Week 14.

Key achievements:
1. **Isolated Target Environment**: Configured an isolated target node (`cpd-target`) on Ubuntu 26.04.1 LTS with systemd and OpenSSH server.
2. **Modular Role Architecture**: Built 6 discrete, single-responsibility Ansible roles (`common`, `docker`, `app_user`, `app_dirs`, `firewall`, `app_config`).
3. **Strict Idempotency**: Verified execution idempotency through dual playbook runs (`changed=0` on second execution).
4. **Security Hardening**: Enforced least privilege (`appdeploy` system user, mode `0750` for configuration/logs, mode `0600` for environment file, and UFW firewall with default-deny policy).
5. **Quality & Standards**: Met the `production` profile in `ansible-lint` and 0 errors in `yamllint`.

---

## 2. Infrastructure & Environment Specifications

### Control Node Specifications
- **Operating Environment**: Ubuntu 26.04.1 LTS (WSL2 on Windows host)
- **Ansible Version**: `ansible-core 2.20.1`
- **Python Version**: Python 3.14.4
- **Linter Versions**: `ansible-lint 26.1.1`, `yamllint 1.37.1`
- **Installed Collections**:
  - `ansible.posix` (v2.1.0)
  - `community.docker` (v5.0.4)
  - `community.general` (v12.1.0)
- **SSH Authentication**: Dedicated ed25519 keypair `~/.ssh/cpd_ansible`

### Target Node Specifications
- **Hostname**: `cpd-target` (inventory alias `cpd-target-node`)
- **Operating System**: Ubuntu 26.04.1 LTS (Resolute Raccoon)
- **Kernel**: `6.18.40.1-microsoft-standard-WSL2`
- **Init System**: `systemd` (version 259)
- **Hardware Profile**: 16 vCPUs (Intel Core i5-12500H), 7.6 GB RAM, 1 TB virtual disk
- **Access Details**: Port 22 SSH via dedicated key (`ganesh@127.0.0.1:22`)

---

## 3. Tool Versions Across Nodes

| Component | Control Node | Target Node |
| :--- | :--- | :--- |
| **OS Distribution** | Ubuntu 26.04.1 LTS | Ubuntu 26.04.1 LTS |
| **Kernel** | 6.18.40.1-microsoft-standard-WSL2 | 6.18.40.1-microsoft-standard-WSL2 |
| **Python** | 3.14.4 | 3.14.4 |
| **Ansible / Engine** | `ansible-core 2.20.1` | N/A (managed node) |
| **Docker Engine** | Docker Desktop 28.x | Docker CE 29.9.0 (build `f415da8`) |
| **containerd** | 2.1.x | 2.2.1 |
| **Firewall** | N/A | UFW 0.36.2 (iptables/nftables) |
| **Service Manager** | systemd 259 | systemd 259 |

---

## 4. Requirement-to-Role Mapping

Every server prerequisite identified in the Week 13 specification (`docs/CONFIG_SPEC.md`) is implemented in dedicated roles:

| Prerequisite Requirement | Target Path / Resource | Responsible Role | Implementation Details |
| :--- | :--- | :--- | :--- |
| System packages & utilities | `/usr/bin/timedatectl`, `curl`, `ca-certificates` | `common` | Installs base packages, syncs timezone to `Asia/Kolkata`. |
| Container runtime | `/usr/bin/docker`, `/etc/docker/daemon.json` | `docker` | Adds official Docker GPG key & apt repo; installs `docker-ce`, `containerd.io`, sets 10MB x 3 log rotation, starts service. |
| Non-root deploy user | `appdeploy` (UID/GID system) | `app_user` | Creates system user `appdeploy`, adds to `docker` group, disables password login. |
| Directory structure | `/opt/construction-dashboard/{config,logs,releases}` | `app_dirs` | Sets `0755` for app root and releases, `0750` for config and logs, owned by `appdeploy:docker`. |
| Network security & firewall | UFW rules for `22/tcp`, `8082/tcp` | `firewall` | Sets default incoming policy to `deny`, default outgoing to `allow`; permits SSH (22) and App (8082); enables UFW. |
| Application environment config | `/opt/construction-dashboard/config/app.env` | `app_config` | Deploys templated environment variables (`SPRING_PROFILES_ACTIVE=dev`, `SERVER_PORT=8080`, `JAVA_OPTS`) with mode `0600`. |
| Systemd service definition | `/etc/systemd/system/construction-dashboard.service` | `app_config` | Templates unit file requiring `docker.service`, enables on boot, leaves in `stopped` state. |

---

## 5. Execution Results & Play Recaps

### 5.1 Dry Run (`--check --diff`)
- **Command**:
  ```bash
  ansible-playbook -i inventory/hosts.yml playbooks/site.yml --check --diff
  ```
- **Evidence Log**: [`docs/evidence/week13/dry-run.log`](./dry-run.log)
- **Play Recap**:
  ```
  cpd-target-node : ok=7 changed=2 unreachable=0 failed=1 skipped=0 rescued=0 ignored=0
  ```
- **Technical Explanation**:  
  In Ansible check mode, the task `docker : Add official Docker apt repository` is evaluated without committing `/etc/apt/sources.list.d/docker.list` to disk or updating the apt index cache. The subsequent task `docker : Install Docker Engine packages` therefore fails to find `docker-ce` in the unaltered local apt cache. As specified in the Week 13 guidelines, this warning is expected during check mode against an unconfigured node and resolves on live execution.

---

### 5.2 First Live Run
- **Command**:
  ```bash
  ANSIBLE_LOG_PATH=docs/evidence/week13/ansible.log ansible-playbook -i inventory/hosts.yml playbooks/site.yml -v
  ```
- **Evidence Log**: [`docs/evidence/week13/first-run.log`](./first-run.log) and [`docs/evidence/week13/ansible.log`](./ansible.log)
- **Duration**: 2 minutes, 0 seconds
- **Play Recap**:
  ```
  cpd-target-node : ok=27 changed=16 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
  ```
- **Execution Highlights**:
  - All 6 roles completed successfully.
  - Apt keys, repositories, Docker Engine, and system user were created.
  - Directories were configured with secure ownership and modes.
  - UFW was activated with rules for ports 22 and 8082.
  - Handlers executed cleanly (systemd daemon reloaded).

---

### 5.3 Second Run (Idempotency Check)
- **Command**:
  ```bash
  ansible-playbook -i inventory/hosts.yml playbooks/site.yml
  ```
- **Evidence Log**: [`docs/evidence/week13/second-run.log`](./second-run.log)
- **Duration**: 19 seconds
- **Play Recap**:
  ```
  cpd-target-node : ok=25 changed=0 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
  ```
- **Idempotency Assessment**:  
  The playbook achieved **100% idempotency** with **`changed=0`** and **`failed=0`**. No tasks performed redundant modifications or state changes.

---

## 6. Target Node Verification: Before vs. After

Comparison based on [`target-before.txt`](./target-before.txt) vs. [`target-after.txt`](./target-after.txt):

| Inspection Item | Before Playbook Execution | After Playbook Execution | Verification Method |
| :--- | :--- | :--- | :--- |
| **Docker Engine** | `docker: command not found` | `Docker version 29.9.0, build f415da8` | `docker --version` |
| **Docker Daemon Service** | Not installed | `active` (running) | `systemctl is-active docker` |
| **App Service State** | Unit does not exist | `enabled`, `inactive` (stopped) | `systemctl is-enabled` / `is-active` |
| **Application User** | User does not exist (`id: no such user`) | `uid=999(appdeploy) gid=986(docker)` | `id appdeploy` |
| **Directory `/opt/...`** | Does not exist | Structure exists with `appdeploy:docker` | `ls -ld /opt/construction-dashboard/*` |
| **Firewall (UFW)** | Inactive | `Status: active`, Default `deny`, rules `22`, `8082` | `sudo ufw status verbose` |
| **Listening Ports** | Only SSH (`22`, `2222`) | SSH (`22`), UFW active, `8082` allowed | `ss -tlnp` |
| **Daemon Log Limits** | Default Docker configuration | `json-file`, max 10MB x 3 files | `cat /etc/docker/daemon.json` |
| **Application Config** | Does not exist | `app.env` present, mode `0600` | `sudo cat .../app.env` |

---

## 7. Container Pull & Network Connectivity Verification

To confirm outbound container image registry communications over the newly configured network and firewall, a test pull was executed on `cpd-target`:

```text
$ sudo docker pull hello-world
latest: Pulling from library/hello-world
4f55086f7dd0: Pull complete
Digest: sha256:11a3eb1597e864c51b3f56733cb2e8a90bf0c284bfc05f9444d81c558bc848aa
Status: Downloaded newer image for hello-world:latest
docker.io/library/hello-world:latest
```

The test succeeded completely, confirming that the Docker daemon can resolve DNS, communicate with registries, download layer blobs, and register container images.

---

## 8. Artifact & Documentation Links

- **Specification**: [`docs/CONFIG_SPEC.md`](../../CONFIG_SPEC.md)
- **Ansible Guide**: [`ansible/README.md`](../../../ansible/README.md)
- **Main Playbook**: [`ansible/playbooks/site.yml`](../../../ansible/playbooks/site.yml)
- **Evidence Files**:
  - Initial State: [`docs/evidence/week13/target-before.txt`](./target-before.txt)
  - Dry Run Output: [`docs/evidence/week13/dry-run.log`](./dry-run.log)
  - First Live Run: [`docs/evidence/week13/first-run.log`](./first-run.log)
  - Ansible Engine Log: [`docs/evidence/week13/ansible.log`](./ansible.log)
  - Post-Execution State: [`docs/evidence/week13/target-after.txt`](./target-after.txt)
  - Idempotency Second Run: [`docs/evidence/week13/second-run.log`](./second-run.log)
- **GitHub Issue**: [#52](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/52)
- **Feature PR**: [#53](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/53)
