package com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel

/**
 * Event emitted by Settings screen when the native platform needs to
 * take some kind of action.
 */
sealed class SettingsEvent {
    data class Email(val email: String) : SettingsEvent()
    data class WebLink(val url: String) : SettingsEvent()
}