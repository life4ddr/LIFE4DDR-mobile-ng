package com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter

import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalProgress
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsStackedGoal
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.toMAPointsDouble
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MAPointGoalProgressConverter : GoalProgressConverter<MAPointsGoal>, KoinComponent {

    private val chartResultOrganizer: ChartResultOrganizer by inject()

    override fun getGoalProgress(
        goal: MAPointsGoal,
        ladderRank: LadderRank?,
    ): Flow<LadderGoalProgress?> {
        val config = FilterState(
            selectedPlayStyle = goal.playStyle,
            clearTypeRange = ClearType.SINGLE_DIGIT_PERFECTS.ordinal..ClearType.MARVELOUS_FULL_COMBO.ordinal,
        )
        return chartResultOrganizer.resultsForConfig(goal, config, enableDifficultyTiers = false).map { (match, _) ->
            val maPointsThousandths = match.sumOf { it.maPointsThousandths() }
            LadderGoalProgress(
                progress = maPointsThousandths.toMAPointsDouble(),
                max = goal.points,
                showMax = true,
                results = match
            )
        }
    }
}

class MAPointStackedGoalProgressConverter : StackedGoalProgressConverter<MAPointsStackedGoal>, KoinComponent {

    private val chartResultOrganizer: ChartResultOrganizer by inject()

    override fun getGoalProgress(
        goal: MAPointsStackedGoal,
        stackIndex: Int,
        ladderRank: LadderRank?,
    ): Flow<LadderGoalProgress?> {
        val config = FilterState(
            selectedPlayStyle = goal.playStyle,
            clearTypeRange = ClearType.SINGLE_DIGIT_PERFECTS.ordinal .. ClearType.MARVELOUS_FULL_COMBO.ordinal,
        )
        val targetPoints = goal.getDoubleValue(stackIndex, MAPointsStackedGoal.KEY_MA_POINTS)!!
        return chartResultOrganizer.resultsForConfig(goal, config, enableDifficultyTiers = false).map { (match, _) ->
            val maPointsThousandths = match.sumOf { it.maPointsThousandths() }
            LadderGoalProgress(
                progress = maPointsThousandths.toMAPointsDouble(),
                max = targetPoints,
                showMax = true,
                results = match
            )
        }
    }
}
