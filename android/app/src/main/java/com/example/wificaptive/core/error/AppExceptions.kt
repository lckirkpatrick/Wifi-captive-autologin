package com.example.wificaptive.core.error

/**
 * Application-specific exception types for better error handling.
 * 
 * These exceptions provide context and allow for user-friendly error messages.
 * All exceptions extend [AppException] which provides a [getUserMessage] method
 * for displaying user-friendly error messages in the UI.
 */

/**
 * Base exception for all application errors.
 * 
 * Provides a mechanism for converting technical error messages into
 * user-friendly messages suitable for display in the UI.
 * 
 * @param message Technical error message for logging/debugging
 * @param cause The underlying exception that caused this error (optional)
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /**
     * User-friendly error message to display in UI
     */
    abstract fun getUserMessage(): String
}

/**
 * Storage-related exceptions
 */
sealed class StorageException(message: String, cause: Throwable? = null) : AppException(message, cause) {
    override fun getUserMessage(): String = "Failed to save or load profiles. Please try again."
}

class ProfileLoadException(message: String, cause: Throwable? = null) : StorageException(message, cause) {
    override fun getUserMessage(): String = "Failed to load profiles. Using default profiles."
}

class ProfileSaveException(message: String, cause: Throwable? = null) : StorageException(message, cause) {
    override fun getUserMessage(): String = "Failed to save profile. Please check storage permissions."
}

/**
 * Network-related exceptions
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : AppException(message, cause) {
    override fun getUserMessage(): String = "Network error occurred. Please check your connection."
}

class PortalTriggerException(message: String, cause: Throwable? = null) : NetworkException(message, cause) {
    override fun getUserMessage(): String = "Failed to trigger captive portal. Please check your Wi-Fi connection."
}

class ConnectivityCheckException(message: String, cause: Throwable? = null) : NetworkException(message, cause) {
    override fun getUserMessage(): String = "Failed to check internet connectivity."
}

/**
 * Accessibility service exceptions
 */
sealed class AccessibilityException(message: String, cause: Throwable? = null) : AppException(message, cause)

class AccessibilityServiceException(message: String, cause: Throwable? = null) : AccessibilityException(message, cause) {
    override fun getUserMessage(): String = "Accessibility service is not enabled. Please enable it in Settings → Accessibility."
}

class PortalClickException(message: String, cause: Throwable? = null) : AccessibilityException(message, cause) {
    override fun getUserMessage(): String = "Failed to automatically click portal button. Please try manually."
}

/**
 * Profile validation exceptions
 */
sealed class ValidationException(message: String, cause: Throwable? = null) : AppException(message, cause)

class InvalidProfileException(message: String, cause: Throwable? = null) : ValidationException(message, cause) {
    override fun getUserMessage(): String = "Invalid profile configuration: $message"
}

class MissingRequiredFieldException(private val fieldName: String, cause: Throwable? = null) : ValidationException("Missing required field: $fieldName", cause) {
    override fun getUserMessage(): String = "Please fill in all required fields: $fieldName"
}

/**
 * Wi-Fi related exceptions
 */
sealed class WifiException(message: String, cause: Throwable? = null) : AppException(message, cause) {
    override fun getUserMessage(): String = "Wi-Fi error occurred. Please check your Wi-Fi settings."
}

class WifiPermissionException(message: String, cause: Throwable? = null) : WifiException(message, cause) {
    override fun getUserMessage(): String = "Location permission is required to scan Wi-Fi networks. Please grant permission in Settings."
}

class WifiNotAvailableException(message: String, cause: Throwable? = null) : WifiException(message, cause) {
    override fun getUserMessage(): String = "Wi-Fi is not available. Please enable Wi-Fi and try again."
}

