package com.perrigogames.life4ddr.nextgen.feature.sanbai.data

import com.perrigogames.life4ddr.nextgen.data.Versioned
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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