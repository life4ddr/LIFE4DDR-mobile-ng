package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import kotlinx.io.files.Path

sealed class PlacementDetailsInput {
    data object Back : PlacementDetailsInput()

    data object FinalizeClicked: PlacementDetailsInput()

    data class PictureTaken(val path: Path) : PlacementDetailsInput()

    data object TooltipDismissed : PlacementDetailsInput()
}