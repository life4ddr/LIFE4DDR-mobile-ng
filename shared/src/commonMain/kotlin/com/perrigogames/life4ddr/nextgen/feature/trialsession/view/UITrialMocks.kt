package com.perrigogames.life4ddr.nextgen.feature.trialsession.view

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import com.perrigogames.life4ddr.nextgen.longNumberString
import dev.icerock.moko.resources.desc.desc
import kotlin.random.Random

object UITrialMocks {
    fun createUITrialSong(
        jacketUrl: String? = "",
        songNameText: String = "",
        subtitleText: String = "",
        playStyle: PlayStyle = PlayStyle.SINGLE,
        difficultyClass: DifficultyClass = DifficultyClass.BEGINNER,
        difficultyText: String = "",
        difficultyNumber: Int = 1,
    ) = UITrialSong(
        jacketUrl = jacketUrl?.desc(),
        songNameText = songNameText.desc(),
        subtitleText = subtitleText.desc(),
        playStyle = playStyle,
        difficultyClass = difficultyClass,
        difficultyText = difficultyText.desc(),
        difficultyNumber = difficultyNumber,
    )

    fun createUITrialRecord(
        trialTitleText: String = "Trial Title",
        trialSubtitleText: String = "(Retired)",
        exScoreText: String = "1234 / 2345",
        progressPercent: Float = Random.nextFloat(),
        trialSongs: List<UITrialRecordSong> = (0..3).map {
            UITrialRecordSong(
                songTitleText = "Song $it",
                scoreText = randomScoreString(),
                difficultyClass = randomDifficultyClass()
            )
        },
        rank: TrialRank = TrialRank.entries.toTypedArray().random(),
        achieved: Boolean = true,
    ) = UITrialRecord(
        trialTitleText = trialTitleText,
        trialSubtitleText = trialSubtitleText,
        exScoreText = exScoreText,
        exProgressPercent = progressPercent,
        trialSongs = trialSongs,
        rank = rank,
        achieved = achieved,
    )

    fun createUITrialRecordSong(
        songTitleText: String = "Song Title",
        scoreText: String = randomScoreString(),
        difficultyClass: DifficultyClass,
    ) = UITrialRecordSong(
        songTitleText = songTitleText,
        scoreText = scoreText,
        difficultyClass = difficultyClass,
    )

    private fun randomScoreString() = (999000 - Random.nextInt(0, 40000)).longNumberString()

    private fun randomDifficultyClass() = if (Random.nextBoolean()) DifficultyClass.CHALLENGE else DifficultyClass.EXPERT
}