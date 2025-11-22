package com.perrigogames.life4ddr.nextgen.feature.ladder.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import com.perrigogames.life4ddr.nextgen.db.DatabaseHelper
import com.perrigogames.life4ddr.nextgen.db.GoalState
import com.perrigogames.life4ddr.nextgen.enums.GoalStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class GoalDatabaseHelper(sqlDriver: SqlDriver): DatabaseHelper(sqlDriver) {

    fun allStates(): List<GoalState> = dbRef.goalStatusQueries.getAll().executeAsList()

    fun stateForId(id: Long): GoalState? =
        dbRef.goalStatusQueries.getStatus(id).executeAsList().firstOrNull()

    fun statesForIdList(ids: List<Long>): Query<GoalState> = dbRef.goalStatusQueries.getStatusList(ids)

    fun insertGoalState(goalId: Long, status: GoalStatus) {
        dbRef.goalStatusQueries.setStatus(goalId, status, Clock.System.now().toString())
    }

    suspend fun deleteAll() = withContext(Dispatchers.Default) {
        dbRef.goalStatusQueries.deleteAll()
    }
}