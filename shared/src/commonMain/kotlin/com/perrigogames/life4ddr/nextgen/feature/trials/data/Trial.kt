@file:UseSerializers(
    TrialTypeSerializer::class,
    TrialRankSerializer::class,
    PlacementRankSerializer::class,
    PlayStyleSerializer::class,
    ChartTypeSerializer::class,
)

package com.perrigogames.life4ddr.nextgen.feature.trials.data

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.MajorVersioned
import com.perrigogames.life4ddr.nextgen.data.PlacementRank
import com.perrigogames.life4ddr.nextgen.data.PlacementRankSerializer
import com.perrigogames.life4ddr.nextgen.enums.ChartTypeSerializer
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.enums.PlayStyleSerializer
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRankSerializer
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialType
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialTypeSerializer
import dev.icerock.moko.resources.desc.image.asImageDesc
import dev.icerock.moko.resources.getImageByFileName
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.getValue
import kotlin.math.min
import kotlin.text.get
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class TrialData(
    override val version: Long,
    @SerialName("major_version") override val majorVersion: Int,
    val trials: List<Course>,
): MajorVersioned {

    companion object {
        const val TRIAL_DATA_REMOTE_VERSION = 3
    }
}

@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
sealed class Course {

    abstract val id: String
    abstract val name: String
    abstract val author: String?
    abstract val state: TrialState
//    abstract val type: TrialType
    abstract val songs: List<TrialSong>
    abstract val playStyle: PlayStyle
    abstract val difficulty: Int?
    abstract val availableRanks: List<TrialRank>
    @SerialName("cover_url") abstract val coverUrl: String?
    @SerialName("cover_override") abstract val coverOverride: Boolean

    @Serializable
    @SerialName("trial")
    data class Trial(
        override val id: String,
        override val name: String,
        override val author: String? = null,
        override val state: TrialState = TrialState.ACTIVE,
        override val songs: List<TrialSong>,
        @SerialName("play_style") override val playStyle: PlayStyle = PlayStyle.SINGLE,
        override val difficulty: Int,
        val goals: List<TrialGoalSet>,
        val isLegacy: Boolean = true, // Legacy trials require clearing the final stage on LIFE4
        @SerialName("cover_url") override val coverUrl: String? = null,
        @SerialName("cover_override") override val coverOverride: Boolean = false,
    ) : Course() {

        val size: Int = songs.size

        fun goalSet(rank: TrialRank?): TrialGoalSet? = goals.find { it.rank == rank }

        override val availableRanks: List<TrialRank> = goals.map { it.rank }

        val currentExScore get() = songs.sumOf { it.ex }
    }

    @Serializable
    @SerialName("placement")
    data class Placement(
        override val id: String,
        override val name: String,
        override val author: String? = null,
        override val state: TrialState = TrialState.ACTIVE,
        override val songs: List<TrialSong>,
        @SerialName("play_style") override val playStyle: PlayStyle = PlayStyle.SINGLE,
        @SerialName("cover_url") override val coverUrl: String? = null,
        @SerialName("cover_override") override val coverOverride: Boolean = false,
        @SerialName("placement_rank") val placementRank: PlacementRank? = null,
    ) : Course() {

        override val availableRanks: List<TrialRank> = emptyList()

        override val difficulty: Int? = null
    }

    @Serializable
    @SerialName("event")
    data class Event(
        override val id: String,
        override val name: String,
        override val author: String? = null,
        override val state: TrialState = TrialState.ACTIVE,
        override val songs: List<TrialSong>,
        @SerialName("play_style") override val playStyle: PlayStyle = PlayStyle.SINGLE,
        override val difficulty: Int,
        @SerialName("cover_url") override val coverUrl: String? = null,
        @SerialName("cover_override") override val coverOverride: Boolean = false,
        @SerialName("event_start") val eventStart: LocalDateTime,
        @SerialName("event_end") val eventEnd: LocalDateTime,
        @SerialName("scoring_groups") val scoringGroups: List<List<TrialRank>>,
    ) : Course() {

        override val availableRanks: List<TrialRank> = scoringGroups.flatten()

        val isActiveEvent: Boolean
            get() = (eventStart.rangeTo(eventEnd)).contains(
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )

        /**
         * Return the scoring group for a user with a particular rank.
         */
        fun findScoringGroup(rank: TrialRank) = scoringGroups.first { it.contains(rank) }
    }

    val totalEx get() = songs.sumOf { it.ex }

    val coverResource by lazy {
        MR.images.getImageByFileName(id)?.asImageDesc()
    }

    companion object {
        const val COURSE_NAME = "course"
    }
}

@Serializable
data class TrialSong(
    val skillId: String = "FIXME",
    @SerialName("play_style") val playStyle: PlayStyle = PlayStyle.SINGLE,
    @SerialName("difficulty_class") val difficultyClass: DifficultyClass,
    val ex: Int,
) {
    @Transient lateinit var chart: Chart
}
