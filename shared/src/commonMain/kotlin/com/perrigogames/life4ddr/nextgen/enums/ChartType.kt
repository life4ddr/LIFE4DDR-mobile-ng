package com.perrigogames.life4ddr.nextgen.enums

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ChartType(
    val style: PlayStyle,
    val difficulty: DifficultyClass
) {
    override fun toString(): String = difficulty.aggregatePrefix + style.aggregateSuffix
}

operator fun PlayStyle.plus(difficulty: DifficultyClass) = ChartType(this, difficulty)
operator fun DifficultyClass.plus(style: PlayStyle) = ChartType(style, this)

object ChartTypeSerializer: KSerializer<ChartType> {
    override val descriptor = PrimitiveSerialDescriptor("chartType", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): ChartType {
        val input = decoder.decodeString()
        return ChartType(PlayStyle.parse(input)!!, DifficultyClass.parse(input)!!)
    }
    override fun serialize(encoder: Encoder, value: ChartType) {
        encoder.encodeString("${value.difficulty.aggregatePrefix}${value.style.aggregateSuffix}")
    }
}
