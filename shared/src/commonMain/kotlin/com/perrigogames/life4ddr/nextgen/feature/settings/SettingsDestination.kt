package com.perrigogames.life4ddr.nextgen.feature.settings

import com.perrigogames.life4ddr.nextgen.util.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class SettingsDestination(override val baseRoute: String) : Destination {
    @Serializable data object SongLock : SettingsDestination("song_lock")
    @Serializable data object Credits : SettingsDestination("credits")
}