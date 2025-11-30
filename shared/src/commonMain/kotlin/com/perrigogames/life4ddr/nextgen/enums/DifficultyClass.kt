package com.perrigogames.life4ddr.nextgen.enums

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.StableId
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enum to describe a class of difficulty, inside which more specific difficulties
 * are more or less in the same range as each other.
 */
@Serializable
enum class DifficultyClass(
    override val stableId: Long,
    val aggregatePrefix: String,
    val nameRes: StringResource,
    val colorRes: ColorResource,
): StableId {
    @SerialName("beginner") BEGINNER(1, "b", MR.strings.beginner, MR.colors.difficultyBeginner),
    @SerialName("basic") BASIC(2, "B", MR.strings.basic, MR.colors.difficultyBasic),
    @SerialName("difficult") DIFFICULT(3, "D", MR.strings.difficult, MR.colors.difficultyDifficult),
    @SerialName("expert") EXPERT(4, "E", MR.strings.expert, MR.colors.difficultyExpert),
    @SerialName("challenge") CHALLENGE(5, "C", MR.strings.challenge, MR.colors.difficultyChallenge),
    ;

    fun aggregateString(playStyle: PlayStyle) = playStyle.aggregateString(this)

    companion object {
        fun parse(stableId: Long?) = stableId?.let { id -> entries.firstOrNull { it.stableId == id } }
        fun parse(chartString: String): DifficultyClass? = entries.firstOrNull { chartString.startsWith(it.aggregatePrefix) }
    }
}

object DifficultyClassSerializer: KSerializer<DifficultyClass> {
    override val descriptor = PrimitiveSerialDescriptor("difficultyClass", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = decoder.decodeString().let {
        DifficultyClass.parse(it) ?: DifficultyClass.valueOf(it.uppercase())
    }
    override fun serialize(encoder: Encoder, value: DifficultyClass) = encoder.encodeString(value.name.lowercase())
}
