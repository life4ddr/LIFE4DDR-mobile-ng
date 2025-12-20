package com.perrigogames.life4ddr.nextgen.data

import com.perrigogames.life4ddr.nextgen.enums.GameVersion

object GameConstants {
    const val HIGHEST_DIFFICULTY = 19
    const val TRIAL_LENGTH = 4
    const val RIVAL_CODE_LENGTH = 8
    const val MAX_SCORE = 1_000_000
    const val SCORE_PENALTY_PERFECT = 10
    const val AAA_SCORE = 990_000

    val SUPPORTED_VERSIONS = listOf(GameVersion.WORLD, GameVersion.A20_PLUS)
}
