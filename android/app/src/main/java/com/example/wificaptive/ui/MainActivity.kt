package com.example.wificaptive.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wificaptive.R
import com.example.wificaptive.core.error.AppException
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.core.storage.ProfileStorage
import com.example.wificaptive.service.wifi.WifiMonitorService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var profileStorage: ProfileStorage
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: ProfileAdapter
    private lateinit var accessibilityBanner: com.google.android.material.card.MaterialCardView
    private var profiles: List<PortalProfile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileStorage = ProfileStorage(applicationContext)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupAccessibilityBanner()
        
        checkAccessibilityService()
        startWifiMonitorService()
    }

    override fun onResume() {
        super.onResume()
        loadProfiles()
        checkAccessibilityService() // Re-check when returning to app
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.emptyView)
        
        adapter = ProfileAdapter(
            profiles = profiles,
            onProfileClick = { profile ->
                openProfileEditor(profile)
            },
            onProfileToggle = { profile, enabled ->
                toggleProfile(profile, enabled)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            openProfileEditor(null)
        }
    }

    private fun setupAccessibilityBanner() {
        accessibilityBanner = findViewById(R.id.accessibilityBanner)
        findViewById<android.widget.Button>(R.id.btnOpenAccessibilitySettings).setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
        findViewById<android.widget.Button>(R.id.btnDismissBanner).setOnClickListener {
            accessibilityBanner.visibility = View.GONE
        }
    }

    private fun loadProfiles() {
        activityScope.launch {
            try {
                profiles = profileStorage.loadProfiles()
                adapter.updateProfiles(profiles)
                updateEmptyView()
            } catch (e: AppException) {
                showError(e.getUserMessage())
                // Use empty list as fallback
                profiles = emptyList()
                adapter.updateProfiles(profiles)
                updateEmptyView()
            } catch (e: Exception) {
                showError("An unexpected error occurred. Please try again.")
                profiles = emptyList()
                adapter.updateProfiles(profiles)
                updateEmptyView()
            }
        }
    }

    private fun updateEmptyView() {
        if (profiles.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun openProfileEditor(profile: PortalProfile?) {
        val intent = Intent(this, ProfileEditorActivity::class.java)
        if (profile != null) {
            intent.putExtra(ProfileEditorActivity.EXTRA_PROFILE_ID, profile.id)
        }
        startActivity(intent)
    }
    
    private fun showTemplateSelectionDialog() {
        val templates = com.example.wificaptive.core.profile.ProfileTemplates.getAllTemplates()
        val templateNames = templates.map { "${it.name} - ${it.description}" }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.select_template)
            .setItems(templateNames.toTypedArray()) { _, which ->
                val selectedTemplate = templates[which]
                val intent = Intent(this, ProfileEditorActivity::class.java)
                intent.putExtra(ProfileEditorActivity.EXTRA_TEMPLATE_NAME, selectedTemplate.name)
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun toggleProfile(profile: PortalProfile, enabled: Boolean) {
        activityScope.launch {
            try {
                val updatedProfile = profile.copy(enabled = enabled)
                profileStorage.updateProfile(updatedProfile)
                loadProfiles()
            } catch (e: AppException) {
                showError(e.getUserMessage())
            } catch (e: Exception) {
                showError("Failed to update profile. Please try again.")
            }
        }
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun checkAccessibilityService() {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )
        
        val serviceEnabled = enabledServices.any { service ->
            service.resolveInfo.serviceInfo.packageName == packageName
        }
        
        if (!serviceEnabled) {
            showAccessibilityBanner()
            // Show dialog only on first launch or if user hasn't dismissed banner
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val hasShownDialog = prefs.getBoolean("accessibility_dialog_shown", false)
            if (!hasShownDialog) {
                showAccessibilityDialog()
                prefs.edit().putBoolean("accessibility_dialog_shown", true).apply()
            }
        } else {
            hideAccessibilityBanner()
        }
    }

    private fun showAccessibilityBanner() {
        accessibilityBanner.visibility = View.VISIBLE
    }

    private fun hideAccessibilityBanner() {
        accessibilityBanner.visibility = View.GONE
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.accessibility_explanation_title)
            .setMessage(R.string.accessibility_explanation_message)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(R.string.dismiss, null)
            .setCancelable(true)
            .show()
    }

    private fun startWifiMonitorService() {
        val intent = Intent(this, WifiMonitorService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_accessibility -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                true
            }
            R.id.action_template -> {
                showTemplateSelectionDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

