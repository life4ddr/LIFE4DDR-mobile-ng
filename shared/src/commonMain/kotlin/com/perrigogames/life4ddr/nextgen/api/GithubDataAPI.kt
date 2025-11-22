package com.perrigogames.life4ddr.nextgen.api

import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRankData
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MessageOfTheDay
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialData

/**
 * API interface for obtaining core application files from Github
 */
interface GithubDataAPI {

    suspend fun getLadderRanks(): LadderRankData
    suspend fun getSongList(): String
    suspend fun getTrials(): TrialData
    suspend fun getMotd(): MessageOfTheDay

    companion object {
        const val MOTD_FILE_NAME = "motd.json"
        const val PARTIAL_DIFFICULTY_FILE_NAME = "partial_difficulties.json"
        const val PLACEMENTS_FILE_NAME = "placements.json"
        const val RANKS_FILE_NAME = "ranks.json"
        const val SONGS_FILE_NAME = "songs.json"
        const val TRIALS_FILE_NAME = "trials.json"
    }
}
