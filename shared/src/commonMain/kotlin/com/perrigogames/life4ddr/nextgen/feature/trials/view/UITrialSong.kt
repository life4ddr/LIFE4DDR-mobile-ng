package com.perrigogames.life4ddr.nextgen.feature.trials.view

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialSong
import com.perrigogames.life4ddr.nextgen.view.UISongJacket
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class UITrialSong(
    val jacket: UISongJacket,
    val songNameText: StringDesc,
    val subtitleText: StringDesc,
    val playStyle: PlayStyle,
    val difficultyClass: DifficultyClass,
    val difficultyText: StringDesc,
    val difficultyNumber: Int,
) {
    val color: ColorResource = difficultyClass.colorRes
    val chartString = playStyle.aggregateString(difficultyClass)
}

fun TrialSong.toUITrialSong(jacket: UISongJacket) = UITrialSong(
    jacket = jacket,
    songNameText = chart.song.title.desc(),
    subtitleText = chart.song.version.uiString,
    playStyle = playStyle,
    difficultyClass = difficultyClass,
    difficultyText = chart.difficultyNumber.toString().desc(),
    difficultyNumber = chart.difficultyNumber,
)
