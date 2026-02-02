package com.perrigogames.life4ddr.nextgen.feature.trialsession.data

import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.ClearType.*
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialEXProgress
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialGoalSet
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialSong
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.util.hasCascade
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.core.component.KoinComponent
import kotlin.collections.isNotEmpty

@Serializable
data class InProgressTrialSession(
    val trial: Course.Trial,
    val results: Array<SongResult?> = arrayOfNulls(trial.songs.size),
    val finalPhotoUriString: String? = null,
) : KoinComponent {

    private val logger by injectLogger(this::class.simpleName ?: "InProgressTrialSession")

    @Transient var goalObtained: Boolean = false

    fun hasResult(index: Int): Boolean = results[index] != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false

        other as InProgressTrialSession

        if (trial != other.trial) return false
        if (!results.contentEquals(other.results)) return false
        if (finalPhotoUriString != other.finalPhotoUriString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trial.hashCode()
        result = 31 * result + results.contentHashCode()
        return result
    }

    fun createOrUpdateSongResult(index: Int, photoUri: String) = copy(
        results = results.copyOf().also {
            it[index] = it[index]?.copy(photoUriString = photoUri)
                ?: SongResult(
                    song = trial.songs[index],
                    photoUriString = photoUri,
                )
        }
    )

    /**
     * Calculates the number of combined misses in the current session.
     */
    val currentMisses: Int get() = results.filterNotNull().sumOf { it.misses ?: 0 }

    /**
     * Calculates the number of combined bad judgments in the current session.
     */
    val currentBadJudgments: Int get() = results.filterNotNull().sumOf { it.badJudges ?: it.misses ?: 0 }

    /**
     * Calculates the number of combined misses in the current session. This
     * function will return null if ANY of the results lacks a misses value.
     */
    val currentValidatedMisses: Int?
        get() = results.filterNotNull().let { current ->
            if (current.any { it.misses == null }) {
                null
            } else currentMisses
        }

    /**
     * Calculates the number of combined bad judgments in the current session. This
     * function will return null if ANY of the results lacks a bad judgments value.
     */
    val currentValidatedBadJudgments: Int?
        get() = results.filterNotNull().let { current ->
            if (current.any { it.badJudges == null }) {
                null
            } else currentBadJudgments
        }

    val progress: TrialEXProgress
        get() = TrialEXProgress(
            currentExScore = currentTotalExScore,
            currentMaxExScore = currentMaxExScore,
            maxExScore = trial.totalEx,
        )

    /** Calculates the current total EX the player has obtained for this session */
    private val currentTotalExScore: Int
        get() = results.filterNotNull().sumOf { it.exScore ?: 0 }

    /** Calculates the highest EX that a player could obtain on the songs that have been currently completed */
    private val currentMaxExScore: Int
        get() = trial.songs.mapIndexed { idx, item ->
            results[idx]?.exScore ?: item.ex
        }.sumOf { it }

    /** Calculates the amount of EX that is missing, which only counts the songs that have been completed */
    private val missingExScore: Int?
        get() = results.let { results ->
            if (results.any { it != null && it.exScore == null }) {
                null
            } else {
                results
                    .mapIndexed { idx, result -> trial.songs[idx].ex to result }
                    .sumOf { (songEx, result) ->
                        result?.let { songEx - it.exScore!! } ?: 0
                    }
            }
        }

    fun isAllInfoPresent(rank: TrialRank): Boolean {
        val goal = trial.goalSet(rank) ?: return true
        results.forEach { result ->
            if (result?.hasAllInfoSpecified(goal) == false) {
                return false
            }
        }
        return true
    }

    /**
     * Checks to see if the specified [TrialRank] goals would be satisfied under the current conditions.
     * Returns true or false if it can reliably be concluded that the requirements are or are not met, or
     * null if there's not enough information to make the determination.
     */
    fun isRankSatisfied(rank: TrialRank): SatisfiedResult {
        val goal = trial.goalSet(rank) ?: return SatisfiedResult.UNSATISFIED
        val presentResults = results.filterNotNull()
        val scores = results.mapNotNull { it?.score }
        val clears = results.mapNotNull { it?.clearType?.stableId?.toInt() }

        fun exMissingSatisfied(): SatisfiedResult = evaluateGoalCheck(goal.exMissing, missingExScore)

        fun judgeMissingSatisfied(): SatisfiedResult = evaluateGoalCheck(goal.judge, currentValidatedBadJudgments)

        fun missTotalSatisfied(): SatisfiedResult = evaluateGoalCheck(goal.miss, currentValidatedMisses)

        fun missEachSatisfied(): SatisfiedResult = if (goal.missEach == null) {
            SatisfiedResult.SATISFIED
        } else {
            presentResults.map {
                val misses = it.misses ?: return@map SatisfiedResult.MISSING_INFO
                (misses <= goal.missEach).toSatisfiedResult()
            }.minimumResult()
        }

        fun scoresSatisfied(): SatisfiedResult = when {
            goal.score == null -> SatisfiedResult.SATISFIED
            presentResults.any { it.score == null } -> SatisfiedResult.MISSING_INFO
            else -> (goal.score.hasCascade(scores)).toSatisfiedResult()
        }

        fun scoresIndexedSatisfied(): SatisfiedResult = when {
            goal.scoreIndexed == null -> SatisfiedResult.SATISFIED
            presentResults.any { it.score == null } -> SatisfiedResult.UNSATISFIED
            else -> {
                trial.songs.mapIndexed { idx, _ ->
                    val result = results[idx] ?: return@mapIndexed SatisfiedResult.SATISFIED
                    (result.score == goal.scoreIndexed[idx]).toSatisfiedResult()
                }.minimumResult()
            }
        }

        fun clearsSatisfied(): SatisfiedResult =
            (goal.clear?.map { it.stableId.toInt() }?.hasCascade(clears))?.toSatisfiedResult()
                ?: SatisfiedResult.SATISFIED

        fun clearsIndexedSatisfied(): SatisfiedResult = when {
            goal.clearIndexed == null -> SatisfiedResult.SATISFIED
            else -> {
                trial.songs.mapIndexed { idx, _ ->
                    val result = results[idx] ?: return@mapIndexed SatisfiedResult.SATISFIED
                    (result.clearType.stableId == goal.clearIndexed[idx].stableId).toSatisfiedResult()
                }.minimumResult()
            }
        }

        return listOf(
            "EX Missing" to exMissingSatisfied(),
            "Bad Judgments" to judgeMissingSatisfied(),
            "Misses" to missTotalSatisfied(),
            "Miss Each" to missEachSatisfied(),
            "Scores" to scoresSatisfied(),
            "Score Idx" to scoresIndexedSatisfied(),
            "Clears" to clearsSatisfied(),
            "Clear Idx" to clearsIndexedSatisfied(),
        ).map { (name, result) ->
            when (result) {
                SatisfiedResult.UNSATISFIED -> logger.d { "$name not satisfied for ${rank.name}" }
                SatisfiedResult.MISSING_INFO -> logger.d { "$name unknown for ${rank.name}" }
                else -> {}
            }
            result
        }.minimumResult()
    }

    private fun evaluateGoalCheck(target: Int?, actual: Int?): SatisfiedResult = when {
        target == null -> SatisfiedResult.SATISFIED
        actual == null -> SatisfiedResult.MISSING_INFO
        else -> (actual <= target).toSatisfiedResult()
    }
}

