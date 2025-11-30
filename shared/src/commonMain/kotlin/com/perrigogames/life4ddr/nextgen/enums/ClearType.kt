package com.perrigogames.life4ddr.nextgen.enums

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.StableId
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enum to describe the possible ways to finish a song, and consequently an
 * entire folder.
 * @param lampRes the text for describing a lamp ("Green lamp (GFC)")
 * @param clearRes the text for describing a clear ("Great Full Combo")
 * @param clearResShort the text for describing a clear as an abbreviation ("GFC", "PFC")
 */
enum class ClearType(
    override val stableId: Long,
    val serialized: List<String>,
    val uiName: StringDesc,
    val passing: Boolean = true,
): StableId {
    NO_PLAY(0, "no_play", MR.strings.not_played.desc(), passing = false),
    FAIL(1, "fail", MR.strings.fail.desc(), passing = false),
    CLEAR(2, "clear", MR.strings.clear.desc()),
    LIFE4_CLEAR(3, listOf("life4", "life4_clear"), MR.strings.clear_life4.desc()),
    GOOD_FULL_COMBO(4, listOf("fc", "good"), MR.strings.clear_fc.desc()),
    GREAT_FULL_COMBO(5, listOf("gfc", "great"), MR.strings.clear_gfc.desc()),
    PERFECT_FULL_COMBO(6, listOf("pfc", "perfect"), MR.strings.clear_pfc.desc()),
    SINGLE_DIGIT_PERFECTS(7, listOf("sdp", "single_digit_perfects"), MR.strings.clear_sdp.desc()),
    MARVELOUS_FULL_COMBO(8, listOf("mfc", "marvelous"), MR.strings.clear_mfc.desc());

    constructor(
        stableId: Long,
        serialized: String,
        uiName: StringDesc,
        passing: Boolean = true
    ): this(stableId, listOf(serialized), uiName, passing)

    val lampRes get() = when(this) {
        NO_PLAY -> MR.strings.not_played
        FAIL -> MR.strings.fail
        CLEAR -> MR.strings.lamp_clear
        LIFE4_CLEAR -> MR.strings.lamp_life4
        GOOD_FULL_COMBO -> MR.strings.lamp_fc
        GREAT_FULL_COMBO -> MR.strings.lamp_gfc
        PERFECT_FULL_COMBO,
        SINGLE_DIGIT_PERFECTS -> MR.strings.lamp_pfc
        MARVELOUS_FULL_COMBO -> MR.strings.lamp_mfc
    }

    val clearRes get() = when(this) {
        NO_PLAY -> MR.strings.not_played
        FAIL -> MR.strings.fail
        CLEAR -> MR.strings.clear
        LIFE4_CLEAR -> MR.strings.clear_life4
        GOOD_FULL_COMBO -> MR.strings.clear_fc
        GREAT_FULL_COMBO -> MR.strings.clear_gfc
        PERFECT_FULL_COMBO -> MR.strings.clear_pfc
        SINGLE_DIGIT_PERFECTS -> MR.strings.clear_sdp
        MARVELOUS_FULL_COMBO -> MR.strings.clear_mfc
    }

    val clearResShort get() = when(this) {
        GOOD_FULL_COMBO -> MR.strings.clear_fc_short
        GREAT_FULL_COMBO -> MR.strings.clear_gfc_short
        PERFECT_FULL_COMBO -> MR.strings.clear_pfc_short
        SINGLE_DIGIT_PERFECTS -> MR.strings.clear_sdp_short
        MARVELOUS_FULL_COMBO -> MR.strings.clear_mfc_short
        else -> clearRes
    }

    val colorRes get() = when(this) {
        NO_PLAY -> MR.colors.no_play
        FAIL -> MR.colors.fail
        CLEAR -> MR.colors.clear
        LIFE4_CLEAR -> MR.colors.life4
        GOOD_FULL_COMBO -> MR.colors.good
        GREAT_FULL_COMBO -> MR.colors.great
        PERFECT_FULL_COMBO,
        SINGLE_DIGIT_PERFECTS -> MR.colors.perfect
        MARVELOUS_FULL_COMBO -> MR.colors.marvelous
    }

    companion object {
        fun parse(stableId: Long?) = stableId?.let { id -> entries.firstOrNull { it.stableId == id } }

        fun parse(v: String) = entries.firstOrNull { it.serialized.contains(v) }

        /**
         * Parse the info received from a ScoreAttack update into a proper clear type
         */
        fun parseSA(grade: String, fullCombo: String): ClearType {
            return when {
                grade == "NoPlay" -> NO_PLAY
                fullCombo == "MerverousFullCombo" -> MARVELOUS_FULL_COMBO
                // FIXME determine SDP
                fullCombo == "PerfectFullCombo" -> PERFECT_FULL_COMBO
                fullCombo == "FullCombo" -> GREAT_FULL_COMBO
                fullCombo == "GoodFullCombo"-> GOOD_FULL_COMBO
                fullCombo == "Life4"-> LIFE4_CLEAR
                else -> CLEAR
            }
        }
    }
}

object ClearTypeSerializer: KSerializer<ClearType> {
    override val descriptor = PrimitiveSerialDescriptor("clearType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = ClearType.parse(
        decoder.decodeString()
    )!!
    override fun serialize(encoder: Encoder, value: ClearType) {
        encoder.encodeString(value.serialized.first())
    }
}
