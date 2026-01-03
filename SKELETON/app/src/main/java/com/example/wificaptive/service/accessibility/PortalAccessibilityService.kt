package com.example.wificaptive.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.wificaptive.core.profile.PortalProfile
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
                    delay(500) // Small delay to let UI settle
                    attemptClick(profile)
                } finally {
                    isProcessing.set(false)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private suspend fun attemptClick(profile: PortalProfile) {
        val rootNode = rootInActiveWindow ?: return
        
        try {
            val targetNode = findClickableNode(rootNode, profile)
            if (targetNode != null) {
                performClick(targetNode)
                clickPerformed.set(true)
                Log.d(TAG, "Successfully clicked portal button")
                
                // Reset after timeout
                delay(profile.timeoutMs)
                resetState()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attempting click", e)
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
        } else {
            // Try to find parent that's clickable
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    recycleNode(parent)
                    return
                }
                val oldParent = parent
                parent = parent.parent
                recycleNode(oldParent)
            }
        }
    }

    private fun resetState() {
        currentProfile = null
        clickPerformed.set(false)
        isProcessing.set(false)
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
    }

    companion object {
        private const val TAG = "PortalAccessibilityService"
        private var instance: PortalAccessibilityService? = null

        fun triggerPortalHandling(profile: PortalProfile) {
            instance?.setCurrentProfile(profile)
            Log.d(TAG, "Triggered portal handling for profile: ${profile.id}")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Accessibility service destroyed")
    }
}

