package com.perrigogames.life4ddr.nextgen.feature.trials.manager

import com.perrigogames.life4ddr.nextgen.db.SelectFullSessions
import com.perrigogames.life4ddr.nextgen.db.TrialSong
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.db.TrialDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.SongResult
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.toList
import kotlin.time.ExperimentalTime

interface TrialRecordsManager {
    val bestSessions: StateFlow<List<SelectFullSessions>>

    fun saveSession(record: InProgressTrialSession, targetRank: TrialRank)

    fun saveSessions(records: List<Pair<InProgressTrialSession, TrialRank>>)

    fun saveFakeSession(
        trial: Trial,
        targetRank: TrialRank,
        exScore: Int,
    )

    fun deleteSession(sessionId: Long)

    fun deleteSessions(trialId: String)

    fun clearSessions()

    fun getSongsForSession(sessionId: Long): List<TrialSong>
}

@OptIn(ExperimentalTime::class)
class DefaultTrialRecordsManager(
    private val dbHelper: TrialDatabaseHelper,
    private val trialSettings: TrialSettings,
): BaseModel(), TrialRecordsManager {

    private val _refresh = MutableSharedFlow<Unit>()
    private val _bestSessions : MutableStateFlow<List<SelectFullSessions>> = MutableStateFlow(emptyList())
    override val bestSessions: StateFlow<List<SelectFullSessions>> get() = _bestSessions.asStateFlow()

    init {
        mainScope.launch {
            _refresh.map {
                dbHelper.fullSessions()
                    .groupBy { it.trialId }
                    .mapNotNull { (_, trials) -> trials.maxByOrNull { it.exScore ?: 0L } }
            }
                .collect(_bestSessions)
        }
        refreshSessions()
    }

    private fun refreshSessions() {
        mainScope.launch {
            _refresh.emit(Unit)
        }
    }

    override fun saveSession(
        record: InProgressTrialSession,
        targetRank: TrialRank
    ) {
        mainScope.launch {
            dbHelper.insertSession(record, targetRank)
            refreshSessions()
        }
    }

    override fun saveSessions(records: List<Pair<InProgressTrialSession, TrialRank>>) {
        mainScope.launch {
            records.forEach { (session, targetRank) ->
                dbHelper.insertSession(session, targetRank)
            }
            refreshSessions()
        }
    }

    override fun saveFakeSession(
        trial: Trial,
        targetRank: TrialRank,
        exScore: Int,
    ) {
        val songCount = trial.songs.size
        val exRatio = exScore / trial.totalEx.toDouble()
        var remainingEx = exScore

        saveSession(
            record = InProgressTrialSession(
                trial = trial,
                results = trial.songs.mapIndexed { idx, song ->
                    val ex = if (idx == songCount - 1) {
                        remainingEx
                    } else {
                        (song.ex * exRatio).toInt()
                    }
                    remainingEx -= ex
                    SongResult(
                        song = song,
                        exScore = ex
                    )
                }.toTypedArray()
            ).also { it.goalObtained = true },
            targetRank = targetRank
        )
    }

    override fun deleteSession(sessionId: Long) {
        mainScope.launch {
            dbHelper.deleteSession(sessionId)
            refreshSessions()
        }
    }

    override fun deleteSessions(trialId: String) {
        mainScope.launch {
            dbHelper.deleteSessions(trialId)
            refreshSessions()
        }
    }

    override fun clearSessions() {
        trialSettings.clearLastSyncTime()
        mainScope.launch {
            dbHelper.deleteAll()
            refreshSessions()
        }
    }

    override fun getSongsForSession(sessionId: Long) = dbHelper.songsForSession(sessionId)
}