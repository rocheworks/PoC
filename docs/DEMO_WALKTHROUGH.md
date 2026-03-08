# Demo Walkthrough: Development Standards Automation

This document provides a step-by-step walkthrough for demonstrating the automated
development standards verification system to leadership.

---

## Overview

This demo shows how **two complementary systems** work together to enforce
development standards from the checklist:

1. **GitHub Copilot Instruction Digest** (`.github/copilot-instructions.md`)
   - Loaded automatically by GitHub Copilot in the IDE (IntelliJ / VS Code)
   - Guides the developer in real-time as they write code
   - Covers all checklist items that can be addressed during development

2. **GitHub Actions Workflow** (`.github/workflows/pr-checklist-verification.yml`)
   - Runs automatically when a PR is raised
   - Enforces checklist items that can be verified programmatically
   - Blocks the PR merge if any check fails
   - Posts detailed feedback as PR comments

---

## Demo Scenario: Adding `middle_name` Field to WebUser

### Step 1: Developer Opens IDE with Copilot

1. Open the project in IntelliJ IDEA (with GitHub Copilot plugin installed)
2. GitHub Copilot automatically loads `.github/copilot-instructions.md`
3. The instructions are now active and will guide the developer

### Step 2: Developer Starts Adding a New Field

1. Open `src/main/java/com/demo/devstandards/model/WebUser.java`
2. Start typing to add a new field: `middle_name`
3. **GitHub Copilot will suggest** adding `@Indexed` annotation along with `@Field`
   because the instruction digest (Section 17 - INDEX REQUIREMENT) explicitly states:
   > "When adding a new field to a MongoDB/DocumentDB document model... you MUST add an @Indexed annotation"

4. Copilot will also suggest updating:
   - `IndexInitializer.java` (programmatic index creation)
   - `docs/INDEX_DOCUMENTATION.md` (index documentation)
   - `scripts/index-registry.json` (index registry)

### Step 3: Developer Makes Changes (Correct Way)

Add the following to `WebUser.java`:
```java
@Field("middle_name")
@Indexed
private String middleName;
```

Add to `IndexInitializer.java`:
```java
mongoTemplate.indexOps(COLLECTION_NAME)
    .ensureIndex(new Index().on("middle_name", Sort.Direction.ASC).named("idx_middle_name"));
```

Update `docs/INDEX_DOCUMENTATION.md` with new row in the table.

Update `scripts/index-registry.json` with new field entry.

### Step 4: Developer Raises PR

1. Commit and push changes to a feature branch
2. Create a Pull Request against `main`

### Step 5: GitHub Actions Workflow Runs

The workflow automatically:
1. **Builds** the project (compile check)
2. **Detects** that `WebUser.java` was modified and a new field was added
3. **Verifies** that:
   - `@Indexed` annotation exists on the new field
   - `IndexInitializer.java` has `ensureIndex()` for the new field
   - `docs/INDEX_DOCUMENTATION.md` mentions the new field
   - `scripts/index-registry.json` has the field registered
4. **Posts a comment** on the PR with the verification results
5. **Blocks or approves** the merge based on results

### Step 6: If Checks Fail (Demo the Failure Case)

To demonstrate what happens when a developer forgets to add an index:

1. Add `middle_name` field WITHOUT `@Indexed` annotation
2. Push and create PR
3. The workflow will:
   - **FAIL** the index verification
   - **Post a comment** explaining exactly what's missing
   - **Block the merge** until issues are fixed
   - The developer sees a clear message: "Field 'middle_name' is MISSING @Indexed annotation"

### Step 7: Developer Fixes and Re-pushes

1. Developer adds the missing `@Indexed` annotation
2. Developer adds index to `IndexInitializer.java`
3. Developer updates documentation
4. Push to the same branch
5. Workflow re-runs and **passes**
6. PR can now be merged

---

## Local Mock CI/CD Demonstration

To demonstrate the verification locally (without GitHub Actions):

```bash
# From the project root directory:

# 1. Make the verification script executable
chmod +x scripts/verify-indexes.sh

# 2. Make your changes (e.g., add middle_name field)

# 3. Stage and commit your changes
git add -A
git commit -m "feat: add middle_name field to WebUser"

# 4. Run the verification script
bash scripts/verify-indexes.sh
```

The script will output PASS/FAIL for each check, just as the GitHub Actions workflow would.

---

## Integration with Existing CI/CD Pipeline

To integrate this into an existing EKS CI/CD pipeline:

1. **Copy** `.github/workflows/pr-checklist-verification.yml` to the existing workflow directory
2. **Copy** `scripts/verify-indexes.sh` and `scripts/index-registry.json` to the repo
3. **Add** `docs/INDEX_DOCUMENTATION.md` to the repo
4. **Configure** branch protection rules to require the `PR Merge Gate` check to pass
5. The workflow is self-contained and does not require additional infrastructure

### For Jenkins/Other CI Systems

The core logic is in `scripts/verify-indexes.sh` which is a standalone bash script.
It can be called from any CI system:

```yaml
# Example Jenkins stage
stage('Index Verification') {
    steps {
        sh 'bash scripts/verify-indexes.sh'
    }
}
```

```yaml
# Example AWS CodeBuild buildspec
phases:
  build:
    commands:
      - bash scripts/verify-indexes.sh
```
