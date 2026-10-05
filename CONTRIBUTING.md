# Contributing Guidelines

Thank you for contributing to the **Construction Progress Dashboard** project! Please adhere to the following workflow and standards to maintain high software quality and clean git history.

---

## 🔄 Contribution Workflow

1. **Find or Open an Issue**: Ensure an issue exists for your task, feature, or bug on GitHub.
2. **Branch from `develop`**:
   - Features: `git checkout -b feature/<issue-number>-<short-description> develop`
   - Bug fixes: `git checkout -b bugfix/<issue-number>-<short-description> develop`
3. **Develop & Verify Locally**:
   - Write clean, well-tested code following repository architecture.
   - Run `./mvnw clean test` (or `.\mvnw.cmd clean test`) to ensure all tests pass.
   - Ensure the app packages cleanly with `./mvnw clean package`.
4. **Commit Following the Convention**: Write atomic, structured commits (see below).
5. **Open a Pull Request**: Submit your PR targeting the `develop` branch. Never target `main` directly.
6. **Code Review**: At least one peer review approval is required before merging.
7. **Branch Deletion**: Delete your feature/bugfix branch after merging.

---

## ✍️ Commit Message Convention

All commits must follow the **Conventional Commits** format:

```text
<type>(<scope>): <short description in present tense>
```

- **Allowed Types**:
  - `feat`: A new user-facing feature or API endpoint
  - `fix`: A bug fix
  - `docs`: Documentation changes only (e.g., README, architecture guides)
  - `chore`: Build tools, dependencies, or repository maintenance tasks
  - `test`: Adding missing tests or correcting existing tests
  - `refactor`: Code restructuring without modifying behavior
  - `ci`: CI/CD pipeline scripts or configuration changes
- **Examples**:
  - `feat(progress): add site event logging form`
  - `fix(controller): resolve null pointer on empty task date`
  - `docs(readme): add setup instructions`

---

## ✅ Pull Request Checklist

Before marking your PR as ready for review, verify:

- [ ] PR title adheres to the Conventional Commits format.
- [ ] Description references the linked issue (e.g., `Closes #12`).
- [ ] Code compiles and passes all unit and integration tests (`./mvnw clean test`).
- [ ] Application packages successfully into WAR (`./mvnw clean package`).
- [ ] Acceptance criteria from the issue description are met.
- [ ] Documentation, code comments, and README are updated where relevant.
- [ ] No hardcoded secrets, tokens, or credentials are introduced.
