package com.example.wificaptive.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wificaptive.R
import com.example.wificaptive.core.profile.MatchType
import com.example.wificaptive.core.profile.PortalProfile
import com.google.android.material.card.MaterialCardView

class ProfileAdapter(
    private var profiles: List<PortalProfile>,
    private val onProfileClick: (PortalProfile) -> Unit,
    private val onProfileToggle: (PortalProfile, Boolean) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    fun updateProfiles(newProfiles: List<PortalProfile>) {
        profiles = newProfiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount() = profiles.size

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSsid: TextView = itemView.findViewById(R.id.textViewSsid)
        private val textViewMatchType: TextView = itemView.findViewById(R.id.textViewMatchType)
        private val textViewTriggerUrl: TextView = itemView.findViewById(R.id.textViewTriggerUrl)
        private val switchEnabled: Switch = itemView.findViewById(R.id.switchEnabled)

        fun bind(profile: PortalProfile) {
            textViewSsid.text = profile.ssid
            textViewMatchType.text = "Match: ${profile.matchType.name}"
            textViewTriggerUrl.text = profile.triggerUrl
            switchEnabled.isChecked = profile.enabled

            itemView.setOnClickListener {
                onProfileClick(profile)
            }

            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onProfileToggle(profile, isChecked)
            }
        }
    }
}

