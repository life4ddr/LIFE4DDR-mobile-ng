package com.perrigogames.life4ddr.nextgen.api

import com.perrigogames.life4ddr.nextgen.data.Versioned
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface SanbaiAPI {
    suspend fun getSongData(): SongListResponse

    fun getAuthorizeUrl(): String
    suspend fun getSessionToken(code: String): SanbaiAuthTokenResponse
    suspend fun getScores(): List<SanbaiScoreResult>?
    suspend fun getPlayerId(): String

    companion object {
        const val SANBAI_CLIENT_ID = "SECRET"
        const val SANBAI_CLIENT_SECRET = "SECRET"
    }
}

@OptIn(ExperimentalTime::class)
@Serializable
data class SongListResponse(
    val lastUpdated: Instant,
    val songs: List<SongListResponseItem>
) : Versioned {

    override val version: Long
        get() = lastUpdated.epochSeconds
}

@Serializable
data class SongListResponseItem(
    @SerialName("song_id") val songId: String,
    @SerialName("song_name") val songName: String,
    @SerialName("alternate_name") val alternateName: String? = null,
    @SerialName("searchable_name") val searchableName: String? = null,
    @SerialName("romanized_name") val romanizedName: String? = null,
    val alphabet: String,
    val deleted: Int? = null,
    @SerialName("version_num") val versionNum: Int,
    val ratings: List<Int>,
    val tiers: List<Double>,
    @SerialName("lock_types") val lockTypes: List<Int>? = null
)

@Serializable
data class SanbaiScoreResult(
    @SerialName("song_id") val songId: String,
    @SerialName("song_name") val songName: String,
    val difficulty: Int,
    val rating: Int,
    val score: Int,
    val lamp: Int,
    val flare: Int? = null,
    @SerialName("flare_skill") val flareSkill: Int? = null,
    @SerialName("time_uploaded") val timeUploaded: Long,
    @SerialName("time_played") val timePlayed: Long?
)

// FIXME SqlDelight
//fun SanbaiScoreResult.toChartResult(): ChartResult {
//    val (playStyle, difficultyClass) = when (difficulty) {
//        0 -> PlayStyle.SINGLE to DifficultyClass.BEGINNER
//        1 -> PlayStyle.SINGLE to DifficultyClass.BASIC
//        2 -> PlayStyle.SINGLE to DifficultyClass.DIFFICULT
//        3 -> PlayStyle.SINGLE to DifficultyClass.EXPERT
//        4 -> PlayStyle.SINGLE to DifficultyClass.CHALLENGE
//        5 -> PlayStyle.DOUBLE to DifficultyClass.BASIC
//        6 -> PlayStyle.DOUBLE to DifficultyClass.DIFFICULT
//        7 -> PlayStyle.DOUBLE to DifficultyClass.EXPERT
//        8 -> PlayStyle.DOUBLE to DifficultyClass.CHALLENGE
//        else -> throw IllegalArgumentException("Invalid difficulty value: $difficulty")
//    }
//    return ChartResult(
//        skillId = songId,
//        difficultyClass = difficultyClass,
//        playStyle = playStyle,
//        clearType = when(lamp) {
//            0 -> ClearType.FAIL
//            1 -> ClearType.CLEAR
//            2 -> ClearType.LIFE4_CLEAR
//            3 -> ClearType.GOOD_FULL_COMBO
//            4 -> ClearType.GREAT_FULL_COMBO
//            5 -> when {
//                score >= 999_910 -> ClearType.SINGLE_DIGIT_PERFECTS
//                else -> ClearType.PERFECT_FULL_COMBO
//            }
//            6 -> ClearType.MARVELOUS_FULL_COMBO
//            else -> ClearType.NO_PLAY
//        },
//        score = score.toLong(),
//        exScore = null,
//        flare = flare?.toLong(),
//        flareSkill = flareSkill?.toLong()
//    )
//}
