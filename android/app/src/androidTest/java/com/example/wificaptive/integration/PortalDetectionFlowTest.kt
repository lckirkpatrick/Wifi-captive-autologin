package com.example.wificaptive.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wificaptive.core.profile.MatchType
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.core.profile.matchesSsid
import com.example.wificaptive.core.storage.ProfileStorage
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Integration tests for end-to-end portal detection flows.
 * 
 * These tests verify that the complete flow from profile matching
 * to portal detection works correctly.
 */
@RunWith(AndroidJUnit4::class)
class PortalDetectionFlowTest {
    
    private lateinit var context: Context
    private lateinit var profileStorage: ProfileStorage
    private lateinit var profilesFile: File
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        profileStorage = ProfileStorage(context)
        profilesFile = File(context.filesDir, "profiles.json")
        
        // Clean up any existing profiles
        if (profilesFile.exists()) {
            profilesFile.delete()
        }
    }
    
    @After
    fun tearDown() {
        // Clean up test data
        if (profilesFile.exists()) {
            profilesFile.delete()
        }
    }
    
    @Test
    fun testProfileMatching_ExactMatch() = runBlocking {
        val profile = PortalProfile(
            id = "test-exact",
            ssid = "TestWiFi",
            matchType = MatchType.EXACT,
            triggerUrl = "http://captive.apple.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        // Test exact match
        assertTrue(matchesSsid(profile, "TestWiFi"))
        assertFalse(matchesSsid(profile, "TestWiFi2"))
        assertFalse(matchesSsid(profile, "testwifi")) // Case sensitive
    }
    
    @Test
    fun testProfileMatching_ContainsMatch() = runBlocking {
        val profile = PortalProfile(
            id = "test-contains",
            ssid = "Airport",
            matchType = MatchType.CONTAINS,
            triggerUrl = "http://captive.apple.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        // Test contains match (case-insensitive)
        assertTrue(matchesSsid(profile, "AirportWiFi"))
        assertTrue(matchesSsid(profile, "MyAirportNetwork"))
        assertTrue(matchesSsid(profile, "airport-wifi")) // Case insensitive
        assertFalse(matchesSsid(profile, "HotelWiFi"))
    }
    
    @Test
    fun testProfileMatching_RegexMatch() = runBlocking {
        val profile = PortalProfile(
            id = "test-regex",
            ssid = ".*Airport.*",
            matchType = MatchType.REGEX,
            triggerUrl = "http://captive.apple.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        // Test regex match (case-insensitive)
        assertTrue(matchesSsid(profile, "AirportWiFi"))
        assertTrue(matchesSsid(profile, "MyAirportNetwork"))
        assertTrue(matchesSsid(profile, "airport-lounge")) // Case insensitive
        assertFalse(matchesSsid(profile, "HotelWiFi"))
    }
    
    @Test
    fun testProfileStorage_SaveAndLoad() = runBlocking {
        val profile1 = PortalProfile(
            id = "test-1",
            ssid = "Test1",
            matchType = MatchType.EXACT,
            triggerUrl = "http://captive.apple.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        val profile2 = PortalProfile(
            id = "test-2",
            ssid = "Test2",
            matchType = MatchType.CONTAINS,
            triggerUrl = "http://www.msftconnecttest.com/redirect",
            clickTextExact = "I Agree",
            clickTextContains = listOf("Accept", "Agree"),
            timeoutMs = 15000L,
            cooldownMs = 5000L,
            enabled = false,
            enableConnectivityValidation = true,
            validationIntervalMs = 300000L,
            enableReconnectionHandling = true
        )
        
        // Save profiles
        profileStorage.saveProfiles(listOf(profile1, profile2))
        
        // Load profiles
        val loadedProfiles = profileStorage.loadProfiles()
        
        assertEquals(2, loadedProfiles.size)
        
        val loaded1 = loadedProfiles.find { it.id == "test-1" }
        assertNotNull(loaded1)
        assertEquals(profile1.ssid, loaded1!!.ssid)
        assertEquals(profile1.matchType, loaded1.matchType)
        assertEquals(profile1.enabled, loaded1.enabled)
        
        val loaded2 = loadedProfiles.find { it.id == "test-2" }
        assertNotNull(loaded2)
        assertEquals(profile2.ssid, loaded2!!.ssid)
        assertEquals(profile2.enableConnectivityValidation, loaded2.enableConnectivityValidation)
        assertEquals(profile2.enableReconnectionHandling, loaded2.enableReconnectionHandling)
    }
    
    @Test
    fun testProfileStorage_AddUpdateDelete() = runBlocking {
        val profile = PortalProfile(
            id = "test-crud",
            ssid = "TestCRUD",
            matchType = MatchType.EXACT,
            triggerUrl = "http://captive.apple.com",
            clickTextExact = null,
            clickTextContains = listOf("Accept"),
            timeoutMs = 10000L,
            cooldownMs = 5000L,
            enabled = true
        )
        
        // Add profile
        profileStorage.addProfile(profile)
        var profiles = profileStorage.loadProfiles()
        assertEquals(1, profiles.count { it.id == "test-crud" })
        
        // Update profile
        val updatedProfile = profile.copy(enabled = false, ssid = "UpdatedSSID")
        profileStorage.updateProfile(updatedProfile)
        profiles = profileStorage.loadProfiles()
        val updated = profiles.find { it.id == "test-crud" }
        assertNotNull(updated)
        assertEquals("UpdatedSSID", updated!!.ssid)
        assertFalse(updated.enabled)
        
        // Delete profile
        profileStorage.deleteProfile("test-crud")
        profiles = profileStorage.loadProfiles()
        assertEquals(0, profiles.count { it.id == "test-crud" })
    }
    
    @Test
    fun testProfileMatching_MultipleProfiles() = runBlocking {
        val profiles = listOf(
            PortalProfile(
                id = "exact-match",
                ssid = "ExactNetwork",
                matchType = MatchType.EXACT,
                triggerUrl = "http://captive.apple.com",
                clickTextExact = null,
                clickTextContains = listOf("Accept"),
                timeoutMs = 10000L,
                cooldownMs = 5000L,
                enabled = true
            ),
            PortalProfile(
                id = "contains-match",
                ssid = "Airport",
                matchType = MatchType.CONTAINS,
                triggerUrl = "http://captive.apple.com",
                clickTextExact = null,
                clickTextContains = listOf("Accept"),
                timeoutMs = 10000L,
                cooldownMs = 5000L,
                enabled = true
            ),
            PortalProfile(
                id = "regex-match",
                ssid = ".*Hotel.*",
                matchType = MatchType.REGEX,
                triggerUrl = "http://captive.apple.com",
                clickTextExact = null,
                clickTextContains = listOf("Accept"),
                timeoutMs = 10000L,
                cooldownMs = 5000L,
                enabled = true
            )
        )
        
        profileStorage.saveProfiles(profiles)
        
        // Test matching with multiple profiles
        val loadedProfiles = profileStorage.loadProfiles()
        val enabledProfiles = loadedProfiles.filter { it.enabled }
        
        // Find matching profile for "ExactNetwork"
        val match1 = enabledProfiles.find { matchesSsid(it, "ExactNetwork") }
        assertNotNull(match1)
        assertEquals("exact-match", match1!!.id)
        
        // Find matching profile for "AirportWiFi"
        val match2 = enabledProfiles.find { matchesSsid(it, "AirportWiFi") }
        assertNotNull(match2)
        assertEquals("contains-match", match2!!.id)
        
        // Find matching profile for "HotelGuest"
        val match3 = enabledProfiles.find { matchesSsid(it, "HotelGuest") }
        assertNotNull(match3)
        assertEquals("regex-match", match3!!.id)
    }
    
    @Test
    fun testProfileStorage_DefaultProfiles() = runBlocking {
        // Load profiles when file doesn't exist (should create defaults)
        val profiles = profileStorage.loadProfiles()
        
        assertTrue(profiles.isNotEmpty())
        assertTrue(profiles.any { it.id.contains("airport", ignoreCase = true) })
        assertTrue(profiles.any { it.id.contains("starbucks", ignoreCase = true) })
    }
}

