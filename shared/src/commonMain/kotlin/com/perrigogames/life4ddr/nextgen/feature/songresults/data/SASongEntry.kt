package com.perrigogames.life4ddr.nextgen.feature.songresults.data

import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import kotlinx.serialization.Serializable

@Serializable
class SASongEntry(
    var skillId: String,
    val playStyle: PlayStyle,
    val difficultyClass: DifficultyClass,
    val score: Long,
    val clearType: ClearType,
)