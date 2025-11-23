package com.perrigogames.life4ddr.nextgen.feature.trials.view

import com.perrigogames.life4ddr.nextgen.db.TrialSession
import com.perrigogames.life4ddr.nextgen.db.TrialSong
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialRecord
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialRecordSong
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UITrialMaps : KoinComponent {

    private val songManager: SongDataManager by inject()
    private val trialDataManager: TrialDataManager by inject()

    /**
     * @throws Error if the associated Trial or Songs cannot be found
     */
    fun trialRecordUIModel(
        session: TrialSession,
        songs: List<TrialSong>,
    ): UITrialRecord {
        val trial = trialDataManager.findTrial(session.trialId)
            ?: throw Error("Trial ${session.trialId} not found")
        return UITrialRecord(
            trialTitleText = trial.name,
            trialSubtitleText = when {
                trial.isRetired -> "(Retired)"
                trial.isEvent -> "(Event)"
                else -> null
            },
            exScoreText = "FIXME",
            exProgressPercent = 0f, // FIXME
            trialSongs = songs.map { trialSong ->
                val s = trial.songs[trialSong.position.toInt()]
//                val song = songManager.findSong(trialSong.id) ?: throw Error("Song ${} not found")
                UITrialRecordSong(
                    songTitleText = "I'm in terrible pain",
                    scoreText = "FIXME",
                    difficultyClass = s.difficultyClass,
                )
            },
            achieved = session.goalObtained,
            rank = session.goalRank,
        )
    }
}