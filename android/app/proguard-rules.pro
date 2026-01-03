# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep serialization classes
-keep class com.example.wificaptive.core.profile.** { *; }

# Keep data classes
-keep class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

