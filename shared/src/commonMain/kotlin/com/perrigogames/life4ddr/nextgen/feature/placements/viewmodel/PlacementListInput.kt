package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

sealed class PlacementListInput {
    data class PlacementSelected(val placementId: String) : PlacementListInput()

    data object GoToRanksScreen : PlacementListInput()

    data object SkipPlacement : PlacementListInput()

    data object SkipPlacementConfirm : PlacementListInput()

    data object SkipPlacementCancel : PlacementListInput()
}