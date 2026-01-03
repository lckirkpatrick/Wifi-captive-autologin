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
    val validationIntervalMs: Long = 300000L, // 5 minutes default
    val enableReconnectionHandling: Boolean = false
)

@Serializable
enum class MatchType {
    EXACT,
    CONTAINS,
    REGEX
}
