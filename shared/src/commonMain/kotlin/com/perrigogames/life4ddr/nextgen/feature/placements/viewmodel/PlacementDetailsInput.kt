package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

sealed class PlacementDetailsInput {
    data object Back : PlacementDetailsInput()

    data object FinalizeClicked: PlacementDetailsInput()

    data class PictureTaken(val uri: String) : PlacementDetailsInput()

    data object TooltipDismissed : PlacementDetailsInput()
}