package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ResultFilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIFilterView
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.toUIFilterView
import com.perrigogames.life4ddr.nextgen.injectLogger
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FilterPanelViewModel : ViewModel(), KoinComponent {

    private val filterSettings: FilterPanelSettings by inject()
    private val songResultSettings: SongResultSettings by inject()
    private val logger: Logger by injectLogger("FilterPanelViewModel")

    private val _state = MutableStateFlow(FilterState())
    val dataState: StateFlow<FilterState> = _state.asStateFlow()
    val uiState: StateFlow<UIFilterView> = combine(
        dataState,
        filterSettings.filterFlags,
    ) { state, flags ->
        logger.d { "FilterPanelViewModel state: $state" }
        state.toUIFilterView(settingsFlags = flags)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UIFilterView())

    init {
        viewModelScope.launch {
            filterSettings.songListFilterState.collect(_state)
        }
    }

    fun handleInput(input: FilterPanelInput) = when(input) {
        is FilterPanelInput.SelectPlayStyle -> {
            mutateChartFilter { it.copy(selectedPlayStyle = input.playStyle) }
        }
        is FilterPanelInput.SetClearTypeRange -> {
            mutateResultFilter { it.copy(clearTypeRange = input.range) }
        }
        is FilterPanelInput.SetDifficultyNumber -> {
            mutateChartFilter { it.copy(difficultyNumberRange = IntRange(input.value, input.value)) }
        }
        is FilterPanelInput.SetDifficultyNumberRange -> {
            mutateChartFilter { it.copy(difficultyNumberRange = input.range) }
        }
        is FilterPanelInput.SetScoreRange -> {
            mutateResultFilter {
                val first = input.first ?: it.scoreRange.first
                val last = input.last ?: it.scoreRange.last
                it.copy(scoreRange = (first .. last))
            }
        }
        is FilterPanelInput.ToggleDifficultyClass -> {
            mutateChartFilter {
                val selection = it.difficultyClassSelection.toMutableSet()
                if (input.selected) {
                    selection.add(input.difficultyClass)
                } else {
                    selection.remove(input.difficultyClass)
                }
                it.copy(difficultyClassSelection = selection.toList())
            }
        }
        is FilterPanelInput.ToggleDifficultyNumberRange -> {
            viewModelScope.launch {
                filterSettings.setUseDifficultyRange(input.enabled)
            }
        }
        FilterPanelInput.ResetFilter -> {
            viewModelScope.launch {
                filterSettings.resetFilterState()
            }
        }
    }

    private fun mutate(block: (FilterState) -> FilterState) {
        viewModelScope.launch {
            val newValue = block(_state.value)
            filterSettings.setSongListFilterState(newValue)
        }
    }

    private fun mutateChartFilter(block: (ChartFilterState) -> ChartFilterState) {
        mutate { it.copy(chartFilter = block(it.chartFilter)) }
    }

    private fun mutateResultFilter(block: (ResultFilterState) -> ResultFilterState) {
        mutate { it.copy(resultFilter = block(it.resultFilter)) }
    }
}