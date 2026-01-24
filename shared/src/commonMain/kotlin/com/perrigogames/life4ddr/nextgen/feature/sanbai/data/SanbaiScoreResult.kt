package com.perrigogames.life4ddr.nextgen.feature.sanbai.data

import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.enums.ResultSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SanbaiScoreResult(
    @SerialName("song_id") val songId: String,
    @SerialName("song_name") val songName: String,
    val difficulty: Int,
    val rating: Int,
    val score: Int,
    val lamp: Int,
    val flare: Int? = null,
    @SerialName("flare_skill") val flareSkill: Int? = null,
    @SerialName("time_uploaded") val timeUploaded: Long,
    @SerialName("time_played") val timePlayed: Long?
)

fun SanbaiScoreResult.toChartResult(): Pair<ChartResult, SanbaiImportFlags> {
    val (playStyle, difficultyClass) = when (difficulty) {
        0 -> PlayStyle.SINGLE to DifficultyClass.BEGINNER
        1 -> PlayStyle.SINGLE to DifficultyClass.BASIC
        2 -> PlayStyle.SINGLE to DifficultyClass.DIFFICULT
        3 -> PlayStyle.SINGLE to DifficultyClass.EXPERT
        4 -> PlayStyle.SINGLE to DifficultyClass.CHALLENGE
        5 -> PlayStyle.DOUBLE to DifficultyClass.BASIC
        6 -> PlayStyle.DOUBLE to DifficultyClass.DIFFICULT
        7 -> PlayStyle.DOUBLE to DifficultyClass.EXPERT
        8 -> PlayStyle.DOUBLE to DifficultyClass.CHALLENGE
        else -> throw IllegalArgumentException("Invalid difficulty value: $difficulty")
    }

    val flare = flare?.toLong()
    var wasLife4GivenWithFlare = false
    val clearType = when(lamp) {
        0 -> ClearType.FAIL
        1 -> {
            if ((flare ?: 0) >= 8) {
                wasLife4GivenWithFlare = true
                ClearType.LIFE4_CLEAR
            } else {
                ClearType.CLEAR
            }
        }
        2 -> ClearType.LIFE4_CLEAR
        3 -> ClearType.GOOD_FULL_COMBO
        4 -> ClearType.GREAT_FULL_COMBO
        5 -> when {
            score >= 999_910 -> ClearType.SINGLE_DIGIT_PERFECTS
            else -> ClearType.PERFECT_FULL_COMBO
        }
        6 -> ClearType.MARVELOUS_FULL_COMBO
        else -> ClearType.NO_PLAY
    }

    return ChartResult(
        skillId = songId,
        difficultyClass = difficultyClass,
        playStyle = playStyle,
        clearType = clearType,
        score = score.toLong(),
        exScore = null,
        flare = flare,
        flareSkill = flareSkill?.toLong(),
        source = ResultSource.SANBAI,
    ) to SanbaiImportFlags(
        wasLife4GivenWithFlare = wasLife4GivenWithFlare
    )
}

data class SanbaiImportFlags(
    var wasLife4GivenWithFlare : Boolean = false
) {

    fun applyFlags(other: SanbaiImportFlags) {
        wasLife4GivenWithFlare = wasLife4GivenWithFlare || other.wasLife4GivenWithFlare
    }
}
