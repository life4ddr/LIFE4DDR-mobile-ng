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

    @BeforeTest
    fun setup() {
        mocker.reset()
        mocker.injectMocks(this)
    }

    @Test
    fun `validate unnumbered hard score floor`() = runTest {
        setUpScores(1_000_000, 950_000, 900_000, 800_000)

        baseGoal.validate().shouldBeNull()

        subject.resultsForGoal(baseGoal).test {
            awaitItem().validateCounts(3, 0, 1)
            awaitComplete()
        }
    }

    @Test
    fun `validate unnumbered hard score floor with unscored exception count`() = runTest {
        setUpScores(1_000_000, 950_000, 900_000, 800_000, 700_000)

        val goal = baseGoal.copy(
            exceptions = 2,
        )
        goal.validate().shouldBeNull()

        subject.resultsForGoal(goal).test {
            awaitItem().validateCounts(3, 2, 0)
            awaitComplete()
        }
    }

    @Test
    fun `validate unnumbered hard score floor with numbered score floor exception`() = runTest {
        setUpScores(1_000_000, 950_000, 900_000, 800_000, 700_000)

        val goal = baseGoal.copy(
            exceptions = 2,
            exceptionScore = 800_000
        )
        goal.validate().shouldBeNull()

        subject.resultsForGoal(goal).test {
            awaitItem().validateCounts(3, 1, 1)
            awaitComplete()
        }
    }

    @Test
    fun `validate numbered hard score floor`() = runTest {
        setUpScores(1_000_000, 950_000, 900_000, 800_000)

        val goal = baseGoal.copy(
            score = 900_000,
        )
        goal.validate().shouldBeNull()

        subject.resultsForGoal(goal).test {
            awaitItem().validateCounts(3, 0, 1)
            awaitComplete()
        }
    }

    @Test
    fun `validate numbered hard score floor with numbered score floor exception`() = runTest {
        setUpScores(1_000_000, 950_000, 900_000, 800_000, 700_000)

        val goal = baseGoal.copy(
            songCount = 2,
            exceptions = 2,
            exceptionScore = 800_000
        )
        goal.validate().shouldBeNull()

        subject.resultsForGoal(goal).test {
            awaitItem().validateCounts(3, 1, 1)
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

    private fun ResultsBundle.validateCounts(done: Int, partlyDone: Int, notDone: Int) {
        resultsDone.size shouldBe done
        resultsPartlyDone.size shouldBe partlyDone
        resultsNotDone.size shouldBe notDone
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
