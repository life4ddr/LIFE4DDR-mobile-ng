package com.perrigogames.life4ddr.nextgen.feature.songresults.manager

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongLibrary
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.matches
import com.perrigogames.life4ddr.nextgen.feature.songresults.db.ResultDatabaseHelper
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface SongResultsManager {

    val library: Flow<List<ChartResultPair>>

    val hasResults: Flow<Boolean>

    fun refresh()

    fun addScores(scores: List<ChartResult>)

    fun clearAllResults()
}

class DefaultSongResultsManager(
    private val songDataManager: SongDataManager,
    private val resultDbHelper: ResultDatabaseHelper,
    private val logger: Logger,
): BaseModel(), SongResultsManager {

    private val results = MutableStateFlow<List<ChartResult>>(emptyList())
    private val _library = MutableStateFlow<List<ChartResultPair>>(emptyList())
    override val library: Flow<List<ChartResultPair>> = _library.asStateFlow()
    override val hasResults: Flow<Boolean> = results.map { it.isNotEmpty() }

    init {
        mainScope.launch {
            combine(
                songDataManager.libraryFlow,
                results
            ) { songData, results ->
                logger.d { "Updating with ${songData.charts.size} charts and ${results.size} results" }
                matchCharts(songData, results)
            }
                .collect(_library)
        }
        refresh()
    }

    override fun refresh() {
        logger.d("Refreshing song results")
        mainScope.launch {
            results.emit(resultDbHelper.selectAll())
        }
    }

    override fun addScores(scores: List<ChartResult>) {
        mainScope.launch {
            resultDbHelper.insertResults(scores)
            results.emit(resultDbHelper.selectAll())
        }
    }

    override fun clearAllResults() {
        logger.w("Clearing all saved song results")
        mainScope.launch {
            resultDbHelper.deleteAll()
            results.emit(emptyList())
        }
    }

    private fun matchCharts(
        library: SongLibrary,
        results: List<ChartResult>
    ): List<ChartResultPair> {
        val matches = mutableMapOf<Chart, ChartResult>()
        results.forEach { result ->
            library.charts
                .firstOrNull { chart -> chart.matches(result) }
                ?.let { chart -> matches[chart] = result }
        }
        return library.charts.map { chart ->
            ChartResultPair(
                chart = chart,
                result = matches[chart]
            )
        }
    }
}
