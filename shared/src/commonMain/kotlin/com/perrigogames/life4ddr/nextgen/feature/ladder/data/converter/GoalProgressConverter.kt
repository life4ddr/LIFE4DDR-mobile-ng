package com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalProgress
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.StackedRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.StackedRankGoalWrapper
import kotlinx.coroutines.flow.Flow

interface GoalProgressConverter<T : BaseRankGoal> {

    fun getGoalProgress(
        goal: T,
        ladderRank: LadderRank?,
    ): Flow<LadderGoalProgress?>
}

interface StackedGoalProgressConverter<M : StackedRankGoal> : GoalProgressConverter<StackedRankGoalWrapper> {

    override fun getGoalProgress(
        goal: StackedRankGoalWrapper,
        ladderRank: LadderRank?,
    ): Flow<LadderGoalProgress?> {
        return getGoalProgress(
            goal = goal.mainGoal as M,
            stackIndex = goal.index,
            ladderRank = ladderRank,
        )
    }

    fun getGoalProgress(
        goal: M,
        stackIndex: Int,
        ladderRank: LadderRank?,
    ): Flow<LadderGoalProgress?>
}
