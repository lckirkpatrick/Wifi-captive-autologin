package com.example.wificaptive.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.example.wificaptive.R
import com.example.wificaptive.core.config.AppConfig
import com.example.wificaptive.core.logging.AppLogger
import com.example.wificaptive.core.logging.LogContext
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class PortalAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    private var currentProfile: PortalProfile? = null
    private var isProcessing = AtomicBoolean(false)
    private var clickPerformed = AtomicBoolean(false)
    private var retryAttempts = AtomicBoolean(false)
    private var retryCount = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (currentProfile == null || isProcessing.get()) {
            return
        }

        val profile = currentProfile!!
        
        // Only process window state changes and content changes
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }

        // Check if we already clicked for this profile
        if (clickPerformed.get()) {
            return
        }

        serviceScope.launch {
            if (isProcessing.compareAndSet(false, true)) {
                try {
                    delay(AppConfig.ACCESSIBILITY_CLICK_DELAY_MS)
                    attemptClickWithRetry(profile)
                } finally {
                    isProcessing.set(false)
                }
            }
        }
    }

    override fun onInterrupt() {
        AppLogger.d(TAG, "Accessibility service interrupted")
    }

    private suspend fun attemptClickWithRetry(profile: PortalProfile) {
        retryCount = 0
        retryAttempts.set(false)
        
        while (retryCount < AppConfig.ACCESSIBILITY_MAX_RETRIES && !clickPerformed.get()) {
            val success = attemptClick(profile)
            
            if (success) {
                clickPerformed.set(true)
                showSuccessNotification(profile)
                val context = LogContext(
                    profileId = profile.id,
                    ssid = profile.ssid,
                    attemptNumber = retryCount + 1,
                    triggerUrl = profile.triggerUrl
                )
                AppLogger.d(TAG, "Successfully clicked portal button", context)
                
                // Reset after timeout
                delay(profile.timeoutMs)
                resetState()
                return
            }
            
            // If failed and we have retries left, wait and retry
            if (retryCount < AppConfig.ACCESSIBILITY_MAX_RETRIES - 1) {
                retryCount++
                val delayMs = if (retryCount == 1) AppConfig.ACCESSIBILITY_RETRY_DELAY_1_MS else AppConfig.ACCESSIBILITY_RETRY_DELAY_2_MS
                val context = LogContext(
                    profileId = profile.id,
                    ssid = profile.ssid,
                    attemptNumber = retryCount,
                    triggerUrl = profile.triggerUrl,
                    additionalData = mapOf("retryDelayMs" to delayMs.toString())
                )
                AppLogger.d(TAG, "Click attempt failed, retrying", context)
                delay(delayMs)
            } else {
                retryCount++
            }
        }
        
        // If we exhausted retries, show failure notification
        if (!clickPerformed.get()) {
            showFailureNotification(profile)
            val context = LogContext(
                profileId = profile.id,
                ssid = profile.ssid,
                attemptNumber = retryCount,
                triggerUrl = profile.triggerUrl
            )
            AppLogger.w(TAG, "Failed to click portal button after all retries", context)
            resetState()
        }
    }
    
    private suspend fun attemptClick(profile: PortalProfile): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        try {
            val targetNode = findClickableNode(rootNode, profile)
            if (targetNode != null) {
                try {
                    performClick(targetNode)
                    return true
                } finally {
                    // Always recycle the targetNode after use
                    recycleNode(targetNode)
                }
            }
            return false
        } catch (e: Exception) {
            val context = LogContext(
                profileId = profile.id,
                ssid = profile.ssid,
                triggerUrl = profile.triggerUrl
            )
            AppLogger.e(TAG, "Error attempting click", context, e)
            return false
        } finally {
            recycleNode(rootNode)
        }
    }

    private fun findClickableNode(root: AccessibilityNodeInfo, profile: PortalProfile): AccessibilityNodeInfo? {
        // First try exact match
        profile.clickTextExact?.let { exactText ->
            val node = findNodeByText(root, exactText, exact = true)
            if (node != null && node.isClickable) {
                return node
            } else if (node != null) {
                // Recycle node if it's not clickable
                recycleNode(node)
            }
        }

        // Then try contains match
        for (containsText in profile.clickTextContains) {
            val node = findNodeByText(root, containsText, exact = false)
            if (node != null && node.isClickable) {
                return node
            } else if (node != null) {
                // Recycle node if it's not clickable
                recycleNode(node)
            }
        }

        return null
    }

    private fun findNodeByText(
        root: AccessibilityNodeInfo,
        text: String,
        exact: Boolean
    ): AccessibilityNodeInfo? {
        val queue = mutableListOf<AccessibilityNodeInfo>()
        queue.add(root)
        var isRoot = true

        while (queue.isNotEmpty()) {
            val node = queue.removeAt(0)
            val wasRoot = isRoot
            isRoot = false
            
            val nodeText = node.text?.toString() ?: ""
            val contentDescription = node.contentDescription?.toString() ?: ""
            
            val matches = if (exact) {
                nodeText.equals(text, ignoreCase = true) ||
                contentDescription.equals(text, ignoreCase = true)
            } else {
                nodeText.contains(text, ignoreCase = true) ||
                contentDescription.contains(text, ignoreCase = true)
            }

            if (matches && node.isClickable) {
                // Recycle all remaining nodes in queue before returning
                queue.forEach { recycleNode(it) }
                return node
            }

            // Add children to queue
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    queue.add(child)
                }
            }
            
            // Recycle the current node if it's not the root and we're not returning it
            if (!wasRoot) {
                recycleNode(node)
            }
        }

        return null
    }

    private fun performClick(node: AccessibilityNodeInfo) {
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            // Note: node recycling is handled by the caller (attemptClick)
        } else {
            // Try to find parent that's clickable
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    recycleNode(parent)
                    // Recycle the original node since we found a clickable parent
                    recycleNode(node)
                    return
                }
                val oldParent = parent
                parent = parent.parent
                recycleNode(oldParent)
            }
            // If no clickable parent found, recycle the original node
            recycleNode(node)
        }
    }

    private fun resetState() {
        currentProfile = null
        clickPerformed.set(false)
        isProcessing.set(false)
        retryCount = 0
        retryAttempts.set(false)
    }

    /**
     * Safely recycle an AccessibilityNodeInfo.
     * recycle() is deprecated in API 33+ but still safe to call (it's a no-op).
     * We call it unconditionally for consistency across all API levels.
     */
    @Suppress("DEPRECATION")
    private fun recycleNode(node: AccessibilityNodeInfo) {
        // recycle() is required for API < 33 to prevent memory leaks.
        // On API 33+, it's a no-op but safe to call for consistency.
        node.recycle()
    }

    fun setCurrentProfile(profile: PortalProfile) {
        currentProfile = profile
        clickPerformed.set(false)
        isProcessing.set(false)
        retryCount = 0
        retryAttempts.set(false)
    }
    
    private fun showSuccessNotification(profile: PortalProfile) {
        try {
            createNotificationChannel()
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, AppConfig.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Portal Auto-Login")
                .setContentText("Successfully connected to ${profile.ssid}")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(AppConfig.NOTIFICATION_ID_SUCCESS, notification)
        } catch (e: Exception) {
            val context = LogContext(profileId = profile.id, ssid = profile.ssid)
            AppLogger.e(TAG, "Error showing success notification", context, e)
        }
    }
    
    private fun showFailureNotification(profile: PortalProfile) {
        try {
            createNotificationChannel()
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(this, AppConfig.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Portal Auto-Login Failed")
                .setContentText("Could not auto-login to ${profile.ssid}. Please try manually.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(AppConfig.NOTIFICATION_ID_FAILURE, notification)
        } catch (e: Exception) {
            val context = LogContext(profileId = profile.id, ssid = profile.ssid)
            AppLogger.e(TAG, "Error showing failure notification", context, e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConfig.NOTIFICATION_CHANNEL_ID,
                "Portal Auto-Login",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "PortalAccessibilityService"
        private var instance: PortalAccessibilityService? = null

        fun triggerPortalHandling(profile: PortalProfile) {
            instance?.setCurrentProfile(profile)
            val context = LogContext(profileId = profile.id, ssid = profile.ssid, triggerUrl = profile.triggerUrl)
            AppLogger.d(TAG, "Triggered portal handling", context)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AppLogger.d(TAG, "Accessibility service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        AppLogger.d(TAG, "Accessibility service destroyed")
    }
}

