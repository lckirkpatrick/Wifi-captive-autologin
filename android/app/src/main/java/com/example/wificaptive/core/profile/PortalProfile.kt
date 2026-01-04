package com.example.wificaptive.core.profile

import kotlinx.serialization.Serializable

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
    val validationIntervalMs: Long = 300000L, // Default: 5 minutes (use AppConfig.DEFAULT_VALIDATION_INTERVAL_MS in code)
    val enableReconnectionHandling: Boolean = false
)

@Serializable
enum class MatchType {
    EXACT,
    CONTAINS,
    REGEX
}
