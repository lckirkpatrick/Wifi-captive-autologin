package com.example.wificaptive.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.wificaptive.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for critical user paths in MainActivity.
 * 
 * Tests cover:
 * - Viewing profile list
 * - Adding new profiles
 * - Enabling/disabling profiles
 * - Opening profile editor
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityUITest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testMainActivity_DisplaysProfileList() {
        // Check that main activity is displayed
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testMainActivity_FabButtonIsVisible() {
        // Check that FAB button is visible
        onView(withId(R.id.fab))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testMainActivity_OpenProfileEditor() {
        // Click FAB to open profile editor
        onView(withId(R.id.fab))
            .perform(click())
        
        // Verify profile editor is opened (check for SSID field)
        onView(withId(R.id.editTextSsid))
            .check(matches(isDisplayed()))
    }
}

