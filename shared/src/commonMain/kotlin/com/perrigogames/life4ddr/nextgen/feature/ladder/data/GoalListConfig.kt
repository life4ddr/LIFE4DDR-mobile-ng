package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import com.perrigogames.life4ddr.nextgen.enums.LadderRank

data class GoalListConfig(
    val targetRank: LadderRank? = null,
    val allowCompleting: Boolean = true,
    val allowHiding: Boolean = true,
)