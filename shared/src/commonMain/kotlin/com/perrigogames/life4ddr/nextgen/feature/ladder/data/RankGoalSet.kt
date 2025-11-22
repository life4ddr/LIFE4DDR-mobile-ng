package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import kotlinx.serialization.Serializable

@Serializable
class RankGoalSet(
    val rank: String,
    val requirements: Int?,
    val goals: List<BaseRankGoal>,
)
