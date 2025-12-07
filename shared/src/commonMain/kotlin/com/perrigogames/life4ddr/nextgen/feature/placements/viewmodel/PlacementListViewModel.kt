package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.placements.manager.PlacementManager
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementSkipConfirmation
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlacementListViewModel : ViewModel(), KoinComponent {

    private val firstRunSettingsManager: FirstRunSettings by inject()
    private val placementManager: PlacementManager by inject()

    private val _screenData = MutableStateFlow(placementManager.createUiData()).cMutableStateFlow()
    val screenData: CStateFlow<UIPlacementListScreen> = _screenData.cStateFlow()

    private val _events = MutableSharedFlow<PlacementListEvent>()
    val events: SharedFlow<PlacementListEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            placementManager.placements
                .map { placementManager.createUiData(it) }
                .collect(_screenData)
        }
    }

    fun handleInput(input: PlacementListInput) = when(input) {
        is PlacementListInput.PlacementSelected -> {
            viewModelScope.launch {
                _events.emit(PlacementListEvent.NavigateToPlacementDetails(input.placementId))
            }
        }
        PlacementListInput.GoToRanksScreen -> {
            firstRunSettingsManager.setInitState(InitState.RANKS)
            viewModelScope.launch {
                _events.emit(PlacementListEvent.NavigateToRanks)
            }
        }
        PlacementListInput.SkipPlacement -> {
            _screenData.value = _screenData.value.copy(skipConfirmation = UIPlacementSkipConfirmation())
        }
        PlacementListInput.SkipPlacementConfirm -> {
            firstRunSettingsManager.setInitState(InitState.DONE)
            viewModelScope.launch {
                _events.emit(PlacementListEvent.NavigateToMainScreen)
            }
        }
        PlacementListInput.SkipPlacementCancel -> {
            _screenData.value = _screenData.value.copy(skipConfirmation = null)
        }
    }
}