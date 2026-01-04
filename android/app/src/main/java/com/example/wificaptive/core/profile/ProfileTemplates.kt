package com.example.wificaptive.core.profile

import java.util.UUID

/**
 * Pre-configured profile templates for common captive portal providers.
 * 
 * These templates can be used as starting points for creating new profiles.
 * Users can select a template and customize it for their specific network.
 */
object ProfileTemplates {
    
    /**
     * Get all available profile templates
     */
    fun getAllTemplates(): List<ProfileTemplate> {
        return listOf(
            airportTemplate,
            hotelTemplate,
            cafeTemplate,
            starbucksTemplate,
            mcdonaldsTemplate,
            airportLoungeTemplate,
            trainStationTemplate,
            shoppingMallTemplate
        )
    }
    
    /**
     * Get a template by name
     */
    fun getTemplate(name: String): ProfileTemplate? {
        return getAllTemplates().find { it.name == name }
    }
    
    /**
     * Create a profile from a template
     */
    fun createProfileFromTemplate(template: ProfileTemplate, customSsid: String? = null): PortalProfile {
        return PortalProfile(
            id = UUID.randomUUID().toString(),
            ssid = customSsid ?: template.defaultSsid,
            matchType = template.matchType,
            triggerUrl = template.triggerUrl,
            clickTextExact = template.clickTextExact,
            clickTextContains = template.clickTextContains,
            timeoutMs = template.timeoutMs,
            cooldownMs = template.cooldownMs,
            enabled = true,
            enableConnectivityValidation = template.enableConnectivityValidation,
            validationIntervalMs = template.validationIntervalMs,
            enableReconnectionHandling = template.enableReconnectionHandling
        )
    }
    
    /**
     * Airport Wi-Fi template
     */
    val airportTemplate = ProfileTemplate(
        name = "Airport Wi-Fi",
        description = "Common airport captive portals",
        defaultSsid = ".*Airport.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://captive.apple.com",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Connect", "Continue", "Agree", "I Agree"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = true,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * Hotel Wi-Fi template
     */
    val hotelTemplate = ProfileTemplate(
        name = "Hotel Wi-Fi",
        description = "Hotel guest network portals",
        defaultSsid = ".*Hotel.*|.*Guest.*|.*WiFi.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://www.msftconnecttest.com/redirect",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue", "Connect", "Get Started"),
        timeoutMs = 15000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = true
    )
    
    /**
     * Cafe/Restaurant Wi-Fi template
     */
    val cafeTemplate = ProfileTemplate(
        name = "Cafe/Restaurant",
        description = "Coffee shops and restaurants",
        defaultSsid = ".*Cafe.*|.*Coffee.*|.*Restaurant.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://captive.apple.com",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue", "Connect"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * Starbucks Wi-Fi template
     */
    val starbucksTemplate = ProfileTemplate(
        name = "Starbucks",
        description = "Starbucks Wi-Fi networks",
        defaultSsid = ".*Starbucks.*|.*GoogleStarbucks.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://www.msftconnecttest.com/redirect",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * McDonald's Wi-Fi template
     */
    val mcdonaldsTemplate = ProfileTemplate(
        name = "McDonald's",
        description = "McDonald's Wi-Fi networks",
        defaultSsid = ".*McDonald.*|.*McD.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://www.msftconnecttest.com/redirect",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue", "Get Started"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * Airport lounge Wi-Fi template
     */
    val airportLoungeTemplate = ProfileTemplate(
        name = "Airport Lounge",
        description = "Premium airport lounge networks",
        defaultSsid = ".*Lounge.*|.*Priority.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://captive.apple.com",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Connect", "Continue"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = true,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * Train station Wi-Fi template
     */
    val trainStationTemplate = ProfileTemplate(
        name = "Train Station",
        description = "Railway station Wi-Fi networks",
        defaultSsid = ".*Station.*|.*Train.*|.*Railway.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://www.msftconnecttest.com/redirect",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue", "Connect"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
    
    /**
     * Shopping mall Wi-Fi template
     */
    val shoppingMallTemplate = ProfileTemplate(
        name = "Shopping Mall",
        description = "Shopping center Wi-Fi networks",
        defaultSsid = ".*Mall.*|.*Shopping.*|.*Center.*",
        matchType = MatchType.REGEX,
        triggerUrl = "http://captive.apple.com",
        clickTextExact = null,
        clickTextContains = listOf("Accept", "Agree", "Continue", "Connect"),
        timeoutMs = 10000L,
        cooldownMs = 5000L,
        enableConnectivityValidation = false,
        validationIntervalMs = 300000L,
        enableReconnectionHandling = false
    )
}

/**
 * Template definition for creating profiles
 */
data class ProfileTemplate(
    val name: String,
    val description: String,
    val defaultSsid: String,
    val matchType: MatchType,
    val triggerUrl: String,
    val clickTextExact: String?,
    val clickTextContains: List<String>,
    val timeoutMs: Long,
    val cooldownMs: Long,
    val enableConnectivityValidation: Boolean,
    val validationIntervalMs: Long,
    val enableReconnectionHandling: Boolean
)

