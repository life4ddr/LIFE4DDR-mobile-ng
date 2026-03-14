package com.perrigogames.life4ddr.nextgen.feature.songresults.manager

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsStackedGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.SongsClearGoal
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.*
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ResultPresentation.*
import com.perrigogames.life4ddr.nextgen.feature.unlocks.manager.UnlockTypeManager
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.perrigogames.life4ddr.nextgen.util.split
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

typealias DifficultyClassMap = Map<DifficultyClass, DifficultyNumberMap>
typealias DifficultyNumberMap = Map<Int, List<ChartResultPair>>

@OptIn(ExperimentalTime::class)
interface ChartResultOrganizer {

    fun chartsForConfig(config: ChartFilterState) : Flow<List<ChartResultPair>>

    fun resultsForConfig(
        results: List<ChartResultPair>,
        config: ResultFilterState,
        presentation: ResultPresentation,
    ): Pair<List<ChartResultPair>, List<ChartResultPair>>

    fun resultsForGoal(
        goal: SongsClearGoal,
        enableDifficultyTiers: Boolean = false,
    ) = resultsForGoal(
        goal = goal,
        config = goal.filterState,
        enableDifficultyTiers = enableDifficultyTiers,
    )

    fun resultsForGoal(
        goal: BaseRankGoal?,
        config: FilterState,
        enableDifficultyTiers: Boolean = false,
    ): Flow<ResultsBundle>
}

