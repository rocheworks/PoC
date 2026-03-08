# Development Standards Automation Demo

A Spring Boot + Maven + DocumentDB project that demonstrates automated enforcement of
development standards via **GitHub Copilot** (IDE-time guidance) and **GitHub Actions**
(PR-time verification).

## Project Structure

```
devstandards-demo/
├── .github/
│   ├── copilot-instructions.md          # GitHub Copilot instruction digest
│   └── workflows/
│       └── pr-checklist-verification.yml # GitHub Actions PR verification
├── docs/
│   ├── DEMO_WALKTHROUGH.md              # Step-by-step demo guide
│   └── INDEX_DOCUMENTATION.md           # Index documentation (checklist item)
├── scripts/
│   ├── demo-simulate-pr.sh             # Local mock CI/CD demo script
│   ├── index-registry.json             # Index registry for verification
│   └── verify-indexes.sh               # Core verification logic
├── src/main/java/com/demo/devstandards/
│   ├── DevstandardsDemoApplication.java # Spring Boot entry point
│   ├── config/
│   │   └── DocDbMongoConfig.java        # DocumentDB TLS connection config
│   ├── model/
│   │   └── WebUser.java                 # WebUser document model
│   ├── repository/
│   │   └── WebUserRepository.java       # Data access layer
│   ├── runner/
│   │   └── WebUserPrintRunner.java      # Prints first 3 WebUser rows
│   └── index/
│       └── IndexInitializer.java        # Programmatic index creation
├── src/main/resources/
│   └── application.properties           # Application configuration
├── src/test/java/...                    # Unit tests
└── pom.xml                              # Maven build configuration
```

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Access to AWS DocumentDB (or local MongoDB for testing)

### Build
```bash
mvn clean compile
```

### Run (requires DocumentDB/MongoDB connection)
```bash
# Set environment variables
export DOCDB_USERNAME=your_username
export DOCDB_PASSWORD=your_password
export DOCDB_HOST=your-cluster.docdb.amazonaws.com
export DOCDB_PORT=27017
export DOCDB_DATABASE=your_database
export DOCDB_CA_FILE=/path/to/global-bundle.pem

mvn spring-boot:run
```

### Run Tests
```bash
mvn test
```

## Demo: Mock CI/CD Pipeline

### Simulate a FAILING PR (developer forgot to add index)
```bash
chmod +x scripts/demo-simulate-pr.sh
./scripts/demo-simulate-pr.sh fail
```

### Simulate a PASSING PR (developer added all required indexes)
```bash
./scripts/demo-simulate-pr.sh pass
```

## How It Works

### 1. GitHub Copilot Instructions (IDE-Time)
The file `.github/copilot-instructions.md` is automatically loaded by GitHub Copilot
when a developer opens this project. It contains all 38 development standards from the
checklist, formatted as instructions that Copilot uses to guide code suggestions.

**Key demo point**: When a developer starts adding a new field like `middle_name` to
`WebUser.java`, Copilot will suggest including `@Indexed` annotation and remind about
updating `IndexInitializer.java` and documentation.

### 2. GitHub Actions Workflow (PR-Time)
The workflow `.github/workflows/pr-checklist-verification.yml` runs on every PR that
touches model files, index files, or documentation. It:

1. Detects new fields added to model classes
2. Verifies `@Indexed` annotation exists
3. Verifies programmatic index creation in `IndexInitializer.java`
4. Verifies index documentation in `docs/INDEX_DOCUMENTATION.md`
5. Verifies index registry in `scripts/index-registry.json`
6. Posts results as PR comments
7. Blocks merge if any check fails

### 3. Integration with Existing CI/CD
The verification logic is in a standalone bash script (`scripts/verify-indexes.sh`)
that can be called from any CI system (GitHub Actions, Jenkins, AWS CodeBuild, etc.).
See `docs/DEMO_WALKTHROUGH.md` for integration examples.

## Checklist Items Covered

| # | Checklist Item | Covered By |
|---|---------------|------------|
| 1 | New fields have necessary indexes | Copilot + Actions |
| 2 | Indexes added by code or pipeline | Copilot + Actions |
| 3 | Indexes documented | Copilot + Actions |

## Configuration for Your Repository

1. Copy `.github/copilot-instructions.md` to your repository's `.github/` directory
2. Copy `.github/workflows/pr-checklist-verification.yml` to your workflows
3. Copy `scripts/verify-indexes.sh` and `scripts/index-registry.json` to your repo
4. Copy `docs/INDEX_DOCUMENTATION.md` template to your repo
5. Configure branch protection rules to require the `PR Merge Gate` status check
