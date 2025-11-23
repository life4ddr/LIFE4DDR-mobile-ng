package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalProgress
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsStackedGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.SongsClearGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.StackedRankGoalWrapper
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.TrialGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.TrialStackedGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.GoalProgressConverter
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.MAPointGoalProgressConverter
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.MAPointStackedGoalProgressConverter
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.SongsClearGoalProgressConverter
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.TrialGoalProgressConverter
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.converter.TrialStackGoalProgressConverter
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

class LadderGoalProgressManager : BaseModel(), KoinComponent {

    private val logger by injectLogger("LadderGoalProgressManager")

    private val converters: Map<KClass<out BaseRankGoal>, GoalProgressConverter<out BaseRankGoal>> = mapOf(
        MAPointsGoal::class to MAPointGoalProgressConverter(),
        MAPointsStackedGoal::class to MAPointStackedGoalProgressConverter(),
        SongsClearGoal::class to SongsClearGoalProgressConverter(),
        TrialGoal::class to TrialGoalProgressConverter(),
        TrialStackedGoal::class to TrialStackGoalProgressConverter(),
    )

    fun getGoalProgress(goal: BaseRankGoal, ladderRank: LadderRank?): Flow<LadderGoalProgress?> {
        return when (goal) {
            is MAPointsGoal -> (converters[MAPointsGoal::class] as MAPointGoalProgressConverter)
                .getGoalProgress(goal, ladderRank)
            is SongsClearGoal -> (converters[SongsClearGoal::class] as SongsClearGoalProgressConverter)
                .getGoalProgress(goal, ladderRank)
            is TrialGoal -> (converters[TrialGoal::class] as TrialGoalProgressConverter)
                .getGoalProgress(goal, ladderRank)
            is StackedRankGoalWrapper -> when (goal.mainGoal) {
                is TrialStackedGoal -> (converters[TrialStackedGoal::class] as TrialStackGoalProgressConverter)
                    .getGoalProgress(goal, ladderRank)
                is MAPointsStackedGoal -> (converters[MAPointsStackedGoal::class] as MAPointStackedGoalProgressConverter)
                    .getGoalProgress(goal, ladderRank)
                else -> flowOf(null)
            }
            else -> {
                logger.w { "Failed to resolve progress for ${goal::class.simpleName} with ID ${goal.id}" }
                flowOf(null)
            }
        }
    }

    fun getProgressMapFlow(goals: List<BaseRankGoal>, ladderRank: LadderRank?): Flow<Map<BaseRankGoal, LadderGoalProgress?>> {
        val flowMap = goals.associateWith { getGoalProgress(it, ladderRank) }
        return combine(
            flowMap.map { (goal, flow) ->
                flow.map { goal to it }
            }
        ) { pairs -> pairs.toMap() }
    }
}