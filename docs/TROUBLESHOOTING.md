# Troubleshooting Guide & Operational Knowledge Base 🛠️

This knowledge base documents the real-world operational issues, build failures, configuration gotchas, and deployment anomalies encountered throughout Weeks 4 through 15 of the **Construction Progress Dashboard** project lifecycle, along with root cause analyses, exact resolutions, and verification commands.

---

## 📋 Comprehensive Troubleshooting Matrix (Weeks 4 – 15)

| # | Milestone | Symptom / Error Message | Root Cause Analysis | Remediation Applied | Verification Command |
|---|---|---|---|---|---|
| **1** | **Week 6** | `CONFLICT (content): Merge conflict in README.md` and `navbar.html` during `git merge develop` on PR #10 | Parallel feature branches `feature/7-searchable-dashboard` and `feature/8-drilldown-alerts` branched from same commit `c90f7c7` and both modified navbar links and documentation. | Resolved conflicts manually by keeping both navigation links and unifying project documentation; committed and pushed to PR branch. | `git diff develop...feature/8-drilldown-alerts` & `gh pr view 10` |
| **2** | **Week 7** | `HTTP 302 Found` redirecting to `/login` when polling synthetic health probe `/health` | Spring Security 6 default filter chain secured all HTTP routes, blocking automated container health monitors and unauthenticated monitors. | Configured `.requestMatchers("/health", "/login", "/css/**", "/js/**", "/images/**").permitAll()` in `SecurityConfig.java`. | `curl -i http://localhost:8080/health` (returns HTTP 200 with `{"status":"UP"}`) |
| **3** | **Week 7** | `Whitelabel Error Page (status=403)` when Site Engineer accesses `/admin/projects` | Role-Based Access Control (RBAC) correctly restricted project management routes to `ROLE_ADMIN`, but user saw default error. | Built custom Thymeleaf 403 error view (`src/main/resources/templates/error/403.html`) with clear user navigation back to dashboard. | `curl -u engineer:engineer123 http://localhost:8080/admin/projects` |
| **4** | **Week 8** | `ParameterBindingException` or unrecognized argument when running `./mvnw -Dbase.url=...` in Windows PowerShell | PowerShell parses `-D` as a PowerShell parameter switch rather than passing it verbatim to the Java executable. | Wrapped Maven wrapper invocations via `cmd.exe /c ".\mvnw.cmd -B ..."` or passed arguments with quotes. | `cmd.exe /c ".\mvnw.cmd -version"` |
| **5** | **Week 8** | Tomcat 10 deployment failure with `ClassNotFoundException: javax.servlet.Filter` | Spring Boot 3 migrated to Jakarta EE 10 (`jakarta.servlet.*`), which is incompatible with legacy Tomcat 9 (`javax.servlet.*`). | Configured modern Apache Tomcat 10.1.x with Jakarta Servlet 6 support using `cargo-maven3-plugin` container definition. | `curl -s http://localhost:8081/construction-dashboard/health` |
| **6** | **Week 9** | Selenium test timing out with `NoSuchElementException` or premature submission on task form | Headless Chrome executed form inputs faster than JavaScript DOM event dispatchers could process dynamic change events. | Enhanced `TaskFormPage` to dispatch synthetic `input` and `change` events, and added explicit `WebDriverWait` for URL stabilization. | `./mvnw -B -Pselenium test` |
| **7** | **Week 10** | Headless Chrome crash in Jenkins CI: `SessionNotCreatedException: DevToolsActivePort file doesn't exist` | Google Chrome cannot initialize GPU hardware acceleration or create sandboxed user directories inside background CI service sessions. | Configured `ChromeOptions` with `--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, and `--disable-gpu` in `WebDriverFactory`. | Jenkins build console output for Stage 5 showing green test suite execution |
| **8** | **Week 11** | Permission denied inside container: `AccessDeniedException: /app/dashboard.war` | Dockerfile copied WAR file under `root:root` ownership, but runtime switched to non-root user `appuser` (UID 10001). | Added `--chown=appuser:appgroup` flag to `COPY` statement in multi-stage Dockerfile and verified runtime user rights. | `docker run --rm <image> whoami` (returns `appuser`) |
| **9** | **Week 12** | Jenkins service fails with `docker: command not found` or `error during connect: open //./pipe/docker_engine: Access is denied` | Jenkins Windows Service runs under `NT AUTHORITY\SYSTEM`, which lacks access to the interactive user's Docker Desktop named pipe. | Configured Docker Desktop with unencrypted daemon on `tcp://localhost:2375` and set `DOCKER_HOST=tcp://localhost:2375` in Jenkins system environment. | Run `docker version` inside Jenkins job workspace |
| **10** | **Week 13** | Ansible check mode (`--check --diff`) fails at `docker : Install Docker Engine packages` | In check mode, `apt_repository` task checks without committing `/etc/apt/sources.list.d/docker.list` to disk, leaving apt cache without Docker packages. | Documented that this is expected behavior during check mode against an unconfigured node; live execution resolves immediately. | `ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml` |
| **11** | **Week 14** | `Host key verification failed` during automated Ansible execution | Fresh target VM SSH host key was not recognized in control node's `known_hosts` file, prompting for interactive confirmation. | Set `ANSIBLE_HOST_KEY_CHECKING=False` in `ansible/ansible.cfg` and passed `StrictHostKeyChecking=no` in SSH configurations. | `ansible app_servers -i ansible/inventory/hosts.yml -m ping` (returns `pong`) |
| **12** | **Week 14** | SSH lockout risk when provisioning UFW firewall on target node | Enabling UFW before allowing SSH port 22 immediately severs the Ansible SSH control connection. | Structured `firewall` role to explicitly allow port `22/tcp` first, allow application port `8082/tcp` second, and only then enable UFW service. | `sudo ufw status verbose` on target node |
| **13** | **Week 14** | Health check timeout when deploying defective test container (`broken-demo`) | Test image misconfigured container port to 9999 instead of 8080, causing `/health` probe on port 8082 to fail. | Ansible `deploy` role `rescue` block caught failure, restored `previous.txt` release tag, restarted container, and restored service. | `sudo tail -n 5 /opt/construction-dashboard/releases/history.log` |
| **14** | **Week 15** | Ansible roles lookup failure: `ERROR! the role 'common' was not found` when invoking from repo root | By default, Ansible searches for `roles/` in the playbook directory (`ansible/playbooks/roles/`) rather than the repository root. | Set `ANSIBLE_ROLES_PATH=ansible/roles` and `ANSIBLE_CONFIG=ansible/ansible.cfg` in Jenkinsfile and wrapper scripts (`deploy-node.sh`/`.ps1`). | `ANSIBLE_ROLES_PATH=ansible/roles ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/healthcheck.yml` |
| **15** | **Week 15** | PowerShell conditional check `-like` returning array instead of boolean | When parsing multi-line console output from `curl`, `$output -like "*pattern*"` filters array lines rather than returning a boolean `$true`. | Joined output lines into a single scalar string: `($output -join "`n") -like "*pattern*"`. | `($out -join "`n") -like "*UP*"` returns `True` |

