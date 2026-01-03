#!/bin/bash

# Security scanning script for Android app
# Checks for common security issues

set -e

echo "🔒 Running security scan..."

cd "$(dirname "$0")" || exit 1

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
        export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

ISSUES=0

# Check for hardcoded secrets
echo "🔍 Checking for hardcoded secrets..."
if grep -r "password\|secret\|api_key\|private_key" app/src/main/java --include="*.kt" --include="*.java" | grep -v "//" | grep -v "test" | grep -v "TODO"; then
    echo "⚠️  Warning: Potential hardcoded secrets found"
    ISSUES=$((ISSUES + 1))
fi

# Check for debug logging in release code
echo "🔍 Checking for debug logging..."
if grep -r "Log\.d\|Log\.v\|println\|System\.out" app/src/main/java --include="*.kt" --include="*.java" | grep -v "//" | grep -v "test"; then
    echo "⚠️  Warning: Debug logging found in production code"
    ISSUES=$((ISSUES + 1))
fi

# Check for insecure network configurations
echo "🔍 Checking for insecure network configurations..."
if grep -r "http://" app/src/main/java --include="*.kt" --include="*.java" | grep -v "//" | grep -v "test" | grep -v "captive.apple.com\|msftconnecttest.com"; then
    echo "⚠️  Warning: HTTP (non-HTTPS) URLs found"
    ISSUES=$((ISSUES + 1))
fi

# Check AndroidManifest for security issues
echo "🔍 Checking AndroidManifest.xml..."
if grep -q "android:debuggable=\"true\"" app/src/main/AndroidManifest.xml; then
    echo "❌ Error: debuggable=true found in manifest"
    ISSUES=$((ISSUES + 1))
fi

# Check for exported services without proper permissions
echo "🔍 Checking for exported services..."
if grep -q "android:exported=\"true\"" app/src/main/AndroidManifest.xml; then
    echo "⚠️  Warning: Exported components found - ensure they have proper permissions"
fi

# Run dependency check (if available)
if command -v gradle &> /dev/null || [ -f "./gradlew" ]; then
    echo "🔍 Checking dependencies for known vulnerabilities..."
    ./gradlew dependencyCheckAnalyze --no-daemon --quiet 2>/dev/null || {
        echo "ℹ️  dependency-check plugin not configured (optional)"
    }
fi

if [ $ISSUES -eq 0 ]; then
    echo "✅ Security scan passed - no issues found!"
    exit 0
else
    echo "⚠️  Security scan found $ISSUES potential issue(s)"
    echo "Please review and fix before releasing"
    exit 1
fi

