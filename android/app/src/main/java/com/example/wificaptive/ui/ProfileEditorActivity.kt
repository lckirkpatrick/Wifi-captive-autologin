package com.example.wificaptive.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wificaptive.R
import com.example.wificaptive.core.profile.MatchType
import com.example.wificaptive.core.profile.PortalProfile
import com.example.wificaptive.core.storage.ProfileStorage
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileEditorActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var profileStorage: ProfileStorage
    
    private lateinit var editTextSsid: TextInputEditText
    private lateinit var spinnerMatchType: Spinner
    private lateinit var editTextTriggerUrl: TextInputEditText
    private lateinit var editTextClickTextExact: TextInputEditText
    private lateinit var editTextClickTextContains: TextInputEditText
    private lateinit var editTextTimeout: TextInputEditText
    private lateinit var editTextCooldown: TextInputEditText
    private lateinit var switchEnabled: Switch
    private lateinit var switchEnableConnectivityValidation: Switch
    private lateinit var layoutValidationInterval: com.google.android.material.textfield.TextInputLayout
    private lateinit var editTextValidationInterval: TextInputEditText
    private lateinit var switchEnableReconnectionHandling: Switch
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    
    private var currentProfile: PortalProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_editor)

        profileStorage = ProfileStorage(applicationContext)
        
        setupToolbar()
        setupViews()
        setupSpinner()
        loadProfile()
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupViews() {
        editTextSsid = findViewById(R.id.editTextSsid)
        spinnerMatchType = findViewById(R.id.spinnerMatchType)
        editTextTriggerUrl = findViewById(R.id.editTextTriggerUrl)
        editTextClickTextExact = findViewById(R.id.editTextClickTextExact)
        editTextClickTextContains = findViewById(R.id.editTextClickTextContains)
        editTextTimeout = findViewById(R.id.editTextTimeout)
        editTextCooldown = findViewById(R.id.editTextCooldown)
        switchEnabled = findViewById(R.id.switchEnabled)
        switchEnableConnectivityValidation = findViewById(R.id.switchEnableConnectivityValidation)
        layoutValidationInterval = findViewById(R.id.layoutValidationInterval)
        editTextValidationInterval = findViewById(R.id.editTextValidationInterval)
        switchEnableReconnectionHandling = findViewById(R.id.switchEnableReconnectionHandling)
        buttonSave = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)

        // Show/hide validation interval based on connectivity validation switch
        switchEnableConnectivityValidation.setOnCheckedChangeListener { _, isChecked ->
            layoutValidationInterval.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        buttonSave.setOnClickListener { saveProfile() }
        buttonDelete.setOnClickListener { deleteProfile() }
    }

    private fun setupSpinner() {
        val matchTypes = arrayOf(MatchType.EXACT, MatchType.CONTAINS, MatchType.REGEX)
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            matchTypes.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMatchType.adapter = adapter
    }

    private fun loadProfile() {
        val profileId = intent.getStringExtra(EXTRA_PROFILE_ID)
        if (profileId != null) {
            activityScope.launch {
                val profiles = profileStorage.loadProfiles()
                currentProfile = profiles.firstOrNull { it.id == profileId }
                currentProfile?.let { populateFields(it) }
                
                if (currentProfile == null) {
                    finish()
                } else {
                    buttonDelete.visibility = android.view.View.VISIBLE
                }
            }
        } else {
            buttonDelete.visibility = android.view.View.GONE
        }
    }

    private fun populateFields(profile: PortalProfile) {
        editTextSsid.setText(profile.ssid)
        spinnerMatchType.setSelection(
            when (profile.matchType) {
                MatchType.EXACT -> 0
                MatchType.CONTAINS -> 1
                MatchType.REGEX -> 2
            }
        )
        editTextTriggerUrl.setText(profile.triggerUrl)
        editTextClickTextExact.setText(profile.clickTextExact ?: "")
        editTextClickTextContains.setText(profile.clickTextContains.joinToString(", "))
        editTextTimeout.setText(profile.timeoutMs.toString())
        editTextCooldown.setText(profile.cooldownMs.toString())
        switchEnabled.isChecked = profile.enabled
        switchEnableConnectivityValidation.isChecked = profile.enableConnectivityValidation
        editTextValidationInterval.setText((profile.validationIntervalMs / 60000).toString()) // Convert to minutes
        layoutValidationInterval.visibility = if (profile.enableConnectivityValidation) android.view.View.VISIBLE else android.view.View.GONE
        switchEnableReconnectionHandling.isChecked = profile.enableReconnectionHandling
    }

    private fun saveProfile() {
        val ssid = editTextSsid.text?.toString()?.trim()
        val triggerUrl = editTextTriggerUrl.text?.toString()?.trim()
        val timeoutText = editTextTimeout.text?.toString()?.trim()
        val cooldownText = editTextCooldown.text?.toString()?.trim()

        if (ssid.isNullOrEmpty() || triggerUrl.isNullOrEmpty()) {
            showError("SSID and Trigger URL are required")
            return
        }

        val timeout = timeoutText?.toLongOrNull() ?: 10000L
        val cooldown = cooldownText?.toLongOrNull() ?: 5000L
        val validationIntervalMinutes = editTextValidationInterval.text?.toString()?.trim()?.toLongOrNull() ?: 5L
        val validationIntervalMs = validationIntervalMinutes * 60000L // Convert to milliseconds

        val matchType = when (spinnerMatchType.selectedItemPosition) {
            0 -> MatchType.EXACT
            1 -> MatchType.CONTAINS
            2 -> MatchType.REGEX
            else -> MatchType.EXACT
        }

        val clickTextExact = editTextClickTextExact.text?.toString()?.trim()
            .takeIf { !it.isNullOrEmpty() }

        val clickTextContains = editTextClickTextContains.text?.toString()?.trim()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: listOf("Accept", "Connect", "Continue")

        val profile = currentProfile?.copy(
            ssid = ssid,
            matchType = matchType,
            triggerUrl = triggerUrl,
            clickTextExact = clickTextExact,
            clickTextContains = clickTextContains,
            timeoutMs = timeout,
            cooldownMs = cooldown,
            enabled = switchEnabled.isChecked,
            enableConnectivityValidation = switchEnableConnectivityValidation.isChecked,
            validationIntervalMs = validationIntervalMs,
            enableReconnectionHandling = switchEnableReconnectionHandling.isChecked
        ) ?: PortalProfile(
            id = UUID.randomUUID().toString(),
            ssid = ssid,
            matchType = matchType,
            triggerUrl = triggerUrl,
            clickTextExact = clickTextExact,
            clickTextContains = clickTextContains,
            timeoutMs = timeout,
            cooldownMs = cooldown,
            enabled = switchEnabled.isChecked,
            enableConnectivityValidation = switchEnableConnectivityValidation.isChecked,
            validationIntervalMs = validationIntervalMs,
            enableReconnectionHandling = switchEnableReconnectionHandling.isChecked
        )

        activityScope.launch {
            if (currentProfile == null) {
                profileStorage.addProfile(profile)
            } else {
                profileStorage.updateProfile(profile)
            }
            finish()
        }
    }

    private fun deleteProfile() {
        val profile = currentProfile ?: return

        AlertDialog.Builder(this)
            .setTitle("Delete Profile")
            .setMessage("Are you sure you want to delete this profile?")
            .setPositiveButton("Delete") { _, _ ->
                activityScope.launch {
                    profileStorage.deleteProfile(profile.id)
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_PROFILE_ID = "profile_id"
    }
}

