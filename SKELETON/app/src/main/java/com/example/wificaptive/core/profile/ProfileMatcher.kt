package com.example.wificaptive.core.profile

fun matchesSsid(profile: PortalProfile, ssid: String): Boolean =
    when (profile.matchType) {
        MatchType.EXACT -> ssid == profile.ssid
        MatchType.CONTAINS -> ssid.contains(profile.ssid, ignoreCase = true)
        MatchType.REGEX -> Regex(profile.ssid, RegexOption.IGNORE_CASE)
            .containsMatchIn(ssid)
    }
