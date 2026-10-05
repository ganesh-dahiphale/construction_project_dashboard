# Git Branching Strategy & Workflow Policy 🌿

This document defines the branching strategy, release process, and merge guidelines for the **Construction Progress Dashboard** project. We adopt a structured Gitflow model designed to facilitate collaborative teamwork, continuous integration, and safe deployments.

---

## 🏛️ Core Branches

### 1. `main` (Production)
- **Purpose**: Represents the stable, production-ready codebase.
- **Access Policy**: **Protected branch**. Direct pushes and commits to `main` are strictly forbidden.
- **Incoming Changes**: Arrive exclusively via Pull Requests from `release/*` branches or `hotfix/*` branches (or `develop` for milestone releases).
- **Tagging**: Every merge to `main` is tagged with a semantic version number (e.g., `v1.0.0`).

### 2. `develop` (Integration)
- **Purpose**: Serves as the primary integration and staging branch. Contains tested features ready for the next release.
- **Access Policy**: **Protected branch**. Direct commits to `develop` are prohibited.
- **Incoming Changes**: Arrive strictly via Pull Requests from `feature/*` and `bugfix/*` branches.
- **CI Trigger**: Every push and PR merge to `develop` triggers an automated build and test pipeline in Jenkins.

---

## 🔀 Supporting & Working Branches

| Branch Type | Base Branch | Merges Into | Naming Convention | Example |
|---|---|---|---|---|
| **Feature** | `develop` | `develop` | `feature/<issue-number>-<short-description>` | `feature/12-task-entry-form` |
| **Bugfix** | `develop` | `develop` | `bugfix/<issue-number>-<short-description>` | `bugfix/34-status-dropdown-fix` |
| **Hotfix** | `main` | `main` & `develop` | `hotfix/<short-description>` | `hotfix/h2-driver-load-error` |
| **Release** | `develop` | `main` & `develop` | `release/<version>` | `release/v1.0.0` |

### Detailed Branch Descriptions

#### Feature Branches (`feature/...`)
- Used for developing new capabilities, views, or endpoints.
- Always branch off from the latest `develop`:
  ```bash
  git checkout develop
  git pull origin develop
  git checkout -b feature/12-task-entry-form
  ```
- Merged back into `develop` via a GitHub Pull Request once unit tests and acceptance criteria pass.

#### Bugfix Branches (`bugfix/...`)
- Used for addressing non-critical issues, styling defects, or test failures found in `develop`.
- Always branch off from `develop` and merge back into `develop` via PR.

#### Hotfix Branches (`hotfix/...`)
- Reserved for critical production bugs that cannot wait for a regular release cycle.
- Branch directly off `main`:
  ```bash
  git checkout main
  git pull origin main
  git checkout -b hotfix/connection-timeout
  ```
- Once validated, hotfix branches are merged into **both** `main` (with a patch version tag) and `develop` to prevent regressions.

#### Release Branches (`release/...`)
- Created when `develop` reaches a release milestone or end of sprint.
- Only documentation updates, minor bug fixes, and version bumps occur on this branch.
- Merged into `main` (and tagged) and back into `develop`.

---

## 🛡️ Merge & Governance Rules

1. **No Direct Commits**: Developers must never commit directly to `main` or `develop`.
2. **Pull Requests Required**: All merges must occur through GitHub Pull Requests.
3. **Mandatory Code Review**: Every PR must be reviewed and approved by at least one other team member.
4. **Automated Status Checks**: PR builds must compile with `./mvnw clean package` and all automated tests must pass.
5. **Clean Branch Hygiene**: Working branches (`feature/*`, `bugfix/*`) must be deleted immediately after their PR is successfully merged.

---

## ✍️ Commit Message Convention

Commits must follow the Conventional Commits specification:

```text
type(scope): short message in present tense
```

### Allowed Types
- **`feat`**: Introduces a new feature or endpoint (e.g., `feat(api): add task completion endpoint`)
- **`fix`**: Patches a bug or defect (e.g., `fix(ui): correct alert modal z-index`)
- **`docs`**: Changes to documentation only (e.g., `docs(readme): add setup instructions`)
- **`chore`**: Maintenance, build tool, or dependency updates (e.g., `chore(deps): update h2 version`)
- **`test`**: Adding new tests or fixing broken tests (e.g., `test(health): add smoke test`)
- **`refactor`**: Code cleanup without changing existing behavior (e.g., `refactor(service): simplify date logic`)
- **`ci`**: Updates to CI/CD pipelines and automation scripts (e.g., `ci(jenkins): add build stage`)

### Best Practices
- Use lowercase imperative verbs: "add", "fix", "update", "remove" (not "added", "fixes", "updating").
- Keep the subject line under 72 characters.
- Reference the issue number in the PR or commit body when appropriate.