---

## 🔍 Detailed Incident Breakdowns & Diagnostic Runbooks

### Incident A: Selenium CI Quality Gate Headless Chrome Crash (Week 10)
- **Diagnostic Command**:
  ```bash
  cmd.exe /c ".\mvnw.cmd -B -Pselenium test -Dheadless=true"
  ```
- **Error Log Pattern**:
  ```text
  org.openqa.selenium.SessionNotCreatedException: Could not start a new session.
  Possible causes are invalid address of the remote server or browser start-up failure.
  ```
- **Root Cause**: The default Chrome profile attempts to spawn a graphics pipeline and shared memory segment (`/dev/shm`). In non-interactive CI agents and virtual machines, this causes immediate segmentation faults.
- **Permanent Solution**:
  ```java
  ChromeOptions options = new ChromeOptions();
  options.addArguments("--headless=new");
  options.addArguments("--no-sandbox");
  options.addArguments("--disable-dev-shm-usage");
  options.addArguments("--disable-gpu");
  options.addArguments("--window-size=1920,1080");
  ```

---

### Incident B: Automated Rollback on Port Misconfiguration (Week 14)
- **Diagnostic Command**:
  ```bash
  ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml -e release_tag=broken-demo
  ```
- **Behavior Observed**:
  1. Playbook executes task `deploy : Start application container service`.
  2. Task `deploy : Wait for /health endpoint to report UP` attempts 25 retries (5s delay) and fails.
  3. Execution branches into the `rescue:` block.
  4. Task `deploy : Revert to previous release` reads `releases/previous.txt` (`0.0.1-SNAPSHOT-b1-fe3efe3`).
  5. System restarts container service with previous stable image.
  6. Task `deploy : Verify previous release is healthy` succeeds.
  7. Task `deploy : Log rollback event` writes audit record: `ROLLED BACK from broken-demo to 0.0.1-SNAPSHOT-b1-fe3efe3`.
  8. Playbook terminates with explicit failure to inform CI orchestrator.
- **Verification**:
  ```bash
  curl http://127.0.0.1:8082/health
  # Response: {"status":"UP"}
  ```

---

### Incident C: Ansible Roles Path Boundary in Jenkins Pipeline (Week 15)
- **Symptom**: Jenkins build failed during Stage 8 with:
  ```text
  ERROR! the role 'common' was not found in /var/jenkins_home/workspace/ansible/playbooks/roles
  ```
- **Root Cause**: When executing `ansible-playbook ansible/playbooks/provision.yml`, Ansible searches for roles relative to the playbook location (`ansible/playbooks/roles/`). Because the roles are organized at `ansible/roles/`, Ansible failed to discover them.
- **Fix**: In `Jenkinsfile` and deployment scripts:
  ```groovy
  withEnv(['ANSIBLE_CONFIG=ansible/ansible.cfg', 'ANSIBLE_ROLES_PATH=ansible/roles']) {
      sh 'ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml -e release_tag=${IMAGE_TAG}'
  }
  ```
