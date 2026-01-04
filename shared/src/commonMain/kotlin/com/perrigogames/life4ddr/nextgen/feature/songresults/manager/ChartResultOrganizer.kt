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
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer.Companion.BASIC_LOCKS
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer.Companion.EXPANDED_LOCKS
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.perrigogames.life4ddr.nextgen.util.split
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

typealias OrganizerBase = Map<PlayStyle, DifficultyClassMap>
typealias DifficultyClassMap = Map<DifficultyClass, DifficultyNumberMap>
typealias DifficultyNumberMap = Map<Int, List<ChartResultPair>>

@OptIn(ExperimentalTime::class)
interface ChartResultOrganizer {

    fun chartsForConfig(config: ChartFilterState) : Flow<List<ChartResultPair>>

    fun resultsForConfig(
        base: BaseRankGoal?,
        config: FilterState,
        enableDifficultyTiers: Boolean
    ): Flow<ResultsBundle>

    companion object {
        const val ASIA_EXCLUSIVE = 10
        const val GOLD_CAB = 20
        const val GRAND_PRIX = 190
        const val BEMANI_PRO_LEAGUE = 240
        const val FLARE_LOCKED = 250
        const val TIME_EVENT_LOCKED = 260
        const val GOLDEN_LEAGUE = 270
        const val EXTRA_SAVIOR = 280
        const val GALAXY_BRAVE = 290
        const val PLATINUM_PASS = 300

        val EXPANDED_LOCKS = listOf(
            ASIA_EXCLUSIVE,
            GOLD_CAB,
            GRAND_PRIX,
            TIME_EVENT_LOCKED,
            GALAXY_BRAVE,
            GOLDEN_LEAGUE,
            PLATINUM_PASS,
            BEMANI_PRO_LEAGUE
        )

        val BASIC_LOCKS = EXPANDED_LOCKS + listOf(
            FLARE_LOCKED,
            EXTRA_SAVIOR,
        )

        fun lockTypeName(lockType: Int?) = when(lockType) {
            ASIA_EXCLUSIVE -> "Asia Exclusive ($lockType)"
            BEMANI_PRO_LEAGUE -> "BEMANI Pro League ($lockType)"
            EXTRA_SAVIOR -> "Extra Savior ($lockType)"
            FLARE_LOCKED -> "Flare Locked ($lockType)"
            GALAXY_BRAVE -> "Galaxy Brave ($lockType)"
            GOLD_CAB -> "Gold Cab ($lockType)"
            GOLDEN_LEAGUE -> "Golden League ($lockType)"
            GRAND_PRIX -> "Grand Prix ($lockType)"
            PLATINUM_PASS -> "DDR Platinum Pass ($lockType)"
            TIME_EVENT_LOCKED -> "Time Event Locked ($lockType)"
            else -> "Unspecified reason ($lockType)"
        }
    }
}

