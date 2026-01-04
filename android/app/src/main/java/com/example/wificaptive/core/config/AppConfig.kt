package com.example.wificaptive.core.config

/**
 * Centralized application configuration.
 * 
 * All timeouts, delays, retry counts, and other magic numbers should be defined here.
 * This allows for easy tuning without modifying business logic.
 */
object AppConfig {
    
    // ========== Accessibility Service Configuration ==========
    
    /** Delay before attempting to click portal button (ms) */
    const val ACCESSIBILITY_CLICK_DELAY_MS = 500L
    
    /** Maximum number of retry attempts for clicking portal button */
    const val ACCESSIBILITY_MAX_RETRIES = 3
    
    /** Delay between retry attempts (ms) - first retry */
    const val ACCESSIBILITY_RETRY_DELAY_1_MS = 1000L
    
    /** Delay between retry attempts (ms) - second retry */
    const val ACCESSIBILITY_RETRY_DELAY_2_MS = 2000L
    
    /** Notification channel ID for portal notifications */
    const val NOTIFICATION_CHANNEL_ID = "portal_autologin_channel"
    
    /** Notification ID for success notifications */
    const val NOTIFICATION_ID_SUCCESS = 1001
    
    /** Notification ID for failure notifications */
    const val NOTIFICATION_ID_FAILURE = 1002
    
    // ========== Wi-Fi Monitoring Configuration ==========
    
    /** Default timeout for portal trigger requests (ms) */
    const val PORTAL_TRIGGER_TIMEOUT_MS = 5000L
    
    /** Default read timeout for portal trigger requests (ms) */
    const val PORTAL_TRIGGER_READ_TIMEOUT_MS = 5000L
    
    /** Cooldown period to prevent duplicate triggers (ms) */
    const val PORTAL_TRIGGER_COOLDOWN_MS = 5000L
    
    /** Time window for detecting reconnections (ms) - 30 seconds */
    const val RECONNECTION_DETECTION_WINDOW_MS = 30000L
    
    // ========== Profile Defaults ==========
    
    /** Default timeout for portal operations (ms) */
    const val DEFAULT_PROFILE_TIMEOUT_MS = 10000L
    
    /** Default cooldown period between portal attempts (ms) */
    const val DEFAULT_PROFILE_COOLDOWN_MS = 5000L
    
    /** Default connectivity validation interval (ms) - 5 minutes */
    const val DEFAULT_VALIDATION_INTERVAL_MS = 300000L
    
    /** Default click text options if none specified */
    val DEFAULT_CLICK_TEXT_OPTIONS = listOf("Accept", "Connect", "Continue")
    
    // ========== Network Configuration ==========
    
    /** Debounce delay for network state changes (ms) */
    const val NETWORK_STATE_DEBOUNCE_MS = 1000L
    
    // ========== Storage Configuration ==========
    
    /** Profile storage filename */
    const val PROFILES_FILENAME = "profiles.json"
}