enum class SatisfiedResult {
    SATISFIED, MISSING_INFO, UNSATISFIED
}

fun Boolean?.toSatisfiedResult(): SatisfiedResult = when (this) {
    null -> SatisfiedResult.MISSING_INFO
    true -> SatisfiedResult.SATISFIED
    false -> SatisfiedResult.UNSATISFIED
}

fun List<SatisfiedResult>.minimumResult(): SatisfiedResult = when {
    this.any { it == SatisfiedResult.UNSATISFIED } -> SatisfiedResult.UNSATISFIED
    this.any { it == SatisfiedResult.MISSING_INFO } -> SatisfiedResult.MISSING_INFO
    else -> SatisfiedResult.SATISFIED
}

@Serializable
data class SongResult(
    val song: TrialSong,
    val photoUriString: String? = null,
    val score: Int? = null,
    val exScore: Int? = null,
    val misses: Int? = null,
    val goods: Int? = null,
    val greats: Int? = null,
    val perfects: Int? = null,
    val passed: Boolean = true,
    val shortcut: ShortcutType? = null,
) {

    val badJudges get() = when {
        misses == null -> null
        goods == null -> null
        greats == null -> null
        else -> misses + goods + greats
    }

    fun hasAllInfoSpecified(goal: TrialGoalSet): Boolean {
        val hasMissRequirement = goal.miss != null || goal.missEach != null
        val hasScoreRequirement = goal.score?.isNotEmpty() == true || goal.scoreIndexed?.isNotEmpty() == true
        val hasBadJudgeRequirement = goal.judge != null
                || goal.clear?.isNotEmpty() == true
                || goal.clearIndexed?.isNotEmpty() == true
        return when {
            this.exScore == null -> false // EX score is always required
            hasMissRequirement && this.misses == null -> false
            hasBadJudgeRequirement && this.badJudges == null -> false
            hasScoreRequirement && this.score == null -> false
            else -> true
        }
    }

    val clearType: ClearType
        get() = when {
        !passed -> FAIL
        exScore == song.ex -> MARVELOUS_FULL_COMBO
        else -> {
            var highestClear = MARVELOUS_FULL_COMBO
            if (perfects == null || perfects > 0) {
                highestClear = PERFECT_FULL_COMBO
            }
            if (greats == null || greats > 0) {
                highestClear = GREAT_FULL_COMBO
            }
            if (goods == null || goods > 0) {
                highestClear = GOOD_FULL_COMBO
            }
            when {
                misses == null -> highestClear = CLEAR
                misses >= 4 -> highestClear = CLEAR
                misses > 0 -> highestClear = LIFE4_CLEAR
            }
            highestClear
        }
    }
}
