package com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel

// TODO MokoResources
sealed class PlacementDetailsEvent {

    data object ShowCamera: PlacementDetailsEvent()

    data class ShowTooltip(
        val title: String, // TODO MokoResources
        val message: String, // TODO MokoResources
        val ctaText: String, // TODO MokoResources
        val ctaAction: PlacementDetailsAction,
    ) : PlacementDetailsEvent()

    data class NavigateToMainScreen(
        val submissionUrl: String? = null, // TODO MokoResources
    ) : PlacementDetailsEvent()
}