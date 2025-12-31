package com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel

import com.perrigogames.life4ddr.nextgen.util.Destination

/**
 * Event emitted by Settings screen when the native platform needs to
 * take some kind of action.
 */
sealed class SettingsEvent {
    data object Close : SettingsEvent()
    data class Navigate(val destination: Destination) : SettingsEvent()
}
