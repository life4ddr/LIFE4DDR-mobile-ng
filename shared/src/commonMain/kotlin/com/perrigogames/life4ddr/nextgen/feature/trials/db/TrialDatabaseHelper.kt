package com.perrigogames.life4ddr.nextgen.feature.trials.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.db.DatabaseHelper
import com.perrigogames.life4ddr.nextgen.db.TrialSession
import com.perrigogames.life4ddr.nextgen.db.TrialSong
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TrialDatabaseHelper(sqlDriver: SqlDriver, logger: Logger): DatabaseHelper(sqlDriver, logger) {

    private val queries get() = dbRef.trialQueries

    fun allRecords(): Query<TrialSession> = queries.selectAllSessions()

    fun allSongs(): Query<TrialSong> = queries.selectAllSongs()

    suspend fun insertSession(
        session: InProgressTrialSession,
        targetRank: TrialRank,
        datetime: Instant? = null
    ) = withContext(Dispatchers.Default) {
        try {
            logger.d { "Inserting trial session for ${session.trial.name}" }

            queries.insertSession(
                null,
                session.trial.id,
                (datetime ?: Clock.System.now()).toString(),
                targetRank,
                session.goalObtained
            )

            val sId = queries.lastInsertRowId().executeAsOne().MAX ?: -1L
            logger.d { "Inserting songs for trial session for ${session.trial.name}, SID=$sId" }
            dbRef.transaction {
                session.results.forEachIndexed { idx, result ->
                    queries.insertSong(
                        null, sId,
                        idx.toLong(),
                        result!!.score?.toLong() ?: 0L,
                        result.exScore?.toLong() ?: 0L,
                        result.misses?.toLong(),
                        result.goods?.toLong(),
                        result.greats?.toLong(),
                        result.perfects?.toLong(),
                        result.passed
                    )
                }
            }
        } catch (e: Exception) {
            logger.e { "Exception inserting trial session for ${session.trial.name}\n${e.stackTraceToString()}" }
        }
    }

    suspend fun deleteSession(id: Long) = withContext(Dispatchers.Default) {
        queries.deleteSession(id)
    }

    suspend fun deleteSessions(trialId: String) = withContext(Dispatchers.Default) {
        sessionsForTrial(trialId).forEach { trial ->
            queries.deleteSession(trial.id)
        }
    }

    suspend fun deleteAll() = withContext(Dispatchers.Default) {
        queries.deleteAllSongs()
        queries.deleteAllSessions()
    }

    fun sessionsForTrial(trialId: String) = queries.selectSessionByTrialId(trialId).executeAsList()

    fun fullSessions() = queries.selectFullSessions().executeAsList()

    fun songsForSession(sessionId: Long) = queries.selectSessionSongs(sessionId).executeAsList()

    fun createRecordExportStrings(): List<String> = allRecords().executeAsList().map {
        (listOf(it.trialId, it.date, it.goalRank.stableId.toString(), it.goalObtained.toString()) +
                songsForSession(it.id).joinToString("\t") {
                        song -> "${song.score}\t${song.exScore}\t${song.misses}\t${song.goods}\t${song.greats}\t${song.perfects}\t${song.passed}"
                }).joinToString("\t")
    }

    fun importRecordExportStrings(input: List<String>) {
        val songs = allSongs().executeAsList()
        val records = allRecords().executeAsList()
            .associateWith { session -> songs.filter { session.id == it.sessionId } }
        dbRef.transaction {
            input.forEach { line ->
                val segs = line.split('\t').toMutableList()
                val trialId = segs.removeAt(0)
                val date = segs.removeAt(0)
                val goalRank = TrialRank.parse(segs.removeAt(0).toLong())!!
                val goalObtained = segs.removeAt(0).toBoolean()

                val newSongs = mutableListOf<TrialInputSong>()
                while(segs.isNotEmpty()) {
                    newSongs.add(
                        TrialInputSong(
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toLong(),
                        segs.removeAt(0).toBoolean())
                    )
                }
                val matches = records.any { record ->
                    record.key.trialId == trialId && record.value.map { it.exScore } == newSongs.map { it.exScore }
                }
                if (!matches) {
                    queries.insertSession(null, trialId, date, goalRank, goalObtained)
                    val sId = queries.lastInsertRowId().executeAsOne()
                    var idx = 0L
                    newSongs.forEach { queries.insertSong(null, sId.MAX ?: -1, idx++, it.score, it.exScore, it.misses, it.goods, it.greats, it.perfects, it.passed) }
                }
            }
        }
    }
}

class TrialInputSong(
    val score: Long,
    val exScore: Long,
    val misses: Long,
    val goods: Long,
    val greats: Long,
    val perfects: Long,
    val passed: Boolean,
)
