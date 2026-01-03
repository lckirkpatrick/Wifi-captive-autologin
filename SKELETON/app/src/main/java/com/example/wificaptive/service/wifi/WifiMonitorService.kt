package com.example.wificaptive.service.wifi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.wificaptive.R
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.core.profile.matchesSsid
import com.example.wificaptive.core.storage.ProfileStorage
import com.example.wificaptive.service.accessibility.PortalAccessibilityService
import com.example.wificaptive.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class WifiMonitorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var profileStorage: ProfileStorage
    private lateinit var wifiManager: WifiManager
    private lateinit var connectivityManager: ConnectivityManager
    private var currentSsid: String? = null
    private var lastTriggeredProfile: String? = null
    private var lastTriggerTime: Long = 0

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkWifiAndTrigger()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                checkWifiAndTrigger()
            }
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
    }

    private fun checkWifiAndTrigger() {
        serviceScope.launch {
            try {
                val ssid = getCurrentSsid()
                if (ssid == null || ssid == currentSsid) {
                    return@launch
                }
                
                currentSsid = ssid
                val profiles = profileStorage.loadProfiles()
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
                    
                    triggerPortal(matchingProfile)
                    lastTriggeredProfile = matchingProfile.id
                    lastTriggerTime = now
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error checking Wi-Fi", e)
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
            try {
                // Trigger the captive portal by making a request
                val url = URL(profile.triggerUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.instanceFollowRedirects = false
                connection.connect()
                connection.disconnect()
                
                // Notify accessibility service
                PortalAccessibilityService.triggerPortalHandling(profile)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error triggering portal", e)
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

