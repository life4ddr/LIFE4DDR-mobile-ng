package com.perrigogames.life4ddr.nextgen.feature.songresults.manager

import app.cash.turbine.test
import com.perrigogames.life4ddr.nextgen.db.ChartResult
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.SongsClearGoal
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Song
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair
import com.perrigogames.life4ddr.nextgen.test.BaseTest
import com.perrigogames.life4ddr.nextgen.util.safeScore
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.kodein.mock.Mock
import org.kodein.mock.Mocker
import org.kodein.mock.UsesFakes
import org.kodein.mock.generated.fake
import org.kodein.mock.generated.injectMocks
import kotlin.test.BeforeTest
import kotlin.test.Test

@UsesFakes(
    Song::class,
    Chart::class,
    ChartResult::class,
)
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultChartResultOrganizerTest : BaseTest() {

    val mocker = Mocker()

    @Mock lateinit var songResultsManager: SongResultsManager
    lateinit var subject: DefaultChartResultOrganizer

    val baseGoal = SongsClearGoal(
        id = 1,
        score = 900_000,
    )

    val pfcGoal = SongsClearGoal(
        id = 1,
        mClearType = ClearType.PERFECT_FULL_COMBO,
        averageScore = 999_800,
    )

    @BeforeTest
    fun setup() {
        mocker.reset()
        mocker.injectMocks(this)
    }

    @Test
    fun `validate unnumbered hard score floor`() = runTest {
        validate(
            goal = baseGoal,
            done = 3, partlyDone = 0, notDone = 2
        )
    }

    @Test
    fun `validate unnumbered hard score floor with unscored exception count`() = runTest {
        validate(
            goal = baseGoal.copy(exceptions = 2),
            done = 3, partlyDone = 2, notDone = 0
        )
    }

    @Test
    fun `validate unnumbered hard score floor with numbered score floor exception`() = runTest {
        validate(
            goal = baseGoal.copy(exceptions = 2, exceptionScore = 800_000),
            done = 3, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate numbered hard score floor`() = runTest {
        validate(
            goal = baseGoal.copy(score = 900_000),
            done = 3, partlyDone = 0, notDone = 2
        )
    }

    @Test
    fun `validate numbered hard score floor with unscored exception count`() = runTest {
        validate(
            goal = baseGoal.copy(score = 900_000, exceptions = 2),
            done = 3, partlyDone = 2, notDone = 0
        )
    }

    @Test
    fun `validate numbered hard score floor with numbered score floor exception`() = runTest {
        validate(
            goal = baseGoal.copy(songCount = 2, exceptions = 2, exceptionScore = 800_000),
            done = 3, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate non-matching difficulty numbers are ignored`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, score = 900_000),
            done = 1, partlyDone = 0, notDone = 2
        )
    }

    @Test
    fun `validate non-matching difficulty numbers are ignored with unscored exception count`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, score = 900_000, exceptions = 2),
            done = 1, partlyDone = 2, notDone = 0
        )
    }

    @Test
    fun `validate non-matching difficulty numbers are ignored with numbered score floor exception`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, score = 900_000, exceptions = 2, exceptionScore = 800_000),
            done = 1, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate lower difficulty numbers are ignored when allowHigher is enabled`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, allowsHigherDiffNum = true, score = 900_000),
            done = 4, partlyDone = 0, notDone = 8
        )
    }

    @Test
    fun `validate lower difficulty numbers are ignored when allowHigher is enabled with unscored exception count`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, allowsHigherDiffNum = true, score = 900_000, exceptions = 2),
            done = 4, partlyDone = 2, notDone = 6
        )
    }

    @Test
    fun `validate lower difficulty numbers are ignored when allowHigher is enabled with numbered score floor exception`() = runTest {
        validate(
            setup = { setUpDiffNumScores() },
            goal = baseGoal.copy(diffNum = 15, allowsHigherDiffNum = true, score = 900_000, exceptions = 2, exceptionScore = 800_000),
            done = 4, partlyDone = 2, notDone = 6
        )
    }

    @Test
    fun `validate average score with all PFCs`() = runTest {
        validate(
            setup = { setUpFullPFCScores() },
            goal = pfcGoal,
            done = 3, partlyDone = 0, notDone = 2
        )
    }

    @Test
    fun `validate average score with most PFCs and one GFC`() = runTest {
        validate(
            setup = { setUpMostPFCScores() },
            goal = pfcGoal,
            done = 3, partlyDone = 0, notDone = 2
        )
    }

    @Test
    fun `validate average score with exception and all PFCs`() = runTest {
        validate(
            setup = { setUpFullPFCScores() },
            goal = pfcGoal.copy(exceptions = 2),
            done = 3, partlyDone = 2, notDone = 0
        )
    }

    @Test
    fun `validate average score with exception and most PFCs and one GFC`() = runTest {
        validate(
            setup = { setUpMostPFCScores() },
            goal = pfcGoal.copy(exceptions = 2),
            done = 3, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate average score with scored exception and all PFCs`() = runTest {
        validate(
            setup = { setUpFullPFCScores() },
            goal = pfcGoal.copy(exceptions = 2, exceptionScore = 999_750),
            done = 3, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate average score with scored exception and most PFCs and one GFC`() = runTest {
        validate(
            setup = { setUpMostPFCScores() },
            goal = pfcGoal.copy(exceptions = 2, exceptionScore = 999_700),
            done = 3, partlyDone = 1, notDone = 1
        )
    }

    @Test
    fun `validate that exception count takes the best available songs for partial match`() = runTest {
        setUpBasicScores()
        val goal = SongsClearGoal(
            id = 1,
            score = 1_000_000,
            exceptions = 2
        )

        subject.resultsForGoal(goal).test {
            awaitItem().let {
                it.resultsDone.apply {
                    size shouldBe 1
                    get(0).result.safeScore shouldBe 1_000_000
                }

                it.resultsPartlyDone.apply {
                    size shouldBe 2
                    get(0).result.safeScore shouldBe 950_000
                    get(1).result.safeScore shouldBe 900_000
                }
            }
            awaitComplete()
        }
    }

    private fun TestScope.setUpScores(vararg scores: Long) =
        setUpResults(*scores.map { TestResult(score = it) }.toTypedArray())

    private fun TestScope.setUpResults(vararg results: TestResult) {
        mocker.every { songResultsManager.library } returns flowOf(
            results.map { result ->
                ChartResultPair(
                    chart = fake<Chart>().copy(
                        song = fake<Song>().copy(
                            title = result.title,
                        ),
                        playStyle = result.playStyle,
                        difficultyClass = result.diffClass,
                        difficultyNumber = result.difficulty,
                    ),
                    result = fake<ChartResult>().copy(
                        clearType = result.clearType,
                        score = result.score,
                    ),
                )
            }
        )
        subject = DefaultChartResultOrganizer(songResultsManager)
        advanceUntilIdle()
    }

    private fun TestScope.setUpBasicScores() = setUpScores(1_000_000, 950_000, 900_000, 800_000, 700_000)

    private fun TestScope.setUpDiffNumScores() = (13..18).flatMap { diff ->
        listOf(900_000L, 800_000L, 700_000L).map { score ->
            TestResult(difficulty = diff, score = score)
        }
    }.let { setUpResults(*it.toTypedArray()) }

    private fun TestScope.setUpFullPFCScores() = setUpResults( // average = 999_800
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_900), // 10P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_850), // 15P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_800), // 20P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_750), // 25P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_700), // 30P
    )

    private fun TestScope.setUpMostPFCScores() = setUpResults( // average = 999_460
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_900), // 10P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_850), // 15P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_800), // 20P
        TestResult(clearType = ClearType.PERFECT_FULL_COMBO, score = 999_750), // 25P
        TestResult(clearType = ClearType.GREAT_FULL_COMBO, score = 998_000),
    )

    private suspend fun TestScope.validate(
        setup: TestScope.() -> Unit = { setUpBasicScores() },
        goal: SongsClearGoal,
        done: Int,
        partlyDone: Int,
        notDone: Int,
    ) {
        setup()
        goal.validate().shouldBeNull()

        subject.resultsForGoal(goal).test {
            awaitItem().let {
                Triple(
                    it.resultsDone.size,
                    it.resultsPartlyDone.size,
                    it.resultsNotDone.size
                ) shouldBe Triple(done, partlyDone, notDone)
            }
            awaitComplete()
        }
    }
}

data class TestResult(
    val title: String = "Song",
    val playStyle: PlayStyle = PlayStyle.SINGLE,
    val diffClass: DifficultyClass = DifficultyClass.CHALLENGE,
    val difficulty: Int = 1,
    val clearType: ClearType = ClearType.CLEAR,
    val score: Long,
)
