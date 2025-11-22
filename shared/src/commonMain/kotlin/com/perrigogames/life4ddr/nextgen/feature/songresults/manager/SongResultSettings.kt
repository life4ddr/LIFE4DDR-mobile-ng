package com.perrigogames.life4ddr.nextgen.feature.songresults.manager

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.SerialFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.toSerialFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings.Companion.KEY_ENABLE_DIFFICULTY_TIERS
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings.Companion.KEY_SHOW_REMOVED_SONGS
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings.Companion.KEY_SONG_LIST_FILTER_STATE
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface SongResultSettings {

    val enableDifficultyTiers: Flow<Boolean>
    fun setEnableDifficultyTiers(enabled: Boolean)

    val songListFilterState: Flow<FilterState>
    fun setSongListFilterState(state: FilterState)

    val showRemovedSongs: Flow<Boolean>
    fun setShowRemovedSongs(enabled: Boolean)

    companion object {
        const val KEY_ENABLE_DIFFICULTY_TIERS = "KEY_ENABLE_DIFFICULTY_TIERS"
        const val KEY_SONG_LIST_FILTER_STATE = "KEY_SONG_LIST_FILTER_STATE"
        const val KEY_SHOW_REMOVED_SONGS = "KEY_SHOW_REMOVED_SONGS"
    }
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultSongResultSettings : SettingsManager(), SongResultSettings {

    override val enableDifficultyTiers: Flow<Boolean> =
        settings.getBooleanFlow(KEY_ENABLE_DIFFICULTY_TIERS, false)
            .distinctUntilChanged()

    override fun setEnableDifficultyTiers(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_ENABLE_DIFFICULTY_TIERS, enabled)
        }
    }

    override val songListFilterState: Flow<FilterState> =
        settings.getStringOrNullFlow(KEY_SONG_LIST_FILTER_STATE)
            .map { state ->
                state?.let { Json.decodeFromString(SerialFilterState.serializer(), it) }
                    ?.toFilterState()
                    ?: FilterState()
            }

    override fun setSongListFilterState(state: FilterState) {
        mainScope.launch {
            settings.putString(
                KEY_SONG_LIST_FILTER_STATE,
                Json.encodeToString(SerialFilterState.serializer(), state.toSerialFilterState())
            )
        }
    }

    override val showRemovedSongs: Flow<Boolean> =
        settings.getBooleanFlow(KEY_SHOW_REMOVED_SONGS, false)
            .distinctUntilChanged()

    override fun setShowRemovedSongs(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_SHOW_REMOVED_SONGS, enabled)
        }
    }
}
