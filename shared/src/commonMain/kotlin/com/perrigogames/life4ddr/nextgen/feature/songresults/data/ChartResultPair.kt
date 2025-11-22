package com.perrigogames.life4ddr.nextgen.feature.songresults.data

import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart

data class ChartResultPair(
    val chart: Chart,
    val result: ChartResult?,
) {

    fun maPointsThousandths(): Int {
        val points = when (chart.difficultyNumber) { // first = MFC, 2nd = SDP
            1 -> 100
            2, 3 -> 250
            4, 5, 6 -> 500
            7, 8, 9 -> 1000
            10 -> 1500
            11 -> 2000
            12 -> 4000
            13 -> 6000
            14 -> 8000
            15 -> 15000
            16, 17, 18, 19, 20 -> 25000
            else -> 0
        }
        return when (result?.clearType) {
            ClearType.MARVELOUS_FULL_COMBO -> points
            ClearType.SINGLE_DIGIT_PERFECTS -> points / 10
            else -> 0
        }
    }

    fun maPoints(): Double = maPointsThousandths().toMAPointsDouble()
}

fun Int.toMAPointsDouble(): Double = this / 1000.0

fun Int.toMAPointsCategoryString() = when (this) {
    100 -> "LV 1"
    250 -> "LV 2-3"
    500 -> "LV 4-6"
    1000 -> "LV 7-9"
    1500 -> "LV 10"
    2000 -> "LV 11"
    4000 -> "LV 12"
    6000 -> "LV 13"
    8000 -> "LV 14"
    15000 -> "LV 15"
    25000 -> "LV 16+"
    else -> "Unknown"
}

fun Chart.matches(other: ChartResult?) = when (other) {
    null -> false
    else -> song.skillId == other.skillId
            && difficultyClass == other.difficultyClass
            && playStyle == other.playStyle
}

fun ChartResult.matches(other: Chart?) = other?.matches(this) ?: false
