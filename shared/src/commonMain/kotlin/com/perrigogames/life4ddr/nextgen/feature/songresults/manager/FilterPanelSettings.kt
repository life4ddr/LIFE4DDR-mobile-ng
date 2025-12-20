package com.perrigogames.life4ddr.nextgen.feature.songresults.manager

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.SerialFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.toSerialFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings.Companion.KEY_FILTER_DIFFICULTY_RANGE
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings.Companion.KEY_FILTER_SHOW_DIFF_CLASSES
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings.Companion.KEY_FILTER_SHOW_PLAY_STYLES
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings.Companion.KEY_SONG_LIST_FILTER_STATE
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

interface FilterPanelSettings {

    val songListFilterState: Flow<FilterState>
    fun setSongListFilterState(state: FilterState)
    fun resetFilterState()

    val showPlayStyleSelector: Flow<Boolean>
    fun setShowPlayStyleSelector(enabled: Boolean)

    val useDifficultyRange: Flow<Boolean>
    fun setUseDifficultyRange(enabled: Boolean)

    val showDiffClasses: Flow<Boolean>
    fun setShowDiffClasses(enabled: Boolean)

    val filterFlags: Flow<FilterFlags> get() = combine(
        showPlayStyleSelector,
        useDifficultyRange,
        showDiffClasses
    ) { showPlayStyleSelector, useDifficultyRange, showDiffClasses ->
        FilterFlags(showPlayStyleSelector, useDifficultyRange, showDiffClasses)
    }

    companion object {
        const val KEY_SONG_LIST_FILTER_STATE = "KEY_SONG_LIST_FILTER_STATE"
        const val KEY_FILTER_SHOW_PLAY_STYLES = "KEY_FILTER_SHOW_PLAY_STYLES"
        const val KEY_FILTER_DIFFICULTY_RANGE = "KEY_FILTER_DIFFICULTY_RANGE"
        const val KEY_FILTER_SHOW_DIFF_CLASSES = "KEY_FILTER_SHOW_DIFF_CLASSES"
    }
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultFilterPanelSettings() : SettingsManager(), FilterPanelSettings {
    override val songListFilterState: Flow<FilterState> =
        settings.getStringOrNullFlow(KEY_SONG_LIST_FILTER_STATE)
            .map { it.toFilterState() }

    private fun String?.toFilterState(): FilterState {
        return this?.let { Json.decodeFromString(SerialFilterState.serializer(), it) }
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

    override fun resetFilterState() {
        setSongListFilterState(FilterState())
    }

    override val showPlayStyleSelector: Flow<Boolean> =
        settings.getBooleanFlow(KEY_FILTER_SHOW_PLAY_STYLES, false)
            .distinctUntilChanged()

    override fun setShowPlayStyleSelector(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_FILTER_SHOW_PLAY_STYLES, enabled)
        }
    }

    override val useDifficultyRange: Flow<Boolean> =
        settings.getBooleanFlow(KEY_FILTER_DIFFICULTY_RANGE, true)
            .distinctUntilChanged()

    override fun setUseDifficultyRange(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_FILTER_DIFFICULTY_RANGE, enabled)
            if (!enabled) {
                val currentState = settings.getStringOrNull(KEY_SONG_LIST_FILTER_STATE).toFilterState()
                setSongListFilterState(
                    currentState.copy(
                        chartFilter = currentState.chartFilter.let { filter ->
                            val topDifficultyNumber = filter.difficultyNumberRange.last
                            filter.copy(
                                difficultyNumberRange = IntRange(topDifficultyNumber, topDifficultyNumber)
                            )
                        }
                    )
                )
            }
        }
    }

    override val showDiffClasses: Flow<Boolean> =
        settings.getBooleanFlow(KEY_FILTER_SHOW_DIFF_CLASSES, true)
            .distinctUntilChanged()

    override fun setShowDiffClasses(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_FILTER_SHOW_DIFF_CLASSES, enabled)
        }
    }
}


data class FilterFlags(
    val showPlayStyleSelector: Boolean = false,
    val useDifficultyRange: Boolean = true,
    val showDiffClasses: Boolean = true,
)
