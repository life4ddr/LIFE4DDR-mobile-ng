package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

sealed class ScoreListEvent {
    data class ShowSanbaiLogin(val url: String) : ScoreListEvent()
}