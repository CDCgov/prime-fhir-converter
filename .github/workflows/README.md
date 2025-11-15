# CI/CD Workflows Documentation

## Overview

This directory contains GitHub Actions workflows for automated building, testing, security scanning, and publishing of the FHIR Converter library.

## Workflows

### `build-and-publish.yml` - Main CI/CD Pipeline

**Purpose**: Complete DevSecOps pipeline for building, testing, securing, and optionally publishing versioned releases.

**Trigger**: Manual (`workflow_dispatch`)

**Inputs**:

- `version` (required): Semantic version for the build (e.g., `1.0.0`, `2.1.3-beta.1`)
- `publish` (optional): Boolean checkbox to publish artifacts to GitHub Releases (default: `false`)

---

## Pipeline Architecture

```ascii
┌─────────────────────────────────────────────────────────────────┐
│ Stage 0: Validation                                             │
│ ✓ Validate semantic version format                              │
│ ✓ Check for version uniqueness (no existing tags)               │
└─────────────────────────────────────────────────────────────────┘
                              |
    ┌─────────────────────────┴────────────────────────┐
    │                                                  │
┌───┴────────────────────────┐    ┌────────────────────┴──────┐
│ Stage 1a: Build            │    │ Stage 1b: Repo Security   │
│ ✓ Kotlin linting (ktlint)  │    │ ✓ Trivy config scan       │
│ ✓ Compile with version     │    │ ✓ Secrets detection       │
│ ✓ Create Main JAR          │    │ ✓ IaC misconfiguration    │
│ ✓ Create Shadow JAR        │    └───────────────────────────┘
└─────────────┬──────────────┘
              |
    ┌─────────┴──────────────────────────────────┐
    │                                            │
┌───┴──────────────────┐  ┌──────────────────────┴────────┐
│ Stage 2a: Test       │  │ Stage 2b: Security Scans      │
│ ✓ JUnit 5 tests      │  │ ✓ Dependency CVE scan (Trivy) │
│ ✓ JaCoCo coverage    │  │ ✓ Artifact JAR scan (Trivy)   │
└──────────────────────┘  └───────────────────────────────┘
              |
              |  (if publish=true)
              v
┌─────────────────────────────────────────────────────────────────┐
│ Stage 2c: CodeQL SAST (release builds only)                     │
│ ✓ Source code security analysis                                 │
│ ✓ Security-only queries                                         │
└─────────────┬───────────────────────────────────────────────────┘
              |
    ┌─────────┴──────────────────┐
    │                            │
┌───┴────────────────┐  ┌────────┴───────────┐
│ Stage 3a: Publish  │  │ Stage 3b: Summary  │
│ (if enabled)       │  │ ✓ Execution report │
│ ✓ Create release   │  │ ✓ Stage statuses   │
│ ✓ Attach JARs      │  └────────────────────┘
│ ✓ Generate notes   │
└────────────────────┘

Note: All security scan results remain private (workflow logs only)
```

---

## Pipeline Stages

### Stage 0: Validation (1 job)

**Job**: `validate`

Validates the version input before proceeding:

- **Format Check**: Ensures semantic versioning format (`X.Y.Z` or `X.Y.Z-suffix`)
- **Uniqueness Check**: Verifies no existing git tag with that version
- **Fast Fail**: Exits immediately if validation fails

### Stage 1: Parallel Build & Security Scan (2 jobs)

**Jobs**: `build`, `security-scan-repo`

#### `build`

- Sets up Java 17 (Temurin distribution)
- Configures Gradle with `gradle/actions/setup-gradle@v5`
- Runs Kotlin linting (`ktlintCheck`)
- Compiles code with version override: `-Pversion=$VERSION`
- Creates JAR artifacts:
  - Main JAR: `prime-fhir-converter-{version}.jar`
  - Shadow JAR: `prime-fhir-converter-{version}-all.jar` (with dependencies)
- Uploads JARs and build outputs for downstream jobs
- Build outputs (`build/classes/`, `.gradle/`) uploaded for reuse

#### `security-scan-repo`

- Scans repository configuration with Trivy
- Detects:
  - Secrets in code
  - Infrastructure-as-Code misconfigurations
  - Workflow security issues
- Results available in workflow logs only (not publicly exposed)
- Fails on >LOW severity issues

### Stage 2: Testing & Dependency Security (3 jobs)

**Jobs**: `test`, `security-scan-dependencies`, `security-scan-artifacts`

#### `test`

- Downloads build outputs from build job (compiled classes, resources)
- Configures Gradle with `gradle/actions/setup-gradle@v5` (cache-read-only mode)
- Runs JUnit 5 test suite (Gradle skips compilation - UP-TO-DATE)
- Generates JaCoCo code coverage report
- Uploads test results and coverage artifacts
- All tests must pass to proceed
- Reuses compiled code, only runs test tasks (~60-70% faster)

