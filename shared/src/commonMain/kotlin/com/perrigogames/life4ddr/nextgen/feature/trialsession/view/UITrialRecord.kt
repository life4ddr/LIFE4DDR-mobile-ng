package com.perrigogames.life4ddr.nextgen.feature.trialsession.view

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

data class UITrialRecord(
    val trialTitleText: String,
    val trialSubtitleText: String? = null,
    val exScoreText: String,
    val exProgressPercent: Float,
    val trialSongs: List<UITrialRecordSong>,
    val rank: TrialRank,
    val achieved: Boolean,
)

data class UITrialRecordSong(
    val songTitleText: String,
    val scoreText: String,
    val difficultyClass: DifficultyClass,
)