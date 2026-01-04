package com.example.wificaptive.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.wificaptive.R
import com.google.android.material.card.MaterialCardView

/**
 * Onboarding activity for first-time users.
 * 
 * Guides users through:
 * - App introduction
 * - Accessibility service setup
 * - Basic usage instructions
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var sharedPrefs: SharedPreferences
    private var currentStep = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        // Check if onboarding already completed
        if (sharedPrefs.getBoolean("onboarding_completed", false)) {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
            return
        }
        
        setupViews()
        showStep(0)
    }
    
    private fun setupViews() {
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (currentStep < 2) {
                showStep(currentStep + 1)
            } else {
                completeOnboarding()
            }
        }
        
        findViewById<Button>(R.id.btnSkip).setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun showStep(step: Int) {
        currentStep = step
        
        val step1Card = findViewById<MaterialCardView>(R.id.cardStep1)
        val step2Card = findViewById<MaterialCardView>(R.id.cardStep2)
        val step3Card = findViewById<MaterialCardView>(R.id.cardStep3)
        
        when (step) {
            0 -> {
                step1Card.visibility = View.VISIBLE
                step2Card.visibility = View.GONE
                step3Card.visibility = View.GONE
                findViewById<Button>(R.id.btnNext).text = getString(R.string.next)
            }
            1 -> {
                step1Card.visibility = View.GONE
                step2Card.visibility = View.VISIBLE
                step3Card.visibility = View.GONE
                findViewById<Button>(R.id.btnNext).text = getString(R.string.next)
                
                // Set up accessibility button
                findViewById<Button>(R.id.btnOpenAccessibility).setOnClickListener {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
            }
            2 -> {
                step1Card.visibility = View.GONE
                step2Card.visibility = View.GONE
                step3Card.visibility = View.VISIBLE
                findViewById<Button>(R.id.btnNext).text = getString(R.string.get_started)
            }
        }
    }
    
    private fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}

