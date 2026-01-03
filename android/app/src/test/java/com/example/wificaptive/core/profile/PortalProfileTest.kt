package com.example.wificaptive.core.profile

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PortalProfileTest {

    @Test
    fun `PortalProfile can be serialized and deserialized`() {
        val profile = PortalProfile(
            id = "test-id",
            ssid = "TestWiFi",
            matchType = MatchType.EXACT,
            triggerUrl = "http://test.com",
            clickTextExact = "Accept",
            clickTextContains = listOf("Connect", "Continue"),
            timeoutMs = 5000L,
            cooldownMs = 3000L,
            enabled = true,
            enableConnectivityValidation = true,
            validationIntervalMs = 300000L,
            enableReconnectionHandling = true
        )

        val json = Json { 
            prettyPrint = false
            ignoreUnknownKeys = true
        }
        
        val serialized = json.encodeToString(profile)
        val deserialized = json.decodeFromString<PortalProfile>(serialized)
        
        assertEquals(profile.id, deserialized.id)
        assertEquals(profile.ssid, deserialized.ssid)
        assertEquals(profile.matchType, deserialized.matchType)
        assertEquals(profile.triggerUrl, deserialized.triggerUrl)
        assertEquals(profile.clickTextExact, deserialized.clickTextExact)
        assertEquals(profile.clickTextContains, deserialized.clickTextContains)
        assertEquals(profile.timeoutMs, deserialized.timeoutMs)
        assertEquals(profile.cooldownMs, deserialized.cooldownMs)
        assertEquals(profile.enabled, deserialized.enabled)
        assertEquals(profile.enableConnectivityValidation, deserialized.enableConnectivityValidation)
        assertEquals(profile.validationIntervalMs, deserialized.validationIntervalMs)
        assertEquals(profile.enableReconnectionHandling, deserialized.enableReconnectionHandling)
    }

    @Test
    fun `PortalProfile has correct default values`() {
        val profile = PortalProfile(
            id = "test",
            ssid = "Test",
            matchType = MatchType.EXACT,
            triggerUrl = "http://test.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        assertEquals(false, profile.enableConnectivityValidation)
        assertEquals(300000L, profile.validationIntervalMs) // 5 minutes
        assertEquals(false, profile.enableReconnectionHandling)
        assertNotNull(profile.clickTextContains)
    }
}

