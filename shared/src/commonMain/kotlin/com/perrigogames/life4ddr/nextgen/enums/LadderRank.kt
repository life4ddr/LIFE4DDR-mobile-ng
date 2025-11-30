package com.perrigogames.life4ddr.nextgen.enums

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass.*
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enum class representing a Rank that a player can earn in LIFE4.
 */
@Serializable(with = LadderRankSerializer::class)
enum class LadderRank(
    val stableId: Long,
    val group: LadderRankClass,
    val classPosition: Int,
    val nameRes: StringResource,
    val drawableRes: ImageResource,
) {
    COPPER1(20, COPPER, 1, MR.strings.copper_1, MR.images.copper_1),
    COPPER2(21, COPPER, 2, MR.strings.copper_2, MR.images.copper_2),
    COPPER3(22, COPPER, 3, MR.strings.copper_3, MR.images.copper_3),
    COPPER4(23, COPPER, 4, MR.strings.copper_4, MR.images.copper_4),
    COPPER5(24, COPPER, 5, MR.strings.copper_5, MR.images.copper_5),
    BRONZE1(40, BRONZE, 1, MR.strings.bronze_1, MR.images.bronze_1),
    BRONZE2(41, BRONZE, 2, MR.strings.bronze_2, MR.images.bronze_2),
    BRONZE3(42, BRONZE, 3, MR.strings.bronze_3, MR.images.bronze_3),
    BRONZE4(43, BRONZE, 4, MR.strings.bronze_4, MR.images.bronze_4),
    BRONZE5(44, BRONZE, 5, MR.strings.bronze_5, MR.images.bronze_5),
    SILVER1(60, SILVER, 1, MR.strings.silver_1, MR.images.silver_1),
    SILVER2(61, SILVER, 2, MR.strings.silver_2, MR.images.silver_2),
    SILVER3(62, SILVER, 3, MR.strings.silver_3, MR.images.silver_3),
    SILVER4(63, SILVER, 4, MR.strings.silver_4, MR.images.silver_4),
    SILVER5(64, SILVER, 5, MR.strings.silver_5, MR.images.silver_5),
    GOLD1(80, GOLD, 1, MR.strings.gold_1, MR.images.gold_1),
    GOLD2(81, GOLD, 2, MR.strings.gold_2, MR.images.gold_2),
    GOLD3(82, GOLD, 3, MR.strings.gold_3, MR.images.gold_3),
    GOLD4(83, GOLD, 4, MR.strings.gold_4, MR.images.gold_4),
    GOLD5(84, GOLD, 5, MR.strings.gold_5, MR.images.gold_5),
    PLATINUM1(100, PLATINUM, 1, MR.strings.platinum_1, MR.images.platinum_1),
    PLATINUM2(101, PLATINUM, 2, MR.strings.platinum_2, MR.images.platinum_2),
    PLATINUM3(102, PLATINUM, 3, MR.strings.platinum_3, MR.images.platinum_3),
    PLATINUM4(103, PLATINUM, 4, MR.strings.platinum_4, MR.images.platinum_4),
    PLATINUM5(104, PLATINUM, 5, MR.strings.platinum_5, MR.images.platinum_5),
    DIAMOND1(120, DIAMOND, 1, MR.strings.diamond_1, MR.images.diamond_1),
    DIAMOND2(121, DIAMOND, 2, MR.strings.diamond_2, MR.images.diamond_2),
    DIAMOND3(122, DIAMOND, 3, MR.strings.diamond_3, MR.images.diamond_3),
    DIAMOND4(123, DIAMOND, 4, MR.strings.diamond_4, MR.images.diamond_4),
    DIAMOND5(124, DIAMOND, 5, MR.strings.diamond_5, MR.images.diamond_5),
    COBALT1(140, COBALT, 1, MR.strings.cobalt_1, MR.images.cobalt_1),
    COBALT2(141, COBALT, 2, MR.strings.cobalt_2, MR.images.cobalt_2),
    COBALT3(142, COBALT, 3, MR.strings.cobalt_3, MR.images.cobalt_3),
    COBALT4(143, COBALT, 4, MR.strings.cobalt_4, MR.images.cobalt_4),
    COBALT5(144, COBALT, 5, MR.strings.cobalt_5, MR.images.cobalt_5),
    PEARL1(165, PEARL, 1, MR.strings.pearl_1, MR.images.pearl_1),
    PEARL2(166, PEARL, 2, MR.strings.pearl_2, MR.images.pearl_2),
    PEARL3(167, PEARL, 3, MR.strings.pearl_3, MR.images.pearl_3),
    PEARL4(168, PEARL, 4, MR.strings.pearl_4, MR.images.pearl_4),
    PEARL5(169, PEARL, 5, MR.strings.pearl_5, MR.images.pearl_5),
    TOPAZ1(185, TOPAZ, 1, MR.strings.topaz_1, MR.images.topaz_1),
    TOPAZ2(186, TOPAZ, 2, MR.strings.topaz_2, MR.images.topaz_2),
    TOPAZ3(187, TOPAZ, 3, MR.strings.topaz_3, MR.images.topaz_3),
    TOPAZ4(188, TOPAZ, 4, MR.strings.topaz_4, MR.images.topaz_4),
    TOPAZ5(189, TOPAZ, 5, MR.strings.topaz_5, MR.images.topaz_5),
    AMETHYST1(200, AMETHYST, 1, MR.strings.amethyst_1, MR.images.amethyst_1),
    AMETHYST2(201, AMETHYST, 2, MR.strings.amethyst_2, MR.images.amethyst_2),
    AMETHYST3(202, AMETHYST, 3, MR.strings.amethyst_3, MR.images.amethyst_3),
    AMETHYST4(203, AMETHYST, 4, MR.strings.amethyst_4, MR.images.amethyst_4),
    AMETHYST5(204, AMETHYST, 5, MR.strings.amethyst_5, MR.images.amethyst_5),
    EMERALD1(220, EMERALD, 1, MR.strings.emerald_1, MR.images.emerald_1),
    EMERALD2(221, EMERALD, 2, MR.strings.emerald_2, MR.images.emerald_2),
    EMERALD3(222, EMERALD, 3, MR.strings.emerald_3, MR.images.emerald_3),
    EMERALD4(223, EMERALD, 4, MR.strings.emerald_4, MR.images.emerald_4),
    EMERALD5(224, EMERALD, 5, MR.strings.emerald_5, MR.images.emerald_5),
    ONYX1(240, ONYX, 1, MR.strings.onyx_1, MR.images.onyx_1),
    ONYX2(241, ONYX, 2, MR.strings.onyx_2, MR.images.onyx_2),
    ONYX3(242, ONYX, 3, MR.strings.onyx_3, MR.images.onyx_3),
    ONYX4(243, ONYX, 4, MR.strings.onyx_4, MR.images.onyx_4),
    ONYX5(244, ONYX, 5, MR.strings.onyx_5, MR.images.onyx_5),
    RUBY1(260, RUBY, 1, MR.strings.ruby_1, MR.images.ruby_1),
    RUBY2(261, RUBY, 2, MR.strings.ruby_2, MR.images.ruby_2),
    RUBY3(262, RUBY, 3, MR.strings.ruby_3, MR.images.ruby_3),
    RUBY4(263, RUBY, 4, MR.strings.ruby_4, MR.images.ruby_4),
    RUBY5(264, RUBY, 5, MR.strings.ruby_5, MR.images.ruby_5),
    ;

    val categoryNameRes get() = when(this.classPosition) {
        1 -> MR.strings.roman_1
        2 -> MR.strings.roman_2
        3 -> MR.strings.roman_3
        4 -> MR.strings.roman_4
        5 -> MR.strings.roman_5
        else -> error("Illegal LadderRank position")
    }

    val groupNameRes get() = group.nameRes

    val colorRes get() = group.colorRes

    val next: LadderRank? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun parse(s: String?): LadderRank? = try {
            s?.let {
                valueOf(it.uppercase()
                    .replace(" IV", "4")
                    .replace(" V", "5")
                    .replace(" III", "3")
                    .replace(" II", "2")
                    .replace(" I", "1"))
            }
        } catch (e: IllegalArgumentException) { null }

        fun parse(stableId: Long?): LadderRank? = stableId?.let { id -> entries.firstOrNull { it.stableId == id } }
    }
}

