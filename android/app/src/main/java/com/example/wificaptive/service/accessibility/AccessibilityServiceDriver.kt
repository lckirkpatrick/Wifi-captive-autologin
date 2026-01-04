package com.example.wificaptive.service.accessibility

import com.example.wificaptive.core.driver.PortalDriver
import com.example.wificaptive.core.profile.PortalProfile
import kotlinx.coroutines.delay

/**
 * PortalDriver implementation using Android Accessibility Service.
 * 
 * This is the current/default driver that uses AccessibilityService to click
 * portal buttons automatically.
 */
class AccessibilityServiceDriver(
    private val accessibilityService: PortalAccessibilityService?
) : PortalDriver {
    
    override suspend fun handlePortal(profile: PortalProfile): Boolean {
        accessibilityService?.let { service ->
            PortalAccessibilityService.triggerPortalHandling(profile)
            // Wait a bit for the service to process
            delay(500)
            return true
        }
        return false
    }
    
    override fun getName(): String = "AccessibilityService"
    
    override fun isAvailable(): Boolean {
        return accessibilityService != null
    }
    
    companion object {
        /**
         * Create and register the accessibility service driver
         */
        fun createAndRegister(accessibilityService: PortalAccessibilityService?) {
            val driver = AccessibilityServiceDriver(accessibilityService)
            com.example.wificaptive.core.driver.DriverRegistry.register(driver)
            // Set as default if no default is set
            if (com.example.wificaptive.core.driver.DriverRegistry.getDefaultDriver() == null) {
                com.example.wificaptive.core.driver.DriverRegistry.setDefaultDriver(driver.getName())
            }
        }
    }
}

