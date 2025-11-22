package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

// TODO LadderImporter
// TODO Settings

interface LadderDialogs {

//    val settings: Settings

    fun onClearGoalStates(positive: () -> Unit)
    fun onClearSongResults(positive: () -> Unit)
    fun onRefreshSongDatabase(positive: () -> Unit)

//    fun showImportProcessingDialog(dataLines: List<String>, opMode: LadderImporter.OpMode)

    fun showLadderUpdateToast()
    fun showImportFinishedToast()
}