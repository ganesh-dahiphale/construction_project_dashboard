# Ansible Configuration Management Guide 🛠️

**Project**: Construction Progress Dashboard  
**Milestone**: Week 13 — Server Configuration & Playbook Architecture  
**Target Roles**: `common`, `docker`, `app_user`, `app_dirs`, `firewall`, `app_config`  

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

Install requirements:
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
├── ansible.cfg                    # Ansible default configuration & privilege escalation
├── requirements.yml               # Required collections (ansible.posix, community.docker)
├── README.md                      # Usage guide and operational instructions
├── inventory/
│   ├── hosts.yml                  # Production target host definition
│   └── hosts.example.yml          # Generic inventory template
├── group_vars/
│   └── app_servers.yml            # Environment parameters, ports, and versions
├── playbooks/
│   └── site.yml                   # Top-level site orchestration playbook
└── roles/
    ├── common/                    # Apt cache, base packages, timezone Asia/Kolkata
    ├── docker/                    # Docker CE upstream repository, packages, daemon.json
    ├── app_user/                  # Dedicated system user (appdeploy) and docker group
    ├── app_dirs/                  # Directories (/opt/construction-dashboard/*)
    ├── firewall/                  # UFW port 22 first, port 8082, default deny inbound
    └── app_config/                # app.env and systemd unit service (enabled, stopped)
```

---

## 3. How to Change Target Node

Edit `inventory/hosts.yml` (or create a custom inventory):

```yaml
---
all:
  children:
    app_servers:
      hosts:
        cpd-target-node:
          ansible_host: <target-ip-or-hostname>
          ansible_port: 22
          ansible_user: <ssh-username>
          ansible_ssh_private_key_file: ~/.ssh/cpd_ansible
```

---

## 4. Configuration Variables (`group_vars/app_servers.yml`)

| Variable | Default Value | Description |
|---|---|---|
| `app_user` | `appdeploy` | Dedicated non-login system user for container execution. |
| `app_group` | `docker` | Group membership allowing container management. |
| `app_dir` | `/opt/construction-dashboard` | Base installation directory path. |
| `app_port` | `8082` | Exposed host port for external web access. |
| `container_name` | `construction-dashboard` | Docker container identifier. |
| `docker_image` | `ganesh/construction-dashboard` | Published Docker Hub container image repository. |
| `image_tag` | `stable` | Image tag to be deployed in Week 14. |
| `spring_profile` | `dev` | Active Spring profile (`dev` with H2 DB for demo). |
| `app_version` | `1.0.0` | Semantic application version recorded in `app.env`. |
| `java_opts` | `-Xms128m -Xmx256m` | JVM memory limits passed to container. |
| `timezone` | `Asia/Kolkata` | Host timezone configuration. |
| `firewall_allowed_ports` | `['22/tcp', '8082/tcp']` | Inbound ports allowed through UFW. |

---

## 5. Playbook Execution Commands

Always run commands from the `ansible/` directory on the control node:

### 1. Connectivity Ping
```bash
ansible app_servers -i inventory/hosts.yml -m ping
```

### 2. Syntax Check
```bash
ansible-playbook -i inventory/hosts.yml playbooks/site.yml --syntax-check
```

### 3. Dry-Run Mode (Check & Diff)
```bash
ansible-playbook -i inventory/hosts.yml playbooks/site.yml --check --diff
```

### 4. Full Execution
```bash
ansible-playbook -i inventory/hosts.yml playbooks/site.yml -v
```

### 5. Running by Specific Role Tags
Each role is assigned a dedicated tag:
```bash
# Run only firewall configuration
ansible-playbook -i inventory/hosts.yml playbooks/site.yml --tags firewall

# Run only docker and app_config
ansible-playbook -i inventory/hosts.yml playbooks/site.yml --tags docker,config
```

Available tags:
- `common`: OS updates, base packages, and timezone
- `docker`: Docker CE repository, daemon.json, and engine installation
- `user`: System user `appdeploy` and group membership
- `dirs`: Runtime directory structure and permissions
- `firewall`: UFW firewall rules and enforcement
- `config`: Environment file `app.env` and systemd unit file

---

## 6. Code Quality & Linting

Verify YAML and Ansible syntax cleanliness:
```bash
# Validate YAML formatting
yamllint ansible

# Validate Ansible best practices
ansible-lint ansible/playbooks/site.yml
```

---

## 7. Target Node Post-Run Verification

Inspect target node over SSH to verify configured state:
```bash
ssh -i ~/.ssh/cpd_ansible ganesh@<target-ip> "
  docker --version &&
  systemctl is-active docker &&
  systemctl is-enabled construction-dashboard &&
  systemctl is-active construction-dashboard &&
  id appdeploy &&
  ls -ld /opt/construction-dashboard /opt/construction-dashboard/* &&
  sudo ufw status verbose &&
  cat /etc/docker/daemon.json &&
  cat /opt/construction-dashboard/config/app.env
"
```
