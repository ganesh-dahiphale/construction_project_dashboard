# Week 6 Lab Evidence: Parallel Feature Development, Merge Conflict Resolution & MVP Baseline v0.2.0

**Student / Maintainer:** Ganesh Dahiphale  
**Course:** DevOps (Civil / Construction Management Track)  
**Project:** Construction Progress Dashboard (Spring Boot 3, Java 17, Thymeleaf, JPA)  
**Date:** 2026-10-05  

---

## 1. GitHub Issues Created

- **Feature A Issue #7**: [Searchable dashboard with summary indicators](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/7)
- **Feature B Issue #8**: [Status drill-down and alert/exception view](https://github.com/ganesh-dahiphale/construction_project_dashboard/issues/8)

---

## 2. Parallel Feature Branches (Created from same commit `c90f7c7`)

- **Branch A**: `feature/7-searchable-dashboard`
- **Branch B**: `feature/8-drilldown-alerts`

```bash
git branch feature/7-searchable-dashboard c90f7c7
git branch feature/8-drilldown-alerts c90f7c7
```

---

## 3. Pull Requests & Reviews

| PR Number | Branch | Target | Description | Status |
|---|---|---|---|---|
| **[#9](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/9)** | `feature/7-searchable-dashboard` | `develop` | Feature A: Executive Dashboard & Multi-criteria Search | **Merged** |
| **[#10](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/10)** | `feature/8-drilldown-alerts` | `develop` | Feature B: Status Drill-down & Overdue/Blocked Alert View | **Merged (Conflict Resolved)** |
| **[#11](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/11)** | `chore/release-v0.2.0` | `develop` | Release documentation & CHANGELOG.md | **Merged** |
| **[#12](https://github.com/ganesh-dahiphale/construction_project_dashboard/pull/12)** | `develop` | `main` | Promotion to main for MVP Release v0.2.0 | **Merged** |

---

## 4. Conflict Evidence on Pull Request #10 (Feature B vs. develop)

GitHub CLI detected conflict on PR #10 after Feature A was merged into `develop`:

```json
{
  "mergeStateStatus": "DIRTY",
  "mergeable": "CONFLICTING",
  "number": 10,
  "title": "feat: status drill-down and alert/exception view (#8)"
}
```

### Pre-Resolution Local Merge Output

```text
$ git checkout feature/8-drilldown-alerts
$ git merge develop
Auto-merging README.md
CONFLICT (content): Merge conflict in README.md
Auto-merging src/main/resources/templates/fragments/navbar.html
CONFLICT (content): Merge conflict in src/main/resources/templates/fragments/navbar.html
Automatic merge failed; fix conflicts and then commit the result.
```

### Unmerged Paths

```text
$ git diff --name-only --diff-filter=U
README.md
src/main/resources/templates/fragments/navbar.html
```

### Raw Conflict Markers in `navbar.html`

```html
<<<<<<< HEAD
                    <a class="nav-link" th:classappend="${activeTab == 'alerts' ? 'active' : ''}" th:href="@{/alerts}">
                        <i class="bi bi-bell me-1"></i>Alerts
=======
                    <a class="nav-link" th:classappend="${activeTab == 'dashboard' ? 'active' : ''}" th:href="@{/dashboard}">
                        <i class="bi bi-speedometer2 me-1"></i>Dashboard
>>>>>>> develop
```

### Raw Conflict Markers in `README.md`

```markdown
<<<<<<< HEAD
- Status drill-down and alert view
=======
- Searchable dashboard with summary indicators
>>>>>>> develop
```

---

## 5. Conflict Resolution Details

### Conflict Resolution Commit
- **Commit Hash:** `3878eb8`
- **Commit Message:** `merge: resolve navbar and README conflicts with develop (#8)`

### Resolved `navbar.html`
Both navigation links (`Dashboard` and `Alerts`) along with `Status` were preserved and structured hierarchically:
```html
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${activeTab == 'home' ? 'active' : ''}" th:href="@{/}">
                        <i class="bi bi-house-door me-1"></i>Home
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${activeTab == 'dashboard' ? 'active' : ''}" th:href="@{/dashboard}">
                        <i class="bi bi-speedometer2 me-1"></i>Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${activeTab == 'tasks' ? 'active' : ''}" th:href="@{/tasks}">
                        <i class="bi bi-list-task me-1"></i>Tasks
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${activeTab == 'status' ? 'active' : ''}" th:href="@{/status}">
                        <i class="bi bi-diagram-3 me-1"></i>Status
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:classappend="${activeTab == 'alerts' ? 'active' : ''}" th:href="@{/alerts}">
                        <i class="bi bi-bell me-1"></i>Alerts
                    </a>
                </li>
```

### Resolved `README.md`
Both feature bullets were preserved in sequence:
```markdown
- Searchable dashboard with summary indicators
- Status drill-down and alert view
```

---

## 6. Automated Test Suite Execution

All **27 tests** passed cleanly on the merged codebase:

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 7. Git Log Graph Across All Branches

```text
*   802931f (tag: v0.2.0, origin/main, main) Merge pull request #12 from ganesh-dahiphale/develop
|\  
| *   11809d7 (HEAD -> develop, origin/develop, origin/HEAD) Merge pull request #11 from ganesh-dahiphale/chore/release-v0.2.0
| |\  
| | * 87bb796 (origin/chore/release-v0.2.0) chore(release): prepare v0.2.0 release documentation and changelog
| |/  
| *   a641866 Merge pull request #10 from ganesh-dahiphale/feature/8-drilldown-alerts
| |\  
| | *   3878eb8 merge: resolve navbar and README conflicts with develop (#8)
| | |\  
| | |/  
| |/|   
| * |   53ccf2c Merge pull request #9 from ganesh-dahiphale/feature/7-searchable-dashboard
| |\ \  
| | * | a11a121 docs(readme): features (#7)
| | * | 84207c5 test(dashboard): tests (#7)
| | * | e1d8f4a feat(web): dashboard page and filter form (#7)
| | * | 3fd3125 feat(repository): search query (#7)
| | * | f3f28e3 feat(service): summary indicators (#7)
| |/ /  
| | * 7360eff docs(readme): features (#8)
| | * 9b7d9cf test(alerts): tests (#8)
| | * ac48ef4 feat(web): alerts page (#8)
| | * 231c50d feat(web): status drill-down (#8)
| | * 91f4b3e feat(service): alert rules (#8)
| |/  
| * c90f7c7 Merge pull request #6 from ganesh-dahiphale/feature/5-task-entry-form
|/| 
| * c4c2845 fix(review): address PR review comments on validation, test coverage and seed guard (#5)
| * c9a24a9 docs(readme): document the task entry feature (#5)
| * 3f5d4e7 test(task): add service and controller tests (#5)
| * 5657304 chore(dev): add sample seed data for dev profile (#5)
| * 374f544 feat(web): add task entry form with validation (#5)
| * 1c9e4df feat(web): add task controller and list page (#5)
| * 109ee70 feat(service): add task service (#5)
| * 5d0d7af feat(repository): add project and task repositories (#5)
| * 78181d4 feat(model): add Project and Task entities with validation (#5)
|/  
* 131dc60 chore: add issue and pull request templates
* 6fab555 docs: add branch policy
* 152df2d test: add health endpoint smoke test
* fd11f9c feat(health): add health check endpoint and home page
```

---

## 8. Release & Git Tag Information

- **Tag:** `v0.2.0`
- **Release URL:** [https://github.com/ganesh-dahiphale/construction_project_dashboard/releases/tag/v0.2.0](https://github.com/ganesh-dahiphale/construction_project_dashboard/releases/tag/v0.2.0)
- **Tag Annotation:** `MVP baseline: entry, dashboard, indicators, drill-down, alerts`

```text
$ git tag -n
v0.2.0          MVP baseline: entry, dashboard, indicators, drill-down, alerts
```

---

## 9. Final Repository Status

```text
$ git status
On branch develop
Your branch is up to date with 'origin/develop'.
nothing to commit, working tree clean
```