#### `security-scan-dependencies`

- Downloads JAR artifacts from build job
- Scans JARs directly for known CVEs using Trivy
- Checks both direct and transitive dependencies embedded in JARs
- Results available in workflow logs only (not publicly exposed)
- Fails on >LOW vulnerabilities
- No rebuild needed, scans existing JARs

#### `security-scan-artifacts`

- Downloads built JAR artifacts
- Scans JARs for embedded vulnerabilities using Trivy
- Validates artifact integrity
- Fails on >LOW vulnerabilities

#### `codeql-scan` (release-only)

- **Only runs if**: `publish` input is `true` (release builds only)
- Performs static application security testing (SAST) using CodeQL
- Analyzes Java/Kotlin source code for security vulnerabilities
- Uses `security-only` queries (excludes code quality checks)
- Protects against publishing vulnerable code in releases

### Stage 3: Publish & Summary (2 jobs)

**Jobs**: `publish`, `workflow-summary`

#### `publish` (conditional)

- **Only runs if**: `publish` input is `true` AND all previous jobs succeed (including CodeQL scan)
- Creates GitHub Release with tag `v{version}`
- Generates release notes with:
  - Installation instructions
  - Security verification badges
  - Change log since last release
- Attaches artifacts:
  - Main JAR
  - Fat JAR (fat JAR with dependencies)

#### `workflow-summary`

- Runs regardless of publish setting
- Generates summary table of all job statuses
- Provides quick overview of pipeline results

---

## Security Features

### DevSecOps Best Practices

1. **Least-Privilege Permissions**

   ```yaml
   permissions:
     contents: write        # Only for releases
     actions: read         # Minimal workflow access
   ```

   Note: `security-events: write` intentionally NOT granted. Security scan results remain private in workflow logs.

2. **Multi-Layer Security Scanning**
   - **Layer 1**: Repository/config (secrets, IaC)
   - **Layer 2**: Dependencies (CVE database)
   - **Layer 3**: Build artifacts (embedded vulnerabilities)
   - **Layer 4**: Source code (CodeQL SAST on releases only)

3. **Security Scanning**

   **Implementation**:
   - All security scans run and gate the pipeline (fail on >LOW severity)
   - Maintainers review scan results in workflow logs before merging/releasing

4. **Trivy Scan Configuration**
   - Severity threshold: `MEDIUM,HIGH,CRITICAL` (excludes LOW)
   - Fail-fast: Pipeline stops on any vulnerability
   - Results: Workflow logs only (not publicly exposed)
   - Scan types:
     - `config`: Repository configuration
     - `vuln`: Vulnerability scanning
     - `secret`: Secret detection

5. **CodeQL Configuration**
   - **Trigger**: Only runs on release builds (`publish: true`)
   - **Language**: Java (analyzes Kotlin as Java)
   - **Purpose**: Final security gate before publishing releases

6. **Pinned Action Versions with SHA256 Hashing**
   - Actions pinned to commit SHA256 hashes (not mutable tags)
   - Renovate Bot maintains both SHA and version comment
   - Example: `actions/checkout@abc123def...456 # v5.0.0`
   - Protects against tag mutation attacks (see: tj-actions/changed-files breach, March 2025)
   - Reduces supply chain attack surface

7. **Gradle Security & Performance**
   - Official `gradle/actions/setup-gradle@v5` for optimized caching
   - Daemon disabled (`-Dorg.gradle.daemon=false`)
   - Build cache enabled (`-Dorg.gradle.caching=true`)
   - Parallel execution enabled (`-Dorg.gradle.parallel=true`)
   - Warning mode enabled (`--warning-mode all`)
   - Build output sharing across jobs (eliminates redundant compilation)

---

## Usage Instructions

### Running the Workflow

1. **Navigate to Actions tab** in GitHub repository

2. **Select workflow**: "Build, Test, and Publish"

3. **Click "Run workflow"**

4. **Enter inputs**:
   - **Version**: Semantic version (e.g., `1.2.0`, `2.0.0-rc.1`)
   - **Publish**: Check to create GitHub Release (default: unchecked)

5. **Click "Run workflow"** (green button)

### Workflow Execution Scenarios

#### Scenario 1: Build & Test Only (Dry Run)

```
Version: 1.2.0
Publish: [ ] (unchecked)
```

**Result**: Builds, tests, scans, but does NOT create release

#### Scenario 2: Full Release

```
Version: 1.2.0
Publish: [x] (checked)
```

**Result**: Builds, tests, scans, and creates GitHub Release v1.2.0

