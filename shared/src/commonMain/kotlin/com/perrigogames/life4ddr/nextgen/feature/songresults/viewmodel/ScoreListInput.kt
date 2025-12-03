package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

sealed class ScoreListInput {
    data class FilterInput(val input: FilterPanelInput) : ScoreListInput()
    data object RefreshSanbaiScores : ScoreListInput()
}