package com.example.wificaptive.service.wifi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wificaptive.R
import com.example.wificaptive.core.driver.DriverRegistry
import com.example.wificaptive.core.error.ConnectivityCheckException
import com.example.wificaptive.core.error.NetworkException
import com.example.wificaptive.core.error.PortalTriggerException
import com.example.wificaptive.core.error.ProfileLoadException
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.core.profile.matchesSsid
import com.example.wificaptive.core.storage.ProfileStorage
import com.example.wificaptive.service.accessibility.PortalAccessibilityService
import com.example.wificaptive.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.net.HttpURLConnection
import java.net.URL

/**
 * Foreground service that monitors Wi-Fi connections and triggers
 * captive portal automation when a matching profile is detected.
 * 
 * This service:
 * - Monitors network state changes via ConnectivityManager
 * - Detects when connected to a Wi-Fi network matching a profile
 * - Triggers the captive portal by making HTTP requests
 * - Optionally validates connectivity and handles reconnections
 * 
 * The service runs in the foreground with a persistent notification.
 */
class WifiMonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var profileStorage: ProfileStorage
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private var currentSsid: String? = null
    private var lastTriggeredProfile: String? = null
    private var lastTriggerTime: Long = 0
    private var lastDisconnectTime: Long = 0
    private var validationJob: Job? = null
    private var activeProfile: PortalProfile? = null
    
    // Debouncing for network state changes
    private var debounceJob: Job? = null
    private val NETWORK_DEBOUNCE_MS = 1000L // 1 second debounce

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            debounceNetworkChange {
                val wasDisconnected = currentSsid == null
                checkWifiAndTrigger()
                
                // If reconnecting to same SSID and reconnection handling is enabled
                if (wasDisconnected && activeProfile != null && activeProfile!!.enableReconnectionHandling) {
                    val now = System.currentTimeMillis()
                    // If disconnected recently (within last 30 seconds), treat as reconnection
                    if ((now - lastDisconnectTime) < 30000) {
                        android.util.Log.d(TAG, "Reconnection detected, re-triggering portal")
                        activeProfile?.let { triggerPortal(it) }
                    }
                }
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                debounceNetworkChange {
                    checkWifiAndTrigger()
                }
            }
        }

        override fun onLost(network: Network) {
            lastDisconnectTime = System.currentTimeMillis()
            currentSsid = null
            activeProfile = null
            validationJob?.cancel()
            validationJob = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        profileStorage = ProfileStorage(applicationContext)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Initial check
        checkWifiAndTrigger()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        debounceJob?.cancel()
        validationJob?.cancel()
    }
    
    /**
     * Debounce network state changes to prevent rapid repeated triggers
     */
    private fun debounceNetworkChange(action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = serviceScope.launch(Dispatchers.IO) {
            delay(NETWORK_DEBOUNCE_MS)
            action()
        }
    }

    private fun checkWifiAndTrigger() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val ssid = getCurrentSsid()
                if (ssid == null || ssid == currentSsid) {
                    return@launch
                }
                
                currentSsid = ssid
                val profiles = try {
                    profileStorage.loadProfiles()
                } catch (e: ProfileLoadException) {
                    // Log error but continue with empty list
                    android.util.Log.e(TAG, "Failed to load profiles: ${e.message}", e)
                    emptyList()
                }
                val matchingProfile = profiles.firstOrNull { profile ->
                    profile.enabled && matchesSsid(profile, ssid)
                }
                
                if (matchingProfile != null) {
                    // Check cooldown
                    val now = System.currentTimeMillis()
                    if (matchingProfile.id == lastTriggeredProfile && 
                        (now - lastTriggerTime) < matchingProfile.cooldownMs) {
                        return@launch
                    }
                    
                    activeProfile = matchingProfile
                    triggerPortal(matchingProfile)
                    lastTriggeredProfile = matchingProfile.id
                    lastTriggerTime = now
                    
                    // Start periodic connectivity validation if enabled
                    if (matchingProfile.enableConnectivityValidation) {
                        startConnectivityValidation(matchingProfile)
                    }
                } else {
                    // No matching profile, stop validation
                    validationJob?.cancel()
                    validationJob = null
                    activeProfile = null
                }
            } catch (e: ProfileLoadException) {
                android.util.Log.e(TAG, "Error loading profiles: ${e.message}", e)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error checking Wi-Fi: ${e.message}", e)
            }
        }
    }

    private fun getCurrentSsid(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val network = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                    val wifiInfo = wifiManager.connectionInfo
                    wifiInfo?.ssid?.removeSurrounding("\"")
                } else {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                val wifiInfo = wifiManager.connectionInfo
                wifiInfo?.ssid?.removeSurrounding("\"")
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun triggerPortal(profile: PortalProfile) {
        serviceScope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                // Trigger the captive portal by making a request
                val url = URL(profile.triggerUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.instanceFollowRedirects = false
                connection.connect()
                
                // Use driver registry to handle portal
                val driver = DriverRegistry.getDefaultDriver()
                if (driver != null && driver.isAvailable()) {
                    driver.handlePortal(profile)
                } else {
                    // Fallback to direct accessibility service call for backward compatibility
                    PortalAccessibilityService.triggerPortalHandling(profile)
                }
            } catch (e: java.net.SocketTimeoutException) {
                throw PortalTriggerException("Connection timeout while triggering portal", e)
            } catch (e: java.net.UnknownHostException) {
                throw PortalTriggerException("Cannot resolve host: ${profile.triggerUrl}", e)
            } catch (e: java.io.IOException) {
                throw PortalTriggerException("Network I/O error: ${e.message}", e)
            } catch (e: Exception) {
                throw PortalTriggerException("Unexpected error triggering portal: ${e.message}", e)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun startConnectivityValidation(profile: PortalProfile) {
        // Cancel existing validation job
        validationJob?.cancel()
        
        validationJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive && activeProfile?.id == profile.id) {
                delay(profile.validationIntervalMs)
                
                if (!isActive) break
                
                // Check if we're still connected to the same SSID
                val currentSsid = getCurrentSsid()
                if (currentSsid == null || !matchesSsid(profile, currentSsid)) {
                    break
                }
                
                // Validate connectivity by checking if captive portal is active
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(profile.triggerUrl)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.instanceFollowRedirects = false
                    connection.connect()
                    
                    val responseCode = connection.responseCode
                    // If we get redirected (302/307) or get a captive portal response, re-trigger
                    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        (responseCode == HttpURLConnection.HTTP_OK && connection.url.toString() != profile.triggerUrl)) {
                        android.util.Log.d(TAG, "Connectivity validation detected portal, re-triggering")
                        triggerPortal(profile)
                    }
                } catch (e: Exception) {
                    // Log connectivity check failure but don't throw - this is expected if portal is active
                    android.util.Log.d(TAG, "Connectivity validation check failed: ${e.message}", e)
                    // If connection fails, might need to re-authenticate
                    try {
                        triggerPortal(profile)
                    } catch (triggerException: PortalTriggerException) {
                        android.util.Log.e(TAG, "Failed to re-trigger portal after connectivity check: ${triggerException.message}", triggerException)
                    }
                } finally {
                    connection?.disconnect()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wi-Fi Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi Captive Portal Monitor")
            .setContentText("Monitoring for captive portals")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "WifiMonitorService"
        private const val CHANNEL_ID = "wifi_monitor_channel"
        private const val NOTIFICATION_ID = 1
    }
}