val LadderRank?.nameRes get() = when(this) {
    null -> MR.strings.no_rank
    else -> this.nameRes
}

val LadderRank?.nullableNext: LadderRank?
    get() = if (this == null) {
        LadderRank.entries.first()
    } else { this.next }

/**
 * Enum describing the groups that Ranks are put into.
 */
enum class LadderRankClass(
    val nameRes: StringResource,
    val colorRes: ColorResource
) {
    COPPER(MR.strings.copper, MR.colors.copper),
    BRONZE(MR.strings.bronze, MR.colors.bronze),
    SILVER(MR.strings.silver, MR.colors.silver),
    GOLD(MR.strings.gold, MR.colors.gold),
    PLATINUM(MR.strings.platinum, MR.colors.platinum),
    DIAMOND(MR.strings.diamond, MR.colors.diamond),
    COBALT(MR.strings.cobalt, MR.colors.cobalt),
    PEARL(MR.strings.pearl, MR.colors.pearl),
    TOPAZ(MR.strings.topaz, MR.colors.topaz),
    AMETHYST(MR.strings.amethyst, MR.colors.amethyst),
    EMERALD(MR.strings.emerald, MR.colors.emerald),
    ONYX(MR.strings.onyx, MR.colors.onyx),
    RUBY(MR.strings.ruby, MR.colors.ruby);

    val ranks by lazy {
        LadderRank.entries
            .filter { it.group == this }
            .sortedBy { it.classPosition }
    }

    /**
     * @param index the index of the rank inside this group, indexed at 0 (index 0 = COPPER 1)
     */
    fun rankAtIndex(index: Int) = ranks[index]

    fun toLadderRank() = ranks.last()
}

val LadderRankClass?.nameRes get() = when(this) {
    null -> MR.strings.no_rank
    else -> this.nameRes
}

object LadderRankSerializer: KSerializer<LadderRank> {
    override val descriptor = PrimitiveSerialDescriptor("ladderRank", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = LadderRank.parse(decoder.decodeString())!!
    override fun serialize(encoder: Encoder, value: LadderRank) = encoder.encodeString(value.name.lowercase())
}
