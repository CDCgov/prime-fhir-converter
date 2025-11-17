# Renovate Bot Setup Guide

## Overview

This repository uses Renovate Bot for automated dependency updates with SHA256 commit hash pinning for enhanced security.

## Pending Installation

### Step 1: Install Renovate GitHub App

1. Go to: https://github.com/apps/renovate
2. Click "Install" or "Configure"
3. Select your organization or account
4. Choose repositories:
   - Select "Only select repositories"
   - Choose `prime-fhir-converter`
5. Click "Install" or "Save"

### Step 2: Verify Configuration

The repository already contains `.github/renovate.json` with optimal settings.

### Step 3: Initial Onboarding

When Renovate first runs, it will:
1. Create an onboarding PR
2. Show all detected dependencies
3. Wait for your approval
4. **Merge the onboarding PR to activate Renovate**

## Configuration Details

### File Location
`.github/renovate.json`

Configured to run between 9PM-8AM EST, respecting EDT/EST automatically.

**3. Dependency Grouping and Priority**

| Group | Priority | Description | Auto-Create PR? |
|-------|----------|-------------|-----------------|
| **CRITICAL Vulnerabilities** | 100 | CVEs with CRITICAL severity | **Yes** (immediate) |
| Vulnerability Alerts | 30 | HIGH/MEDIUM CVEs | No (dashboard only) |
| Security Updates | 20 | Log4j, Jackson | No (dashboard only) |
| GitHub Actions | 10 | All actions in one PR | No (dashboard only) |
| Kotlin Ecosystem | 7 | Kotlin stdlib and plugins | No (dashboard only) |
| FHIR Libraries | 5 | HAPI FHIR dependencies | No (dashboard only) |
| Gradle Plugins | 4 | Build plugins | No (dashboard only) |
| Test Dependencies | 3 | JUnit, MockK, AssertK | No (dashboard only) |
| All Other | 1 | Everything else | No (dashboard only) |

**4. Passive Mode (Dashboard-Only)**

All updates except CRITICAL vulnerabilities require manual approval:
- Updates appear in Renovate Dependency Dashboard issue
- Check boxes to create PRs for specific updates

**5. Vulnerability Alerts**

Renovate monitors for security vulnerabilities (CVEs). CRITICAL vulnerabilities automatically create PRs. All other severity levels appear in the dashboard for manual approval.

## Using Renovate

**Operating Mode**: Passive (manual approval required for most updates)

### Dependency Dashboard

**Primary workflow** for sporadic maintenance:

1. Go to repository Issues tab
2. Look for "Renovate Dependency Dashboard" issue
3. See all available updates with details (release notes, changelogs, compatibility)
4. **Check boxes next to updates you want** to create PRs
5. Renovate creates PRs only for checked items

**Passive Mode Benefits**:
- No PR spam - you control when PRs are created
- Perfect for part-time/sporadic maintenance
- Dashboard shows everything, you choose what to act on
- Updates persist in dashboard until you address them

### Automatic PRs (Exceptions)

Renovate **automatically creates PRs** only for:
- **CRITICAL vulnerabilities** (CVEs with CRITICAL severity)
  - Highest priority (100)
  - Labeled `critical`, `security`, `auto-created`
  - Created immediately, not waiting for approval

All other updates (including HIGH/MEDIUM vulnerabilities) require manual approval via dashboard.

### Review and Merge

When a PR is created (manually or automatically):

1. Review PR description (includes release notes, changelogs)
2. Check CI/CD pipeline status
3. Merge when ready

**Note**: Auto-merge is disabled for safety. All PRs require manual review and merge.

## Package Rules

### CRITICAL Vulnerability Rule (Highest Priority)

```json
{
  "vulnerabilitySeverity": ["CRITICAL"],
  "prCreation": "immediate",
  "prPriority": 100
}
```

- **Highest priority** (100) - overrides all other rules
- **Auto-creates PRs** immediately (bypasses passive mode)
- Only for CRITICAL CVE severity
- Labeled: `critical`, `security`, `auto-created`
- Commit prefix: `deps(CRITICAL):`

This ensures critical security issues get immediate attention even when maintainers are not actively monitoring the dashboard.

### GitHub Actions Rule
```json
{
  "matchManagers": ["github-actions"],
  "pinDigests": true,
  "groupName": "GitHub Actions",
  "prPriority": 10
}
```

- Pins all actions to SHA256 hashes
- Groups all action updates into single PR
- High priority (10)

### Security Updates Rule
```json
{
  "matchPackagePatterns": [
    "^org\\.apache\\.logging\\.log4j",
    "^com\\.fasterxml\\.jackson"
  ],
  "groupName": "Security Updates",
  "prPriority": 20
}
```

- Highest priority (20)
- Separate PR for critical libraries
- Immediate attention required

### Kotlin Blocking Rule
```json
{
  "matchPackagePatterns": ["^org\\.jetbrains\\.kotlin"],
  "matchUpdateTypes": ["major"],
  "enabled": false
}
```

- Blocks major Kotlin version updates
- Prevents breaking changes without manual review
- Minor/patch updates still allowed

## Customization

### Switch to Active Mode (Auto-Create All PRs)

Current configuration uses **passive mode** (dashboard-only). To auto-create PRs for all updates:

Edit `.github/renovate.json`:
```json
{
  "prCreation": "not-pending"  // Change from "approval"
}
```

**Warning**: This will create many PRs. Only use if actively maintaining the repository.

### Enable Auto-merge for Low-Risk Updates

```json
{
  "packageRules": [
    {
      "matchManagers": ["github-actions"],
      "matchUpdateTypes": ["patch", "digest"],
      "automerge": true
    }
  ]
}
```

**Warning**: Only enable for trusted, well-tested dependencies.

### Add New Dependency Groups

```json
{
  "packageRules": [
    {
      "matchPackagePatterns": ["^org\\.springframework"],
      "groupName": "Spring Framework",
      "prPriority": 6
    }
  ]
}
```

## Security Best Practices

1. **Review all PRs** - Never blindly merge dependency updates
2. **Monitor security advisories** - Subscribe to GitHub security alerts
3. **Keep SHA pinning enabled** - Protects against supply chain attacks
4. **Test before merging** - Ensure CI/CD passes
5. **Check release notes** - Understand what's changing

## Resources

- **Renovate Docs**: https://docs.renovatebot.com
- **Configuration Options**: https://docs.renovatebot.com/configuration-options/
- **GitHub Actions Manager**: https://docs.renovatebot.com/modules/manager/github-actions/
- **Preset Configs**: https://docs.renovatebot.com/presets-config/

## Support

For issues with Renovate:
1. Check Renovate Dashboard issue comments
2. Review Renovate documentation
3. Open issue in this repository
4. Contact DevSecOps team
