package com.example.wificaptive.core.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileMatcherTest {

    @Test
    fun `matchesSsid with EXACT match type returns true for exact match`() {
        val profile = PortalProfile(
            id = "test",
            ssid = "MyWiFi",
            matchType = MatchType.EXACT,
            triggerUrl = "http://test.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        assertTrue(matchesSsid(profile, "MyWiFi"))
        assertFalse(matchesSsid(profile, "MyWiFi2"))
        assertFalse(matchesSsid(profile, "mywifi"))
    }

    @Test
    fun `matchesSsid with CONTAINS match type returns true for substring match`() {
        val profile = PortalProfile(
            id = "test",
            ssid = "Airport",
            matchType = MatchType.CONTAINS,
            triggerUrl = "http://test.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        assertTrue(matchesSsid(profile, "Airport WiFi"))
        assertTrue(matchesSsid(profile, "Free Airport WiFi"))
        assertTrue(matchesSsid(profile, "airport")) // case insensitive
        assertFalse(matchesSsid(profile, "Coffee Shop"))
    }

    @Test
    fun `matchesSsid with REGEX match type returns true for regex match`() {
        val profile = PortalProfile(
            id = "test",
            ssid = ".*Airport.*",
            matchType = MatchType.REGEX,
            triggerUrl = "http://test.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        assertTrue(matchesSsid(profile, "Airport WiFi"))
        assertTrue(matchesSsid(profile, "Free Airport Network"))
        assertTrue(matchesSsid(profile, "airport")) // case insensitive
        assertFalse(matchesSsid(profile, "Coffee Shop"))
    }

    @Test
    fun `matchesSsid with REGEX handles complex patterns`() {
        val profile = PortalProfile(
            id = "test",
            ssid = "Starbucks.*",
            matchType = MatchType.REGEX,
            triggerUrl = "http://test.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        assertTrue(matchesSsid(profile, "Starbucks WiFi"))
        assertTrue(matchesSsid(profile, "Starbucks_Guest"))
        assertFalse(matchesSsid(profile, "Coffee Shop"))
    }
}

