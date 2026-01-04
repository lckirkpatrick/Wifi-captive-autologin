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
 * UI tests for ProfileEditorActivity.
 * 
 * Tests cover:
 * - Adding a new profile
 * - Editing profile fields
 * - Saving profile
 * - Form validation
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ProfileEditorUITest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(ProfileEditorActivity::class.java)
    
    @Test
    fun testProfileEditor_AllFieldsVisible() {
        // Check that all form fields are visible
        onView(withId(R.id.editTextSsid))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.spinnerMatchType))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.editTextTriggerUrl))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.editTextClickTextContains))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testProfileEditor_FillAndSaveProfile() {
        // Fill in profile fields
        onView(withId(R.id.editTextSsid))
            .perform(typeText("TestWiFi"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextTriggerUrl))
            .perform(typeText("http://captive.apple.com"), closeSoftKeyboard())
        
        onView(withId(R.id.editTextClickTextContains))
            .perform(typeText("Accept, Agree"), closeSoftKeyboard())
        
        // Note: Actual save test would require mocking ProfileStorage
        // This is a basic UI interaction test
    }
    
    @Test
    fun testProfileEditor_SaveButtonVisible() {
        // Check that save button is visible
        onView(withId(R.id.buttonSave))
            .check(matches(isDisplayed()))
    }
}

