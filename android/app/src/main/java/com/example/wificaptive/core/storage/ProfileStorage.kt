package com.example.wificaptive.core.storage

import android.content.Context
import com.example.wificaptive.core.error.ProfileLoadException
import com.example.wificaptive.core.error.ProfileSaveException
import com.example.wificaptive.core.profile.PortalProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class ProfileStorage(private val context: Context) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val profilesFile: File by lazy {
        File(context.filesDir, "profiles.json")
    }
    
    // Encryption helper (only used if supported)
    private val encryptionHelper: EncryptionHelper? by lazy {
        val helper = EncryptionHelper(context)
        if (helper.isEncryptionSupported()) helper else null
    }
    
    // In-memory cache for profiles
    @Volatile
    private var cachedProfiles: List<PortalProfile>? = null
    @Volatile
    private var cacheTimestamp: Long = 0
    private val cacheLock = Any()

    suspend fun loadProfiles(): List<PortalProfile> = withContext(Dispatchers.IO) {
        // Check cache first
        synchronized(cacheLock) {
            cachedProfiles?.let { return@withContext it }
        }
        
        // Cache miss - load from disk
        val profiles = loadProfilesFromDisk()
        
        // Update cache
        synchronized(cacheLock) {
            cachedProfiles = profiles
            cacheTimestamp = System.currentTimeMillis()
        }
        
        profiles
    }
    
    private suspend fun loadProfilesFromDisk(): List<PortalProfile> = withContext(Dispatchers.IO) {
        try {
            if (!profilesFile.exists()) {
                val defaultProfiles = getDefaultProfiles()
                saveProfilesToDisk(defaultProfiles)
                return@withContext defaultProfiles
            }
            var jsonString = profilesFile.readText()
            if (jsonString.isBlank()) {
                val defaultProfiles = getDefaultProfiles()
                saveProfilesToDisk(defaultProfiles)
                return@withContext defaultProfiles
            }
            
            // Decrypt if encryption is supported and data appears encrypted
            jsonString = decryptIfNeeded(jsonString)
            
            json.decodeFromString<List<PortalProfile>>(jsonString)
        } catch (e: SerializationException) {
            // If deserialization fails, return defaults
            val defaultProfiles = getDefaultProfiles()
            try {
                saveProfilesToDisk(defaultProfiles)
            } catch (saveException: Exception) {
                // If we can't save defaults, just return them
            }
            throw ProfileLoadException("Failed to deserialize profiles: ${e.message}", e)
        } catch (e: IOException) {
            // If file read fails, return defaults
            val defaultProfiles = getDefaultProfiles()
            try {
                saveProfilesToDisk(defaultProfiles)
            } catch (saveException: Exception) {
                // If we can't save defaults, just return them
            }
            throw ProfileLoadException("Failed to read profiles file: ${e.message}", e)
        } catch (e: Exception) {
            // For any other exception, return defaults but log the error
            val defaultProfiles = getDefaultProfiles()
            try {
                saveProfilesToDisk(defaultProfiles)
            } catch (saveException: Exception) {
                // If we can't save defaults, just return them
            }
            throw ProfileLoadException("Unexpected error loading profiles: ${e.message}", e)
        }
    }

    suspend fun saveProfiles(profiles: List<PortalProfile>) = withContext(Dispatchers.IO) {
        saveProfilesToDisk(profiles)
        
        // Update cache after successful save
        synchronized(cacheLock) {
            cachedProfiles = profiles
            cacheTimestamp = System.currentTimeMillis()
        }
    }
    
    private suspend fun saveProfilesToDisk(profiles: List<PortalProfile>) = withContext(Dispatchers.IO) {
        try {
            var jsonString = json.encodeToString(profiles)
            
            // Encrypt if encryption is supported
            jsonString = encryptIfNeeded(jsonString)
            
            profilesFile.writeText(jsonString)
        } catch (e: SerializationException) {
            throw ProfileSaveException("Failed to serialize profiles: ${e.message}", e)
        } catch (e: IOException) {
            throw ProfileSaveException("Failed to write profiles file: ${e.message}", e)
        } catch (e: Exception) {
            throw ProfileSaveException("Unexpected error saving profiles: ${e.message}", e)
        }
    }
    
    /**
     * Invalidate the cache, forcing a reload from disk on next loadProfiles() call
     */
    fun invalidateCache() {
        synchronized(cacheLock) {
            cachedProfiles = null
            cacheTimestamp = 0
        }
    }
    
    /**
     * Encrypts data if encryption is supported
     */
    private fun encryptIfNeeded(data: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && encryptionHelper != null) {
            try {
                encryptionHelper!!.encrypt(data)
            } catch (e: Exception) {
                // If encryption fails, fall back to unencrypted storage
                android.util.Log.w("ProfileStorage", "Encryption failed, using unencrypted storage: ${e.message}")
                data
            }
        } else {
            data
        }
    }
    
    /**
     * Decrypts data if it appears to be encrypted
     */
    private fun decryptIfNeeded(data: String): String {
        // Check if data looks encrypted (Base64 encoded, longer than typical JSON)
        // Simple heuristic: if it's much longer than expected and Base64-like, try decrypting
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && 
                   encryptionHelper != null && 
                   data.length > 200 && // Encrypted data is typically much longer
                   isBase64Like(data)) {
            try {
                encryptionHelper!!.decrypt(data)
            } catch (e: Exception) {
                // If decryption fails, assume it's unencrypted data
                android.util.Log.w("ProfileStorage", "Decryption failed, assuming unencrypted: ${e.message}")
                data
            }
        } else {
            data
        }
    }
    
    /**
     * Simple check if string looks like Base64
     */
    private fun isBase64Like(data: String): Boolean {
        // Base64 strings are typically longer and contain only Base64 characters
        return data.length > 100 && data.matches(Regex("^[A-Za-z0-9+/=]+$"))
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

