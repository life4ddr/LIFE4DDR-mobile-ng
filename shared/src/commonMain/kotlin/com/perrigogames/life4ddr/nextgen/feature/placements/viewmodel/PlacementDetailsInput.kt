package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

sealed class PlacementDetailsInput {

    data object FinalizeClicked: PlacementDetailsInput()

    data object PictureTaken : PlacementDetailsInput()

    data object TooltipDismissed : PlacementDetailsInput()
}