# Extensibility Guide

This document explains how to extend the WiFi Captive Auto-Login app **without modifying existing code**. All extensions follow the Open/Closed Principle: open for extension, closed for modification.

## Architecture Principles

1. **Never modify existing logic paths** - All new features are added as extensions
2. **Profile-driven** - All behavior is controlled by profile configuration
3. **Plugin architecture** - New capabilities are added as plugins/drivers

## Extension Points

### 1. Adding New Match Types

**Current Implementation:** `ProfileMatcher.kt` uses a `when` statement with enum `MatchType`

**How to Extend (Future):**
The match type system will be refactored to use a strategy pattern:

```kotlin
// New match type strategy
class PrefixMatchStrategy : MatchStrategy {
    override fun matches(profile: PortalProfile, ssid: String): Boolean {
        return ssid.startsWith(profile.ssid, ignoreCase = true)
    }
}

// Register in MatchStrategyRegistry
MatchStrategyRegistry.register("PREFIX", PrefixMatchStrategy())
```

**Current Status:** Requires refactoring to strategy pattern (see task: `match-type-strategy`)

### 2. Adding New Action Drivers

**Current Implementation:** `WifiMonitorService` directly calls `PortalAccessibilityService.triggerPortalHandling()`

**How to Extend (Future):**
The driver system will use an interface and registry:

```kotlin
// New driver interface
interface PortalDriver {
    suspend fun handlePortal(profile: PortalProfile): Boolean
    fun getName(): String
}

// Example: HTTP-based driver
class HttpAutomatorDriver : PortalDriver {
    override suspend fun handlePortal(profile: PortalProfile): Boolean {
        // Make HTTP POST request to portal endpoint
        // Parse response and extract form data
        // Submit form automatically
        return true
    }
    override fun getName() = "HttpAutomator"
}

// Register in DriverRegistry
DriverRegistry.register(HttpAutomatorDriver())
```

**Current Status:** Requires driver interface creation (see task: `driver-interface`)

### 3. Adding Observability/Logging

**Current Implementation:** `StructuredLogger.kt` provides a logging interface

**How to Extend:**
You can create custom logger implementations:

```kotlin
// Custom logger that exports to file
class FileExportLogger : StructuredLogger {
    override fun logEvent(level: LogLevel, tag: String, message: String, 
                         context: LogContext?, throwable: Throwable?) {
        // Write to file with structured format
        val logEntry = LogEntry(level, tag, message, context?.toMap(), throwable)
        writeToFile(logEntry)
    }
}

// Set custom logger
AppLogger.setLogger(FileExportLogger())
```

**Status:** ✅ Ready to use - no code changes needed

### 4. Adding Profile Import/Export

**How to Add:**
Create a new module `core/importer`:

```kotlin
// New file: core/importer/ProfileImporter.kt
class ProfileImporter(private val profileStorage: ProfileStorage) {
    suspend fun importFromJson(json: String): List<PortalProfile> {
        // Parse JSON and validate profiles
        // Add to storage
    }
    
    suspend fun exportToJson(profileIds: List<String>): String {
        // Load profiles and serialize to JSON
    }
}
```

**Status:** ✅ Can be added as new module - no existing code changes needed

### 5. Adding Diagnostics Screen

**How to Add:**
Create a new activity `ui/DiagnosticsActivity`:

```kotlin
// New file: ui/DiagnosticsActivity.kt
class DiagnosticsActivity : AppCompatActivity() {
    // Display:
    // - Current Wi-Fi SSID
    // - Active profiles
    // - Recent log entries
    // - Accessibility service status
    // - Network connectivity status
}
```

**Status:** ✅ Can be added as new activity - no existing code changes needed

## Current Extension Limitations

### Match Types
- **Issue:** Uses enum + when statement
- **Solution:** Refactor to strategy pattern (planned)
- **Impact:** Adding new match types currently requires modifying `ProfileMatcher.kt`

### Action Drivers
- **Issue:** Hardcoded `PortalAccessibilityService` call
- **Solution:** Create `PortalDriver` interface and registry (planned)
- **Impact:** Adding HttpAutomator/WebViewAutomator requires modifying `WifiMonitorService.kt`

## Best Practices for Extensions

1. **Create new files/modules** - Don't modify existing ones
2. **Use dependency injection** - Pass dependencies through constructors
3. **Follow existing patterns** - Match the style of current code
4. **Add tests** - Ensure extensions don't break existing functionality
5. **Document changes** - Update this guide when adding new extension points

## Example: Adding a New Feature

Let's say you want to add email notifications when a portal is successfully logged into:

1. **Create new module:** `core/notifications/EmailNotifier.kt`
2. **Create interface:** `NotificationService` (if multiple notification types)
3. **Register in Application class:** `WifiCaptiveApplication.onCreate()`
4. **Hook into existing code:** Add call in `PortalAccessibilityService.showSuccessNotification()`
   - **Note:** This requires modifying existing code, so consider making it optional/configurable

**Better approach (no existing code changes):**
- Add notification configuration to `PortalProfile` (new optional field)
- Create notification registry that existing code can query
- Existing code calls registry, which returns appropriate notifier

## Future Refactoring Tasks

To make the codebase fully extensible without modification:

1. ✅ **Structured Logging** - Done (can be extended)
2. ⏳ **Match Type Strategy Pattern** - Planned
3. ⏳ **Driver Interface & Registry** - Planned
4. ✅ **Configuration Management** - Done (AppConfig can be extended)
5. ✅ **Error Handling** - Done (AppExceptions can be extended)

## Questions?

If you need to add a feature but aren't sure how to do it without modifying existing code, check:
1. Can it be added as a new module?
2. Can it use the existing extension points (logging, config)?
3. Does it require a new extension point (match type, driver)?
4. Should we create a new extension point first?

