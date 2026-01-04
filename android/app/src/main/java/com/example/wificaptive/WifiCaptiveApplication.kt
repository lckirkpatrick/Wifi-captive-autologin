package com.example.wificaptive

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import com.example.wificaptive.ui.MainActivity
import com.example.wificaptive.ui.OnboardingActivity

class WifiCaptiveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Check if this is first launch and show onboarding
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = !sharedPrefs.getBoolean("onboarding_completed", false)
        
        if (isFirstLaunch) {
            // Set up activity lifecycle callback to show onboarding
            registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: android.app.Activity, savedInstanceState: android.os.Bundle?) {
                    if (activity is MainActivity && isFirstLaunch) {
                        val intent = Intent(activity, OnboardingActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
                
                override fun onActivityStarted(activity: android.app.Activity) {}
                override fun onActivityResumed(activity: android.app.Activity) {}
                override fun onActivityPaused(activity: android.app.Activity) {}
                override fun onActivityStopped(activity: android.app.Activity) {}
                override fun onActivitySaveInstanceState(activity: android.app.Activity, outState: android.os.Bundle) {}
                override fun onActivityDestroyed(activity: android.app.Activity) {}
            })
        }
    }
}
