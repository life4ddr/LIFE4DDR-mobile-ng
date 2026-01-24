package com.perrigogames.life4ddr.nextgen.feature.trials.enums

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.StableId
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRank.*
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enum class representing a Rank that a player can earn in a LIFE4 Trial.
 */
@Serializable
enum class TrialRank(
    override val stableId: Long,
    val parent: LadderRank,
    val nameRes: StringResource,
    val drawableRes: ImageResource,
): StableId {
    COPPER(10, COPPER5, MR.strings.copper, MR.images.copper_3),
    BRONZE(15, BRONZE5, MR.strings.bronze, MR.images.bronze_3),
    SILVER(20, SILVER5, MR.strings.silver, MR.images.silver_3),
    GOLD(25, GOLD5, MR.strings.gold, MR.images.gold_3),
    PLATINUM(30, PLATINUM5, MR.strings.platinum, MR.images.platinum_3),
    DIAMOND(35, DIAMOND5, MR.strings.diamond, MR.images.diamond_3),
    COBALT(40, COBALT5, MR.strings.cobalt, MR.images.cobalt_3),
    PEARL(45, PEARL5, MR.strings.pearl, MR.images.pearl_3),
    TOPAZ(50, TOPAZ5, MR.strings.topaz, MR.images.topaz_3),
    AMETHYST(55, AMETHYST5, MR.strings.amethyst, MR.images.amethyst_3),
    EMERALD(60, EMERALD5, MR.strings.emerald, MR.images.emerald_3),
    ONYX(65, ONYX5, MR.strings.onyx, MR.images.onyx_3),
    RUBY(70, RUBY5, MR.strings.ruby, MR.images.ruby_3),
    ;

    val colorRes get() = parent.colorRes

    /**
     * Generates a list of this and all [TrialRank]s that are higher than this.
     */
    val andUp: Array<TrialRank>
        get() = entries.toTypedArray().let { it.copyOfRange(this.ordinal, it.size) }

    val next: TrialRank?
        get() = entries.getOrNull(this.ordinal + 1)

    companion object {
        fun parse(s: String?): TrialRank? = when (s) {
            null, "NONE" -> null
            else -> valueOf(s.uppercase())
        }

        fun parse(stableId: Long): TrialRank? = entries.firstOrNull { it.stableId == stableId }

        fun fromLadderRank(userRank: LadderRank?, parsePlatinum: Boolean) = when(userRank?.group) {
            null -> null
            LadderRankClass.COPPER -> COPPER
            LadderRankClass.BRONZE -> BRONZE
            LadderRankClass.SILVER -> SILVER
            LadderRankClass.GOLD -> GOLD
            LadderRankClass.PLATINUM -> if (parsePlatinum) PLATINUM else GOLD
            LadderRankClass.DIAMOND -> DIAMOND
            LadderRankClass.COBALT -> COBALT
            LadderRankClass.PEARL -> PEARL
            LadderRankClass.TOPAZ -> TOPAZ
            LadderRankClass.AMETHYST -> AMETHYST
            LadderRankClass.EMERALD -> EMERALD
            LadderRankClass.ONYX -> ONYX
            LadderRankClass.RUBY -> RUBY
        }
    }
}

object TrialRankSerializer: KSerializer<TrialRank> {
    override val descriptor = PrimitiveSerialDescriptor("trialRank", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = TrialRank.valueOf(decoder.decodeString().uppercase())
    override fun serialize(encoder: Encoder, value: TrialRank) = encoder.encodeString(value.name.lowercase())
}
