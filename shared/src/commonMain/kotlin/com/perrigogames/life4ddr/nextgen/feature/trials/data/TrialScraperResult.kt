package com.perrigogames.life4ddr.nextgen.feature.trials.data

import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

sealed class TrialScraperResult {

    abstract val total: Int
    abstract val hits: Int

    data class Progress(
        val position: Int,
        override val total: Int,
        override val hits: Int,
        val trial: Trial
    ) : TrialScraperResult()

    data class Success(
        override val total: Int,
        override val hits: Int,
        val trial: Trial,
        val rank: TrialRank,
        val exScore: Int
    ) : TrialScraperResult()

    data class Finished(
        override val total: Int,
        override val hits: Int,
    ) : TrialScraperResult()
}
