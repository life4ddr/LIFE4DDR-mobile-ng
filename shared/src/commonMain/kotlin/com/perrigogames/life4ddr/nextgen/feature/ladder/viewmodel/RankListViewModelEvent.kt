package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

sealed class RankListViewModelEvent {
    data object NavigateToPlacements : RankListViewModelEvent()
    data object NavigateToMainScreen : RankListViewModelEvent()
}