#### Scenario 3: Pre-release

```
Version: 2.0.0-beta.1
Publish: [x] (checked)
```

**Result**: Creates release for beta version

---

## Version Override Mechanism

The workflow overrides the version in `build.gradle.kts` using Gradle properties:

```bash
./gradlew build -Pversion=1.2.0
```

This allows you to:

- Build multiple versions without modifying source code
- Keep `build.gradle.kts` at development version (e.g., `0.1-SNAPSHOT`)
- Create releases with clean version tags

**Note**: The version in `build.gradle.kts` is NOT updated by the workflow.

---

## Artifacts

### Build Artifacts (Always Created)

Stored in workflow run with specified retention:

- `main-jar`: Standard JAR without dependencies (30 days retention)
- `shadow-jar`: Fat JAR with all dependencies embedded (30 days retention)
- `build-outputs`: Compiled classes, resources, and Gradle metadata (1 day retention)
  - Shared between build and test jobs to eliminate redundant compilation
- `test-results`: JUnit XML test results (30 days retention)
- `coverage-report`: JaCoCo HTML coverage report (30 days retention)

### Release Artifacts (If Published)

Attached to GitHub Release, permanent storage

- `prime-fhir-converter-{version}.jar`: Main JAR
- `prime-fhir-converter-{version}-all.jar`: Fat JAR

### Security Reports

Available in workflow logs only (not publicly exposed)

- Repository configuration scan results (Trivy)
- Dependency vulnerability scan results (Trivy)
- Artifact security scan results (Trivy)
- Source code security analysis (CodeQL, release builds only)

---

## Troubleshooting

### Common Issues

#### [ERROR] "Invalid version format"

**Problem**: Version doesn't match semantic versioning

**Solution**: Use format `X.Y.Z` or `X.Y.Z-suffix`

```
Valid:   1.0.0, 2.1.3, 1.0.0-beta.1, 3.2.1-rc.2
Invalid: v1.0.0, 1.0, 1.0.0.0, latest, 1.x
```

#### [ERROR] "Version tag already exists"

**Problem**: Git tag for this version already exists

**Solution**: Use a new version number or delete the existing tag

```bash
# Check existing tags
git tag -l

# Delete tag if needed (be careful!)
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
```

#### [ERROR] "Trivy scan failed"

**Problem**: Security vulnerabilities found

**Solution**:

1. Review detailed scan results in workflow logs
2. Identify vulnerable dependencies or misconfigurations
3. Update vulnerable dependencies in `build.gradle.kts` or fix configuration issues
4. Rerun workflow after fixes

**Note**: Security scan results are only available in workflow logs, not in GitHub Security tab.

#### [ERROR] "Tests failed"

**Problem**: Unit tests or integration tests failing

**Solution**:

1. Download test results artifact from workflow
2. Fix failing tests locally, test, push
3. Rerun workflow

#### [ERROR] "Publish job didn't run"

**Problem**: Publish checkbox was unchecked OR previous jobs failed

**Solution**:

- Ensure publish checkbox is checked when triggering
- Verify all build, test, and security scans passed
- Check workflow summary for failed jobs

---

## Maintenance

### Renovate Bot for Automated Updates

**Active**: `.github/renovate.json`

Renovate is configured to automatically update dependencies with the following features:

**GitHub Actions (SHA256 Pinning)**:

- Pins actions to commit SHA256 hashes for security
- Maintains version tags in comments for readability
- Example: `actions/checkout@abc123...def # v5.0.0`
- Protects against tag mutation attacks

**Schedule**:

- Runs between 9PM-8AM EST (off-business hours)

**Grouping Strategy**:

- GitHub Actions: Single grouped PR
- Security updates (Log4j, Jackson): High priority separate PRs
- FHIR libraries: Grouped by ecosystem
- Test dependencies: Grouped together
- Kotlin ecosystem: Grouped together
- Gradle plugins: Grouped together

**Dashboard**:

- View all pending updates: Issues tab → "Renovate Dependency Dashboard"
- See PRs before they're created
- Control update scheduling per dependency

---

## Integration with Branch Protection

Recommended repository settings:

### Branch Protection Rules (master)

- [x] Require pull request before merging
- [x] Require status checks to pass
- [x] Require conversation resolution
- [x] Do not allow bypassing the above settings
- [x] Require linear history

### Security Features

- [x] Enable Renovate Bot (replaces Dependabot)
- [x] Enable vulnerability alerts
- [x] Enable secret scanning
- [x] Enable code scanning (CodeQL)

---

## Support

For issues or questions about the CI/CD pipeline:

1. Check workflow run logs in GitHub Actions tab
2. Review this documentation
3. Open an issue in the repository

---
