package com.example.wificaptive.core.logging

import android.util.Log

/**
 * Structured logging interface for the application.
 * 
 * Provides context-aware logging with profile ID, SSID, attempt numbers, etc.
 * This allows for better diagnostics and log export capabilities.
 */
interface StructuredLogger {
    fun logEvent(
        level: LogLevel,
        tag: String,
        message: String,
        context: LogContext? = null,
        throwable: Throwable? = null
    )
}

/**
 * Log levels matching Android's Log levels
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * Context information for structured logging
 */
data class LogContext(
    val profileId: String? = null,
    val ssid: String? = null,
    val attemptNumber: Int? = null,
    val triggerUrl: String? = null,
    val additionalData: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "profileId" to profileId,
            "ssid" to ssid,
            "attemptNumber" to attemptNumber,
            "triggerUrl" to triggerUrl
        )
        map.putAll(additionalData)
        return map.filterValues { it != null }
    }
}

/**
 * Default implementation using Android's Log system
 */
class AndroidStructuredLogger : StructuredLogger {
    override fun logEvent(
        level: LogLevel,
        tag: String,
        message: String,
        context: LogContext?,
        throwable: Throwable?
    ) {
        val contextStr = context?.let { formatContext(it) } ?: ""
        val fullMessage = if (contextStr.isNotEmpty()) {
            "$message | Context: $contextStr"
        } else {
            message
        }

        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, fullMessage, throwable)
            LogLevel.DEBUG -> Log.d(tag, fullMessage, throwable)
            LogLevel.INFO -> Log.i(tag, fullMessage, throwable)
            LogLevel.WARN -> Log.w(tag, fullMessage, throwable)
            LogLevel.ERROR -> Log.e(tag, fullMessage, throwable)
        }
    }

    private fun formatContext(context: LogContext): String {
        val parts = mutableListOf<String>()
        context.profileId?.let { parts.add("profileId=$it") }
        context.ssid?.let { parts.add("ssid=$it") }
        context.attemptNumber?.let { parts.add("attempt=$it") }
        context.triggerUrl?.let { parts.add("url=$it") }
        context.additionalData.forEach { (key, value) -> parts.add("$key=$value") }
        return parts.joinToString(", ")
    }
}

/**
 * Global logger instance
 */
object AppLogger {
    private var logger: StructuredLogger = AndroidStructuredLogger()

    /**
     * Set a custom logger implementation (useful for testing or log export)
     */
    fun setLogger(customLogger: StructuredLogger) {
        logger = customLogger
    }

    /**
     * Get the current logger instance
     */
    fun getLogger(): StructuredLogger = logger

    // Convenience methods
    fun v(tag: String, message: String, context: LogContext? = null) {
        logger.logEvent(LogLevel.VERBOSE, tag, message, context)
    }

    fun d(tag: String, message: String, context: LogContext? = null) {
        logger.logEvent(LogLevel.DEBUG, tag, message, context)
    }

    fun i(tag: String, message: String, context: LogContext? = null) {
        logger.logEvent(LogLevel.INFO, tag, message, context)
    }

    fun w(tag: String, message: String, context: LogContext? = null, throwable: Throwable? = null) {
        logger.logEvent(LogLevel.WARN, tag, message, context, throwable)
    }

    fun e(tag: String, message: String, context: LogContext? = null, throwable: Throwable? = null) {
        logger.logEvent(LogLevel.ERROR, tag, message, context, throwable)
    }
}

