package com.perrigogames.life4ddr.nextgen.util

import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart

fun Chart.toStringExt() = "${song.title} ${difficultyClass.aggregateString(playStyle)} ($difficultyNumber)}"

fun ChartResult.toStringExt() = "$score - $clearType"

val ChartResult?.safeScore
    get() = this?.score ?: 0

val ChartResult?.safeClear
    get() = this?.clearType ?: ClearType.NO_PLAY