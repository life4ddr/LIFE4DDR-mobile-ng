package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.placements.manager.PlacementManager
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementDetails
import com.perrigogames.life4ddr.nextgen.feature.trials.view.toUITrialSong
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class PlacementDetailsViewModel(
    placementId: String,
    private val placementManager: PlacementManager,
    private val logger: Logger,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(UIPlacementDetails())
    val state: StateFlow<UIPlacementDetails> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlacementDetailsEvent>(replay = 0)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            placementManager.findPlacement(placementId)
                .collect { placement ->
                    if (placement == null) {
                        logger.e("Placement ID $placementId not found")
                    } else {
                        _state.value = _state.value.copy(
                            rankIcon = placement.placementRank!!.toLadderRank(),
                            descriptionPoints = listOf(
                                MR.strings.placement_detail_description_1.desc(),
                                MR.strings.placement_detail_description_2.desc(),
                                MR.strings.placement_detail_description_3.desc(),
                            ),
                            songs = placement.songs.map { song ->
                                song.toUITrialSong()
                            }
                        )
                    }
                }
        }
    }

    fun handleAction(action: PlacementDetailsInput) {
        viewModelScope.launch {
            when (action) {
                PlacementDetailsInput.Back -> _events.emit(PlacementDetailsEvent.Back)
                PlacementDetailsInput.FinalizeClicked -> _events.emit(PlacementDetailsEvent.ShowCamera)
                PlacementDetailsInput.PictureTaken -> _events.emit(
                    PlacementDetailsEvent.ShowTooltip(
                        title = MR.strings.placement_complete_tooltip_title.desc(),
                        message = MR.strings.placement_complete_tooltip_message.desc(),
                        ctaText = MR.strings.okay.desc(),
                        ctaAction = PlacementDetailsInput.TooltipDismissed
                    )
                )
                PlacementDetailsInput.TooltipDismissed -> {
                    _events.emit(PlacementDetailsEvent.NavigateToMainScreen(
                        submissionUrl = SUBMISSION_URL
                    ))
                }
            }
        }
    }

    companion object {
        const val SUBMISSION_URL = "https://life4ddr.com/submissions/"
    }
}
