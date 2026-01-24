package com.perrigogames.life4ddr.nextgen.feature.trials.data

import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank

sealed class TrialScraperResult {

    data class StartingProfile(
        val username: String
    ) : TrialScraperResult()

    data class ProfileError(
        val username: String
    ) : TrialScraperResult()

    data class NoTrialsFound(
        val username: String
    ) : TrialScraperResult()

    sealed class ProfileFound : TrialScraperResult() {
        abstract val total: Int
        abstract val hits: Int

        data class Progress(
            val position: Int,
            override val total: Int,
            override val hits: Int,
            val trial: Trial
        ) : ProfileFound()

        data class Success(
            override val total: Int,
            override val hits: Int,
            val trial: Trial,
            val rank: TrialRank,
            val exScore: Int
        ) : ProfileFound()

        data class Finished(
            override val total: Int,
            override val hits: Int,
        ) : ProfileFound()
    }
}
