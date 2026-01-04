package com.example.wificaptive.core.profile

/**
 * Matches an SSID against a profile's SSID pattern.
 * 
 * Uses the MatchStrategyRegistry to delegate to the appropriate strategy.
 * This allows new match types to be added without modifying this function.
 * 
 * @param profile The portal profile containing the SSID pattern and match type
 * @param ssid The SSID to check against the profile
 * @return true if the SSID matches the profile's pattern, false otherwise
 */
fun matchesSsid(profile: PortalProfile, ssid: String): Boolean {
    val strategy = MatchStrategyRegistry.getStrategy(profile.matchType)
    return strategy.matches(profile, ssid)
}
