package com.example.wificaptive.core.storage

import android.content.Context
import com.example.wificaptive.core.profile.PortalProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class ProfileStorage(private val context: Context) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val profilesFile: File by lazy {
        File(context.filesDir, "profiles.json")
    }

    suspend fun loadProfiles(): List<PortalProfile> = withContext(Dispatchers.IO) {
        try {
            if (!profilesFile.exists()) {
                val defaultProfiles = getDefaultProfiles()
                saveProfiles(defaultProfiles)
                return@withContext defaultProfiles
            }
            val jsonString = profilesFile.readText()
            if (jsonString.isBlank()) {
                val defaultProfiles = getDefaultProfiles()
                saveProfiles(defaultProfiles)
                return@withContext defaultProfiles
            }
            json.decodeFromString<List<PortalProfile>>(jsonString)
        } catch (e: Exception) {
            // If loading fails, return defaults
            val defaultProfiles = getDefaultProfiles()
            saveProfiles(defaultProfiles)
            defaultProfiles
        }
    }

    suspend fun saveProfiles(profiles: List<PortalProfile>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(profiles)
            profilesFile.writeText(jsonString)
        } catch (e: Exception) {
            throw RuntimeException("Failed to save profiles", e)
        }
    }

    suspend fun addProfile(profile: PortalProfile) = withContext(Dispatchers.IO) {
        val profiles = loadProfiles().toMutableList()
        profiles.add(profile)
        saveProfiles(profiles)
    }

    suspend fun updateProfile(profile: PortalProfile) = withContext(Dispatchers.IO) {
        val profiles = loadProfiles().toMutableList()
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            profiles[index] = profile
            saveProfiles(profiles)
        }
    }

    suspend fun deleteProfile(profileId: String) = withContext(Dispatchers.IO) {
        val profiles = loadProfiles().toMutableList()
        profiles.removeAll { it.id == profileId }
        saveProfiles(profiles)
    }

    private fun getDefaultProfiles(): List<PortalProfile> {
        return listOf(
            PortalProfile(
                id = "default-airport",
                ssid = ".*Airport.*",
                matchType = com.example.wificaptive.core.profile.MatchType.REGEX,
                triggerUrl = "http://captive.apple.com",
                clickTextExact = null,
                clickTextContains = listOf("Accept", "Connect", "Continue"),
                timeoutMs = 10000L,
                cooldownMs = 5000L,
                enabled = true
            ),
            PortalProfile(
                id = "default-starbucks",
                ssid = ".*Starbucks.*",
                matchType = com.example.wificaptive.core.profile.MatchType.REGEX,
                triggerUrl = "http://www.msftconnecttest.com/redirect",
                clickTextExact = null,
                clickTextContains = listOf("Accept", "Agree", "Continue"),
                timeoutMs = 10000L,
                cooldownMs = 5000L,
                enabled = true
            )
        )
    }
}

