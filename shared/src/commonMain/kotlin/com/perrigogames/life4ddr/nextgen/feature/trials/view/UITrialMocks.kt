package com.perrigogames.life4ddr.nextgen.feature.trials.view

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.view.UISongJacket
import dev.icerock.moko.resources.desc.desc

fun createUITrialSong(
    jacketUrl: String = "https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/bag-jacket.webp",
    songNameText: String = "bag",
    artistText: String = "REVEN-G",
    playStyle: PlayStyle = PlayStyle.SINGLE,
    difficultyClass: DifficultyClass = DifficultyClass.EXPERT,
    difficultyText: String = "13",
    difficultyNumber: Int = 13,
) = UITrialSong(
    jacket = UISongJacket.WithUrl(jacketUrl),
    songNameText = songNameText.desc(),
    subtitleText = artistText.desc(),
    playStyle = playStyle,
    difficultyClass = difficultyClass,
    difficultyText = difficultyText.desc(),
    difficultyNumber = difficultyNumber,
)

val bags = listOf(
    createUITrialSong(
        songNameText = "bag",
        difficultyClass = DifficultyClass.DIFFICULT,
        difficultyText = "13",
        difficultyNumber = 13,
    ),
    createUITrialSong(
        songNameText = "bag",
        difficultyClass = DifficultyClass.EXPERT,
        difficultyText = "13",
        difficultyNumber = 13,
    ),
    createUITrialSong(
        songNameText = "bag",
        difficultyClass = DifficultyClass.CHALLENGE,
        difficultyText = "13",
        difficultyNumber = 13,
    )
)