package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

import com.perrigogames.life4ddr.nextgen.db.GoalState
import com.perrigogames.life4ddr.nextgen.enums.GoalStatus
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.db.GoalDatabaseHelper
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GoalStateManager: BaseModel() {

    private val goalDBHelper: GoalDatabaseHelper by inject()

    private val _updated = MutableSharedFlow<Unit>(replay = 1)
    val updated = _updated.asSharedFlow()

    init {
        mainScope.launch {
            _updated.emit(Unit)
        }
    }

    private fun getGoalState(id: Long): GoalState? = goalDBHelper.stateForId(id)

    fun getOrCreateGoalState(id: Long): GoalState = getGoalState(id)
        ?: GoalState(id, GoalStatus.INCOMPLETE, Clock.System.now().toString())

    fun getOrCreateGoalState(goal: BaseRankGoal): GoalState = getOrCreateGoalState(goal.id.toLong())

    fun getGoalStateList(goals: List<BaseRankGoal>): List<GoalState> =
        goalDBHelper.statesForIdList(goals.map { it.id.toLong() }).executeAsList()

    fun setGoalState(id: Long, status: GoalStatus) {
        mainScope.launch {
            goalDBHelper.insertGoalState(id, status)
            _updated.emit(Unit)
        }
    }
}