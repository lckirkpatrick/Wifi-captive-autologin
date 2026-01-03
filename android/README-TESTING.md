# Testing & Quality Assurance

## Unit Tests

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

Run tests with coverage:
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

## Linting

Run lint check:
```bash
./gradlew lintDebug
```

View lint report:
```bash
open app/build/reports/lint-results-debug.html
```

## Security Scanning

Run security scan:
```bash
./security-scan.sh
```

This checks for:
- Hardcoded secrets
- Debug logging in production
- Insecure network configurations
- Manifest security issues
- Exported components

## Pre-commit Hooks

Git pre-commit hook automatically runs:
- Lint checks
- Unit tests

To bypass (not recommended):
```bash
git commit --no-verify
```

## Continuous Integration

For CI/CD pipelines, run:
```bash
./gradlew clean lintDebug testDebugUnitTest assembleDebug
```

