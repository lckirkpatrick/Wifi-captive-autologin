package com.example.wificaptive.core.profile

import kotlinx.serialization.Serializable

/**
 * Configuration profile for a captive portal Wi-Fi network.
 * 
 * Each profile defines how to detect a Wi-Fi network (by SSID) and how to
 * automatically accept the captive portal terms.
 * 
 * @param id Unique identifier for this profile
 * @param ssid The SSID pattern to match against (interpreted based on matchType)
 * @param matchType How to match the SSID (EXACT, CONTAINS, or REGEX)
 * @param triggerUrl URL to request to trigger the captive portal page
 * @param clickTextExact Exact text to click on the portal page (optional)
 * @param clickTextContains List of text patterns to search for on portal page
 * @param timeoutMs Maximum time to wait for portal page to load (milliseconds)
 * @param cooldownMs Minimum time between portal trigger attempts (milliseconds)
 * @param enabled Whether this profile is currently active
 * @param enableConnectivityValidation If true, periodically validate internet connectivity
 * @param validationIntervalMs Interval between connectivity checks (milliseconds, default 5 minutes)
 * @param enableReconnectionHandling If true, re-trigger portal on network reconnection
 */
@Serializable
data class PortalProfile(
    val id: String,
    val ssid: String,
    val matchType: MatchType,
    val triggerUrl: String,
    val clickTextExact: String?,
    val clickTextContains: List<String>,
    val timeoutMs: Long,
    val cooldownMs: Long,
    val enabled: Boolean,
    val enableConnectivityValidation: Boolean = false,
    val validationIntervalMs: Long = 300000L, // 5 minutes default
    val enableReconnectionHandling: Boolean = false
)

/**
 * SSID matching algorithm types.
 * 
 * - EXACT: SSID must exactly match the profile SSID
 * - CONTAINS: SSID must contain the profile SSID (case-insensitive)
 * - REGEX: SSID must match the profile SSID as a regular expression (case-insensitive)
 */
@Serializable
enum class MatchType {
    EXACT,
    CONTAINS,
    REGEX
}
