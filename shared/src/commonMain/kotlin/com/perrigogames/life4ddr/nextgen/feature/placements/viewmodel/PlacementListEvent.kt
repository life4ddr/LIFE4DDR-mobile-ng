package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

sealed class PlacementListEvent {
    data class NavigateToPlacementDetails(val placementId: String) : PlacementListEvent()

    data object NavigateToRanks : PlacementListEvent()

    data object NavigateToMainScreen : PlacementListEvent()
}