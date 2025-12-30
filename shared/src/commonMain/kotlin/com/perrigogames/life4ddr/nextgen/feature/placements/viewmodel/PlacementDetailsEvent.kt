package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

import dev.icerock.moko.resources.desc.StringDesc

sealed class PlacementDetailsEvent {

    data object Back : PlacementDetailsEvent()

    data object ShowCamera: PlacementDetailsEvent()

    data class ShowTooltip(
        val title: StringDesc,
        val message: StringDesc,
        val ctaText: StringDesc,
        val ctaAction: PlacementDetailsInput,
    ) : PlacementDetailsEvent()

    data class NavigateToMainScreen(
        val submissionUrl: String,
    ) : PlacementDetailsEvent()
}