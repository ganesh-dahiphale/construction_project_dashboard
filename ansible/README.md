# Ansible Configuration Management & Deployment Guide 🛠️

**Project**: Construction Progress Dashboard  
**Milestone**: Week 14 — Provisioning, Idempotency, Health Checks & Automatic Rollback  
**Roles**: `common`, `docker`, `app_user`, `app_dirs`, `firewall`, `app_config`, `deploy`  

---

## 1. Prerequisites

### Control Node (Where Ansible Runs)
- **OS**: Linux, macOS, or WSL2 (Ubuntu 22.04 / 24.04 / 26.04 LTS). Native Windows is **not** supported as an Ansible control node.
- **Python**: Python 3.10+
- **Core Packages**:
  - `ansible-core >= 2.15.0`
  - `ansible-lint >= 6.0.0`
  - `yamllint >= 1.30.0`
- **Ansible Collections**:
  - `ansible.posix >= 1.5.0`
  - `community.docker >= 3.10.0`

Install required collections:
```bash
ansible-galaxy collection install -r requirements.yml
```

### Target Node (Managed Host)
- **OS**: Clean Ubuntu 22.04, 24.04, or 26.04 LTS.
- **SSH Access**: Key-based authentication configured for a user with `sudo` privileges without interactive password prompt (`NOPASSWD:ALL`).
- Dedicated private key configured at `~/.ssh/cpd_ansible`.

---

## 2. Directory Structure

```text
ansible/
├── .ansible-lint                  # Production linting configuration profile
├── ansible.cfg                    # Ansible default configuration & privilege escalation
├── requirements.yml               # Required collections (ansible.posix, community.docker)
├── README.md                      # Usage guide and operational instructions
├── demo/
│   ├── Dockerfile.broken          # Defective image listening on wrong port (9999) for rollback demo
│   └── load-broken-image.sh       # Stream loader script for target Docker daemon
├── inventory/
│   ├── hosts.yml                  # Production target host definition
│   └── hosts.example.yml          # Generic inventory template
├── group_vars/
│   └── app_servers.yml            # Environment parameters, ports, and versions
├── playbooks/
│   ├── site.yml                   # Top-level site infrastructure orchestration playbook
│   ├── provision.yml              # End-to-end node provisioning & application deployment
│   ├── healthcheck.yml            # Comprehensive health check and readiness audit
│   ├── rollback.yml               # Manual rollback to previous stable release
│   └── status.yml                 # Release status and deployment audit trail display
└── roles/
    ├── common/                    # Apt cache, base packages, timezone Asia/Kolkata
    ├── docker/                    # Docker CE upstream repository, packages, daemon.json
    ├── app_user/                  # Dedicated system user (appdeploy) and docker group
    ├── app_dirs/                  # Runtime directories (/opt/construction-dashboard/*)
    ├── firewall/                  # UFW port 22 first, port 8082, default deny inbound
    ├── app_config/                # app.env and systemd unit service configuration
    └── deploy/                    # Application container deployment, health check & rollback
```

---

## 3. Configuration Variables (`group_vars/app_servers.yml`)

| Variable | Default Value | Description |
|---|---|---|
| `app_user` | `appdeploy` | Dedicated non-login system user for container execution. |
| `app_group` | `docker` | Group membership allowing container management. |
| `app_dir` | `/opt/construction-dashboard` | Base installation directory path. |
| `app_port` | `8082` | Exposed host port for external web access. |
| `container_name` | `construction-dashboard` | Docker container identifier. |
| `docker_image` | `localhost:5000/construction-dashboard` | Published container image repository. |
| `image_tag` | `stable` | Default image tag if not passed via CLI. |
| `spring_profile` | `dev` | Active Spring profile (`dev` with H2 DB for demo). |
| `app_version` | `1.0.0` | Semantic application version recorded in `app.env`. |
| `java_opts` | `-Xms128m -Xmx256m` | JVM memory limits passed to container. |
| `timezone` | `Asia/Kolkata` | Host timezone configuration. |
| `firewall_allowed_ports` | `['22/tcp', '8082/tcp']` | Inbound ports allowed through UFW. |

---

## 4. Playbooks & Usage Guide

Always run commands from the `ansible/` directory on the control node:

### 1. Provisioning & Application Deployment (`playbooks/provision.yml`)
Provisions the clean target node, applies firewall and Docker configuration, pulls the specified container release, configures systemd, and performs a live health check gate.

```bash
# Deploy a specific release
ansible-playbook -i inventory/hosts.yml playbooks/provision.yml -e release_tag=0.0.1-SNAPSHOT-b2-f3cac90
```

### 2. Live Health & Readiness Verification (`playbooks/healthcheck.yml`)
Audits target service status, container status, HTTP `/health` response (200 OK + `{"status":"UP"}`), login page version badge, disk usage, and memory availability.

```bash
ansible-playbook -i inventory/hosts.yml playbooks/healthcheck.yml
```

### 3. Manual Rollback Playbook (`playbooks/rollback.yml`)
Safely reverts the running application to the previously deployed release recorded in `previous.txt`. Requires explicit confirmation to prevent accidental rollbacks.

```bash
ansible-playbook -i inventory/hosts.yml playbooks/rollback.yml -e confirm_rollback=yes
```

### 4. Release Audit & Status (`playbooks/status.yml`)
Displays the currently active release (`current.txt`), the previous release (`previous.txt`), and the full timestamped deployment history log (`history.log`).

```bash
ansible-playbook -i inventory/hosts.yml playbooks/status.yml
```

---

## 5. Automatic Rollback Mechanism

The `deploy` role implements an atomic deployment workflow with Ansible `block` / `rescue`:
1. **Pre-flight**: Records the currently active release (`pre_deploy_tag`).
2. **Deploy Block**:
   - Pulls target container image.
   - Updates `app.env` and systemd unit file with new release tag.
   - Restarts application container service.
   - Polls `/health` endpoint up to 25 retries with 5s delay.
   - Verifies version badge on `/login`.
   - On success, updates `previous.txt`, `current.txt`, and logs deployment to `history.log`.
3. **Rescue Block**:
   - Triggered automatically if `/health` fails or times out.
   - Reverts `app.env` and systemd unit to `pre_deploy_tag`.
   - Restarts service with previous stable release.
   - Verifies health check returns to UP.
   - Records rollback event in `history.log`.
   - Fails the playbook with an informative alert for the operator.

---

## 6. Code Quality & Linting

Verify YAML and Ansible syntax cleanliness:
```bash
# Validate Ansible playbooks and roles against production rules
ansible-lint

# Syntax check all playbooks
ansible-playbook -i inventory/hosts.yml playbooks/provision.yml --syntax-check
ansible-playbook -i inventory/hosts.yml playbooks/healthcheck.yml --syntax-check
ansible-playbook -i inventory/hosts.yml playbooks/rollback.yml --syntax-check
ansible-playbook -i inventory/hosts.yml playbooks/status.yml --syntax-check
```
