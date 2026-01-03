# Quality Assurance & Development Tools

## Summary

This document outlines the quality assurance tools and processes set up for the WiFi Captive Auto-Login Android app.

## ✅ Implemented

### 1. Unit Testing
- **Framework**: JUnit 4, Mockito, Robolectric
- **Coverage**: Tests for `ProfileMatcher` and `PortalProfile` serialization
- **Command**: `./gradlew testDebugUnitTest`
- **Status**: ✅ All tests passing

### 2. Code Coverage (JaCoCo)
- **Tool**: JaCoCo 0.8.11
- **Reports**: HTML and XML formats
- **Command**: `./gradlew testDebugUnitTest jacocoTestReport`
- **View Report**: `open app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **Status**: ✅ Configured and working

### 3. Android Lint
- **Tool**: Built-in Android Lint
- **Command**: `./gradlew lintDebug`
- **Report**: `app/build/reports/lint-results-debug.html`
- **Status**: ✅ Integrated in pre-commit hook

### 4. Security Scanning
- **Script**: `security-scan.sh`
- **Checks**:
  - Hardcoded secrets
  - Debug logging in production
  - Insecure HTTP URLs
  - Manifest security issues
  - Exported components
- **Command**: `./security-scan.sh`
- **Status**: ✅ Configured

### 5. Git Pre-commit Hook
- **Location**: `.git/hooks/pre-commit`
- **Checks**:
  - Android Lint
  - Unit tests
- **Status**: ✅ Active and working

### 6. CI/CD (GitHub Actions)
- **Workflow**: `.github/workflows/ci.yml`
- **Runs on**: Push/PR to dev, stage, main branches
- **Actions**:
  - Lint check
  - Unit tests
  - Coverage report generation
  - APK build
  - Artifact upload
- **Status**: ✅ Configured (ready when pushed to GitHub)

### 7. Dependency Management
- **Tool**: GitHub Dependabot
- **Config**: `.github/dependabot.yml`
- **Schedule**: Weekly checks for Gradle dependencies
- **Status**: ✅ Configured (ready when pushed to GitHub)

## 📋 Recommendations

### Dependency Scanning
**Recommendation**: ✅ **Skip in pre-commit, rely on GitHub**

**Reasoning**:
- GitHub already provides dependency scanning via Dependabot and Security tab
- Dependency scanning is slow (can take minutes)
- Pre-commit hooks should be fast (< 30 seconds)
- GitHub's scanning is more comprehensive and automated

**Action**: No changes needed. GitHub will automatically scan dependencies when code is pushed.

### Code Style (ktlint/Detekt)
**Recommendation**: ⚠️ **Optional - Can add later**

**Reasoning**:
- Android Lint already provides good code quality checks
- ktlint had configuration conflicts with modern Gradle setup
- Can be added later if team wants stricter style enforcement
- Not critical for SOHO app quality

**If adding later**:
- Use ktlint Gradle plugin (not standalone)
- Configure in `settings.gradle` plugin management
- Add to CI/CD (not pre-commit, as it can be slow)

### Performance Testing
**Recommendation**: ❌ **Skip for now**

**Reasoning**:
- Overkill for SOHO app
- App is simple (no complex algorithms)
- Current architecture is efficient
- Can add later if performance issues arise

## 🚀 Current Workflow

### Local Development
1. Make code changes
2. Run tests: `./gradlew testDebugUnitTest`
3. Check coverage: `./gradlew jacocoTestReport`
4. Run lint: `./gradlew lintDebug`
5. Commit (pre-commit hook runs automatically)

### CI/CD Pipeline (GitHub)
1. Push to branch → GitHub Actions triggers
2. Runs all checks (lint, tests, coverage)
3. Builds APK
4. Uploads artifacts
5. Dependabot checks dependencies weekly

## 📊 Coverage Goals

- **Current**: Basic coverage for core logic
- **Target**: 60-70% for core business logic
- **Focus Areas**:
  - Profile matching logic ✅
  - Profile storage/serialization ✅
  - SSID matching ✅

## 🔒 Security

- **Dependency Scanning**: GitHub Dependabot + Security tab
- **Code Scanning**: `security-scan.sh` script
- **Secrets**: No hardcoded secrets (checked by script)
- **Network**: Only HTTP for captive portal detection (acceptable)

## 📝 Notes

- Pre-commit hook can be bypassed with `git commit --no-verify` (not recommended)
- Coverage reports are generated but not enforced (no minimum threshold)
- All quality checks pass before code is merged
- GitHub Actions will run automatically on push/PR