@OptIn(ExperimentalTime::class)
class DefaultChartResultOrganizer(
    private val songResultsManager: SongResultsManager,
    private val logger: Logger,
): BaseModel(), KoinComponent, ChartResultOrganizer {

    private val basicOrganizer = MutableStateFlow<OrganizerBase>(emptyMap())

    private val chartListCache = mutableMapOf<ChartFilterState, Flow<List<ChartResultPair>>>()

    init {
        mainScope.launch {
            songResultsManager.library.map { base ->
                base.groupByPlayStyle().mapValues { (_, l1) ->
                    l1.groupByDifficultyClass().mapValues { (_, l2) ->
                        l2.groupByDifficultyNumber()
                    }
                }
            }
            .collect(basicOrganizer)
        }
    }

    override fun chartsForConfig(config: ChartFilterState) : Flow<List<ChartResultPair>> {
        return if (config in chartListCache) {
            chartListCache[config]!!
        } else {
            basicOrganizer
                .map { it[config.selectedPlayStyle] ?: emptyMap() }
                .map { diffClassMap: DifficultyClassMap ->
                    val result = config.difficultyClassSelection.flatMap { diffClass ->
                        val diffNumMap = diffClassMap[diffClass] ?: emptyMap()
                        config.difficultyNumberRange.flatMap { diffNum ->
                            diffNumMap[diffNum] ?: emptyList()
                        }
                    }
                    return@map when(config.ignoreFilterType) {
                        IgnoreFilterType.BASIC -> {
                            result.filterNot { it.chart.song.deleted || (it.chart.lockType ?: 0) in BASIC_LOCKS }
                        }
                        IgnoreFilterType.EXPANDED -> {
                            result.filterNot { it.chart.song.deleted || (it.chart.lockType ?: 0) in EXPANDED_LOCKS }
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
        base: BaseRankGoal?,
        config: FilterState,
        enableDifficultyTiers: Boolean
    ): Flow<ResultsBundle> {
        return chartsForConfig(config.chartFilter)
            .map { ChartFilterer(it) }
            .map { filterer ->
                val start = Clock.System.now()
                var results = filterer.filtered(config.resultFilter)
                val filterEnd = Clock.System.now()

                (base as? SongsClearGoal)?.let { songClearGoal ->
                    results = processExceptions(songClearGoal, results)
                }
                results.copy(
                    resultsDone = results.resultsDone.specialSorted(base, enableDifficultyTiers),
                    resultsPartlyDone = results.resultsPartlyDone.specialSorted(base, enableDifficultyTiers),
                    resultsNotDone = results.resultsNotDone.specialSorted(base, enableDifficultyTiers)
                ).also {
                    val sortEnd = Clock.System.now()
                    val filterMillis = (filterEnd - start).inWholeMilliseconds
                    val sortMillis = (sortEnd - filterEnd).inWholeMilliseconds
                    val totalMillis = filterMillis + sortMillis
                    logger.d { "Filter time: ${filterMillis}ms, Sort time: ${sortMillis}ms, Total time: ${totalMillis}ms" }
                }
            }
    }

    private fun processExceptions(
        goal: SongsClearGoal,
        results: ResultsBundle
    ): ResultsBundle {
        return when {
            goal.exceptionScore == null -> results
            goal.exceptions != null -> {
                var floorAchieved = mutableListOf<ChartResultPair>()
                val floorNotAchieved = mutableListOf<ChartResultPair>()
                results.resultsNotDone.forEach { pair ->
                    if ((pair.result?.score ?: 0) >= goal.exceptionScore) {
                        floorAchieved.add(pair)
                    } else {
                        floorNotAchieved.add(pair)
                    }
                }
                floorAchieved = floorAchieved.sortedByDescending { it.result?.score ?: 0 }.toMutableList()
                while(floorAchieved.size > goal.exceptions) {
                    floorNotAchieved.add(floorAchieved.removeAt(floorAchieved.lastIndex))
                }
                ResultsBundle(
                    resultsDone = results.resultsDone,
                    resultsPartlyDone = floorAchieved,
                    resultsNotDone = floorNotAchieved
                )
            }
            goal.songExceptions != null -> {
                val floorAchieved = mutableListOf<ChartResultPair>()
                val floorNotAchieved = mutableListOf<ChartResultPair>()
                results.resultsNotDone.forEach { pair ->
                    val isException = pair.chart.song.title in goal.songExceptions
                    when {
                        !isException -> { floorNotAchieved.add(pair) }
                        (pair.result?.score ?: 0) >= goal.exceptionScore -> { floorAchieved.add(pair) }
                        else -> { floorNotAchieved.add(pair) }
                    }
                }
                ResultsBundle(
                    resultsDone = results.resultsDone,
                    resultsPartlyDone = floorAchieved,
                    resultsNotDone = floorNotAchieved
                )
            }
            else -> results
        }
    }

    private fun List<ChartResultPair>.specialSorted(
        base: BaseRankGoal?,
        enableDifficultyTiers: Boolean
    ): List<ChartResultPair> = sortedWith { a, b ->
        if (base is MAPointsGoal || base is MAPointsStackedGoal) {
            // First, MA points, descending
            val maCompare = ((b.maPointsThousandths() - a.maPointsThousandths()) * 100)
            if (maCompare != 0) {
                return@sortedWith maCompare
            }
        } else {
            // First, difficulty number, ascending
            val diffCompare = if (enableDifficultyTiers) {
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

class ChartFilterer(
    private val chartResults: List<ChartResultPair>
) {

    fun all(): List<ChartResultPair> = chartResults

    fun filtered(config: ResultFilterState): ResultsBundle {
        // TODO search cache for more simple versions of the search
        val (done, notDone) = chartResults.split { chart ->
            config.clearTypeRange.contains(chart.result?.clearType?.ordinal ?: 0)
                    && config.scoreRange.contains(chart.result?.score ?: 0)
        }
        // TODO implement some caching
        return ResultsBundle(
            resultsDone = done,
            resultsNotDone = notDone
        )
    }
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

