package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.jackets.db.JacketsDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.placements.manager.PlacementManager
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementSkipConfirmation
import com.perrigogames.life4ddr.nextgen.util.Destination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PlacementListViewModel(
    private val firstRunSettingsManager: FirstRunSettings,
    private val placementManager: PlacementManager,
    private val jacketsDatabaseHelper: JacketsDatabaseHelper,
) : ViewModel(), KoinComponent {

    private val _screenData = MutableStateFlow(placementManager.createUiData())
    val screenData: StateFlow<UIPlacementListScreen> = _screenData.asStateFlow()

    private val _events = MutableSharedFlow<PlacementListEvent>()
    val events: SharedFlow<PlacementListEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                placementManager.placements,
                jacketsDatabaseHelper.updates
            ) { placements, _ -> placementManager.createUiData(placements) }
                .collect(_screenData)
        }
    }

    fun handleInput(input: PlacementListInput) = when(input) {
        is PlacementListInput.PlacementSelected -> {
            emitNavigationEvent(
                destination = FirstRunDestination.PlacementDetails(input.placementId),
                popExisting = false
            )
        }
        PlacementListInput.GoToRanksScreen -> {
            firstRunSettingsManager.setInitState(InitState.RANKS)
            emitNavigationEvent(destination = FirstRunDestination.InitialRankList)
        }
        PlacementListInput.SkipPlacement -> {
            _screenData.value = _screenData.value.copy(skipConfirmation = UIPlacementSkipConfirmation())
        }
        PlacementListInput.SkipPlacementConfirm -> {
            firstRunSettingsManager.setInitState(InitState.DONE)
            emitNavigationEvent(destination = FirstRunDestination.MainScreen)
        }
        PlacementListInput.SkipPlacementCancel -> {
            _screenData.value = _screenData.value.copy(skipConfirmation = null)
        }
    }

    private fun emitNavigationEvent(destination: Destination, popExisting: Boolean = true) {
        viewModelScope.launch {
            _events.emit(PlacementListEvent.Navigate(destination, popExisting))
        }
    }
}