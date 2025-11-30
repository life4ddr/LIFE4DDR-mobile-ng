package com.perrigogames.life4ddr.nextgen.feature.ladder.enum

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.CaloriesStackedRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.DifficultySetGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.MAPointsStackedGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.SongsClearGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.StackedRankGoalWrapper
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.TrialGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.TrialStackedGoal
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class RankGoalUserType(
    val serialized: String,
    val titleString: StringDesc,
) {
    LEVEL_12("12", StringDesc.ResourceFormatted(MR.strings.level_header, 12)),
    LEVEL_13("13", StringDesc.ResourceFormatted(MR.strings.level_header, 13)),
    LEVEL_14("14", StringDesc.ResourceFormatted(MR.strings.level_header, 14)),
    LEVEL_15("15", StringDesc.ResourceFormatted(MR.strings.level_header, 15)),
    LEVEL_16("16", StringDesc.ResourceFormatted(MR.strings.level_header, 16)),
    LEVEL_17("17", StringDesc.ResourceFormatted(MR.strings.level_header, 17)),
    LEVEL_18("18", StringDesc.ResourceFormatted(MR.strings.level_header, 18)),
    LEVEL_19("19", StringDesc.ResourceFormatted(MR.strings.level_header, 19)),
    PFC("pfc", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_pfcs)),
    COMBO("combo", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_combo)),
    LIFE4("life4", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_life4)),
    MFC("mfc", StringDesc.ResourceFormatted(MR.strings.clear)),
    SINGLE_SCORE("single_score", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_mfcs)),
    CLEAR("clear", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_single_score)),
    SINGLE_CLEAR("single_clear", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_single_clear)),
    SET_CLEAR("set_clear", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_set_clear)),
    CALORIES("calories", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_calories)),
    TRIALS("trials", StringDesc.ResourceFormatted(MR.strings.rank_goal_category_trials)),
    ;

    companion object {
        fun parse(v: String) = entries.firstOrNull { it.serialized == v }
    }
}

fun BaseRankGoal.userType(rank: LadderRank): RankGoalUserType {
    return when (this) {
        is StackedRankGoalWrapper -> mainGoal.userType(rank)
        is CaloriesStackedRankGoal -> RankGoalUserType.CALORIES
        is TrialGoal,
        is TrialStackedGoal -> RankGoalUserType.TRIALS
        is DifficultySetGoal -> RankGoalUserType.SET_CLEAR
        is MAPointsGoal,
        is MAPointsStackedGoal -> RankGoalUserType.MFC
        is SongsClearGoal -> {
            if (this.userType != null) {
                return this.userType
            }
            if (rank.group <= LadderRankClass.SILVER) {
                return when {
                    score != null -> RankGoalUserType.SINGLE_SCORE
                    songCount != null && songCount == 1 -> RankGoalUserType.SINGLE_CLEAR
                    else -> RankGoalUserType.CLEAR
                }
            }
            if (diffNum != null &&
                diffNum >= 12 &&
                rank.group >= LadderRankClass.GOLD
            ) {
                diffNum.toLevelUserType()?.let { return it }
            }
            return when (clearType) {
                ClearType.PERFECT_FULL_COMBO -> RankGoalUserType.PFC
                ClearType.GREAT_FULL_COMBO,
                ClearType.GOOD_FULL_COMBO -> RankGoalUserType.COMBO
                ClearType.LIFE4_CLEAR -> RankGoalUserType.LIFE4
                else -> error("No user type for goal $id")
            }
        }
        else -> error("No user type for goal $id")
    }
}

private fun Int.toLevelUserType() = when (this) {
    12 -> RankGoalUserType.LEVEL_12
    13 -> RankGoalUserType.LEVEL_13
    14 -> RankGoalUserType.LEVEL_14
    15 -> RankGoalUserType.LEVEL_15
    16 -> RankGoalUserType.LEVEL_16
    17 -> RankGoalUserType.LEVEL_17
    18 -> RankGoalUserType.LEVEL_18
    19 -> RankGoalUserType.LEVEL_19
    else -> null
}

object RankGoalUserTypeSerializer: KSerializer<RankGoalUserType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("rankGoalUserType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = RankGoalUserType.parse(decoder.decodeString())!!
    override fun serialize(encoder: Encoder, value: RankGoalUserType) {
        encoder.encodeString(value.serialized)
    }
}
