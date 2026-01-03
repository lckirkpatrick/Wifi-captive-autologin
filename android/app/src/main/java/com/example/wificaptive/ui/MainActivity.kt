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
    private var profiles: List<PortalProfile> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileStorage = ProfileStorage(applicationContext)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        
        checkAccessibilityService()
        startWifiMonitorService()
    }

    override fun onResume() {
        super.onResume()
        loadProfiles()
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

    private fun loadProfiles() {
        activityScope.launch {
            profiles = profileStorage.loadProfiles()
            adapter.updateProfiles(profiles)
            updateEmptyView()
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

    private fun toggleProfile(profile: PortalProfile, enabled: Boolean) {
        activityScope.launch {
            val updatedProfile = profile.copy(enabled = enabled)
            profileStorage.updateProfile(updatedProfile)
            loadProfiles()
        }
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
            showAccessibilityDialog()
        }
    }

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("Accessibility Service Required")
            .setMessage(getString(R.string.enable_accessibility))
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
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
            else -> super.onOptionsItemSelected(item)
        }
    }
}

