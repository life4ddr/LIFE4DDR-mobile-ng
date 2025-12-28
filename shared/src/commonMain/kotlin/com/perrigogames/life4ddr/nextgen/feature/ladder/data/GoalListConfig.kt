package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import com.perrigogames.life4ddr.nextgen.enums.LadderRank

data class GoalListConfig(
    val targetRank: LadderRank? = null,
    val allowCompletingGoalsManually: Boolean = true,
    val allowHidingIndividualGoals: Boolean = true,
    val allowHidingCompletedGoals: Boolean = false,
)