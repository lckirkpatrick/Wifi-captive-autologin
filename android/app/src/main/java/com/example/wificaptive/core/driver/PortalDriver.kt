package com.example.wificaptive.core.driver

import com.example.wificaptive.core.profile.PortalProfile

/**
 * Interface for portal automation drivers.
 * 
 * Different drivers can implement different automation strategies:
 * - AccessibilityServiceDriver: Uses Android Accessibility Service (current implementation)
 * - HttpAutomatorDriver: Makes HTTP requests to portal endpoints
 * - WebViewAutomatorDriver: Uses WebView to interact with portal pages
 * 
 * New drivers can be added without modifying existing code by implementing this interface
 * and registering with DriverRegistry.
 */
interface PortalDriver {
    /**
     * Handle the captive portal for the given profile
     * 
     * @param profile The portal profile to handle
     * @return true if portal was successfully handled, false otherwise
     */
    suspend fun handlePortal(profile: PortalProfile): Boolean
    
    /**
     * Get the name of this driver
     */
    fun getName(): String
    
    /**
     * Check if this driver is available/ready to use
     */
    fun isAvailable(): Boolean
}

/**
 * Registry for portal drivers.
 * 
 * Allows registration of new drivers at runtime without modifying existing code.
 */
object DriverRegistry {
    private val drivers = mutableMapOf<String, PortalDriver>()
    private var defaultDriverName: String? = null
    
    /**
     * Register a new driver
     */
    fun register(driver: PortalDriver) {
        drivers[driver.getName()] = driver
        // Set as default if it's the first one
        if (defaultDriverName == null) {
            defaultDriverName = driver.getName()
        }
    }
    
    /**
     * Get a driver by name
     */
    fun getDriver(name: String): PortalDriver? {
        return drivers[name]
    }
    
    /**
     * Get the default driver
     */
    fun getDefaultDriver(): PortalDriver? {
        return defaultDriverName?.let { drivers[it] }
    }
    
    /**
     * Set the default driver
     */
    fun setDefaultDriver(name: String) {
        if (drivers.containsKey(name)) {
            defaultDriverName = name
        } else {
            throw IllegalArgumentException("Driver not found: $name")
        }
    }
    
    /**
     * Get all available drivers
     */
    fun getAvailableDrivers(): List<PortalDriver> {
        return drivers.values.filter { it.isAvailable() }
    }
    
    /**
     * Get all registered drivers (including unavailable ones)
     */
    fun getAllDrivers(): List<PortalDriver> {
        return drivers.values.toList()
    }
}

