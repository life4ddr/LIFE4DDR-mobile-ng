package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

import androidx.lifecycle.ViewModel
import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.ResultSource
import com.perrigogames.life4ddr.nextgen.enums.flareScoreValue
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultsManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIManualScoreInput
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManualScoreInputViewModel(
    private val chart: Chart,
    private val songResultsManager: SongResultsManager,
) : ViewModel() {

    private val _state = MutableStateFlow(
        UIManualScoreInput(
            songTitle = chart.song.title.desc(),
            songDifficultyClass = chart.difficultyClass,
            songDifficultyNumber = "(${chart.difficultyNumber})".desc(),
        )
    )
    val state: StateFlow<UIManualScoreInput> = _state.asStateFlow()

    fun submitResult(
        score: Long,
        clearType: ClearType,
        flare: Int,
    ) {
        songResultsManager.addScores(
            listOf(
                ChartResult(
                    skillId = chart.song.skillId,
                    difficultyClass = chart.difficultyClass,
                    playStyle = chart.playStyle,
                    clearType = clearType,
                    score = score,
                    exScore = null,
                    flare = flare.toLong(),
                    flareSkill = flareScoreValue(chart.difficultyNumber, flare),
                    source = ResultSource.MANUAL,
                )
            )
        )
    }
}
