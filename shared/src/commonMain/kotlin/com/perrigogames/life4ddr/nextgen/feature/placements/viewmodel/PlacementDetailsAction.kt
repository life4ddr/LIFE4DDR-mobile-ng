package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

sealed class PlacementDetailsAction {

    data object FinalizeClicked: PlacementDetailsAction()

    data object PictureTaken : PlacementDetailsAction()

    data object TooltipDismissed : PlacementDetailsAction()
}