@OptIn(ExperimentalTime::class)
class DefaultChartResultOrganizer(
    private val songResultsManager: SongResultsManager,
    private val unlockTypeManager: UnlockTypeManager,
    private val logger: Logger
): BaseModel(), ChartResultOrganizer {

    private val basicOrganizer = songResultsManager.library.map { base ->
        base.groupByPlayStyle().mapValues { (_, l1) ->
            l1.groupByDifficultyClass().mapValues { (_, l2) ->
                l2.groupByDifficultyNumber()
            }
        }
    }

    private val chartListCache = mutableMapOf<ChartFilterState, Flow<List<ChartResultPair>>>()

    override fun chartsForConfig(config: ChartFilterState) : Flow<List<ChartResultPair>> {
        return if (config in chartListCache) {
            chartListCache[config]!!
        } else {
            combine(
                basicOrganizer
                    .map { it[config.selectedPlayStyle] ?: emptyMap() },
                unlockTypeManager.basicLockKeys,
                unlockTypeManager.expandedLockKeys,
            ) { diffClassMap: DifficultyClassMap, basicLocks, expandedLocks ->
                    val result = config.difficultyClassSelection.flatMap { diffClass ->
                        val diffNumMap = diffClassMap[diffClass] ?: emptyMap()
                        config.difficultyNumberRange.flatMap { diffNum ->
                            diffNumMap[diffNum] ?: emptyList()
                        }
                    }
                    return@combine when(config.ignoreFilterType) {
                        IgnoreFilterType.BASIC -> {
                            result.filterNot { it.chart.song.deleted || (it.chart.lockType ?: 0) in basicLocks }
                        }
                        IgnoreFilterType.EXPANDED -> {
                            result.filterNot { it.chart.song.deleted || (it.chart.lockType ?: 0) in expandedLocks }
                        }
                        IgnoreFilterType.ALL_ACTIVE -> {
                            result.filterNot { it.chart.song.deleted }
                        }
                        IgnoreFilterType.ALL -> result
                    }
                }
                .also { chartListCache[config] = it }
        }
    }

    override fun resultsForConfig(
        results: List<ChartResultPair>,
        config: ResultFilterState,
        presentation: ResultPresentation,
    ): Pair<MutableList<ChartResultPair>, MutableList<ChartResultPair>> {
        return results.split { chart ->
            config.clearTypeRange.contains(chart.result?.clearType?.ordinal ?: 0)
                    && config.scoreRange.contains(chart.result?.score ?: 0)
        }.let { it.first.toMutableList() to it.second.toMutableList() }
    }

    override fun resultsForGoal(
        goal: BaseRankGoal?,
        config: FilterState,
        enableDifficultyTiers: Boolean,
    ): Flow<ResultsBundle> {
        return chartsForConfig(config.chartFilter)
            .map { results ->
                val presentation = when {
                    goal is MAPointsGoal || goal is MAPointsStackedGoal -> MA_POINTS
                    enableDifficultyTiers -> DIFFICULTY_TIERS
                    else -> NORMAL
                }

                // Calculate the primary goal completion
                val (done, notDone) = resultsForConfig(
                    results = results,
                    config = config.resultFilter,
                    presentation = presentation,
                ).let {
                    // Done items will not be modified, so sort them now
                    it.first.specialSorted(presentation) to it.second
                }

                // If exceptions are allowed, calculate the exception completion
                val songClearGoal = goal as? SongsClearGoal
                return@map if (songClearGoal?.hasExceptions == true) {
                    // If there's an exception floor, eliminate items that don't already fit that
                    val (exceptionDone, exceptionNotDone) = resultsForConfig(
                        results = notDone,
                        config = songClearGoal.exceptionFilterState,
                        presentation = presentation,
                    )

                    // Process the exceptions depending on which variety is needed
                    when {
                        songClearGoal.exceptions != null -> {
                            // Sort by score and take just the first X songs
                            exceptionDone.sortByDescending { it.result?.score ?: 0 }
                            while (exceptionDone.size > songClearGoal.exceptions) {
                                exceptionNotDone.add(0, exceptionDone.removeAt(exceptionDone.lastIndex))
                            }
                        }
                        songClearGoal.songExceptions != null -> {
                            // Remove all songs not specified in the list
                            val inProgress = exceptionDone.toMutableList()
                            exceptionDone.clear()
                            while (inProgress.isNotEmpty()) {
                                val item = inProgress.removeAt(inProgress.lastIndex)
                                val isException = item.chart.song.title in goal.songExceptions!!
                                if (isException) {
                                    exceptionNotDone.add(0, item)
                                } else {
                                    exceptionDone.add(0, item)
                                }
                            }
                        }
                        else -> error("Unaccounted exception found for goal ${goal.id}")
                    }

                    ResultsBundle(
                        resultsDone = done,
                        resultsPartlyDone = exceptionDone.specialSorted(presentation),
                        resultsNotDone = exceptionNotDone.specialSorted(presentation),
                    )
                } else ResultsBundle(
                    resultsDone = done,
                    resultsNotDone = notDone.specialSorted(presentation),
                )
            }
    }

    private fun List<ChartResultPair>.specialSorted(
        presentation: ResultPresentation
    ): List<ChartResultPair> = sortedWith { a, b ->
        if (presentation == MA_POINTS) {
            // First, MA points, descending
            val maCompare = ((b.maPointsThousandths() - a.maPointsThousandths()) * 100)
            if (maCompare != 0) {
                return@sortedWith maCompare
            }
        } else {
            // First, difficulty number, ascending
            val diffCompare = if (presentation == DIFFICULTY_TIERS) {
                ((a.chart.combinedDifficultyNumber - b.chart.combinedDifficultyNumber) * 1000).toInt()
            } else {
                a.chart.difficultyNumber - b.chart.difficultyNumber
            }
            if (diffCompare != 0) {
                return@sortedWith diffCompare
            }

            // Then, by score, descending
            val scoreCompare = ((b.result?.score ?: 0) - (a.result?.score ?: 0)).toInt()
            if (scoreCompare != 0) {
                return@sortedWith scoreCompare
            }
        }

        // Otherwise, by name, ascending
        return@sortedWith a.chart.song.title.compareTo(b.chart.song.title)
    }
}

enum class ResultPresentation {
    NORMAL, DIFFICULTY_TIERS, MA_POINTS
}

data class ResultsBundle(
    val resultsDone: List<ChartResultPair> = emptyList(),
    val resultsPartlyDone: List<ChartResultPair> = emptyList(),
    val resultsNotDone: List<ChartResultPair> = emptyList()
)

fun List<ChartResultPair>.groupByPlayStyle(): Map<PlayStyle, List<ChartResultPair>> =
    groupBy { it.chart.playStyle }

fun List<ChartResultPair>.groupByDifficultyClass(): Map<DifficultyClass, List<ChartResultPair>> =
    groupBy { it.chart.difficultyClass }

fun List<ChartResultPair>.groupByDifficultyNumber(): Map<Int, List<ChartResultPair>> =
    groupBy { it.chart.difficultyNumber }

fun List<ChartResultPair>.groupByClearType(): Map<ClearType, List<ChartResultPair>> =
    groupBy { it.result?.clearType ?: ClearType.NO_PLAY }

