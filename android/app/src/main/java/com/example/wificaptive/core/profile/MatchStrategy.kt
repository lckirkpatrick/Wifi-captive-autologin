package com.example.wificaptive.core.profile

/**
 * Strategy interface for SSID matching algorithms.
 * 
 * This allows new match types to be added without modifying existing code.
 * Each match type implements this interface and is registered in MatchStrategyRegistry.
 */
interface MatchStrategy {
    /**
     * Determines if the given SSID matches the profile's SSID pattern
     * 
     * @param profile The portal profile containing the SSID pattern
     * @param ssid The SSID to check against the profile
     * @return true if the SSID matches, false otherwise
     */
    fun matches(profile: PortalProfile, ssid: String): Boolean
}

/**
 * Registry for match strategies.
 * 
 * Allows registration of new match types at runtime without modifying existing code.
 */
object MatchStrategyRegistry {
    private val strategies = mutableMapOf<MatchType, MatchStrategy>()
    
    init {
        // Register built-in strategies
        register(MatchType.EXACT, ExactMatchStrategy())
        register(MatchType.CONTAINS, ContainsMatchStrategy())
        register(MatchType.REGEX, RegexMatchStrategy())
    }
    
    /**
     * Register a new match strategy for a match type
     */
    fun register(matchType: MatchType, strategy: MatchStrategy) {
        strategies[matchType] = strategy
    }
    
    /**
     * Get the strategy for a given match type
     */
    fun getStrategy(matchType: MatchType): MatchStrategy {
        return strategies[matchType] 
            ?: throw IllegalArgumentException("No strategy registered for match type: $matchType")
    }
    
    /**
     * Check if a strategy is registered for the given match type
     */
    fun hasStrategy(matchType: MatchType): Boolean {
        return strategies.containsKey(matchType)
    }
}

/**
 * Exact match strategy - SSID must exactly match the profile SSID
 */
class ExactMatchStrategy : MatchStrategy {
    override fun matches(profile: PortalProfile, ssid: String): Boolean {
        return ssid == profile.ssid
    }
}

/**
 * Contains match strategy - SSID must contain the profile SSID (case-insensitive)
 */
class ContainsMatchStrategy : MatchStrategy {
    override fun matches(profile: PortalProfile, ssid: String): Boolean {
        return ssid.contains(profile.ssid, ignoreCase = true)
    }
}

/**
 * Regex match strategy - SSID must match the profile SSID as a regular expression (case-insensitive)
 */
class RegexMatchStrategy : MatchStrategy {
    override fun matches(profile: PortalProfile, ssid: String): Boolean {
        return try {
            Regex(profile.ssid, RegexOption.IGNORE_CASE).containsMatchIn(ssid)
        } catch (e: Exception) {
            // If regex is invalid, fall back to false
            false
        }
    }
}

