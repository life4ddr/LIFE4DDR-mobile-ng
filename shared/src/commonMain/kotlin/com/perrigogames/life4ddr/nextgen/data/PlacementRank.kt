package com.perrigogames.life4ddr.nextgen.data

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class PlacementRank(
    val stableId: Long,
    val parent: LadderRankClass
) {
    COPPER(20, LadderRankClass.COPPER),
    BRONZE(25, LadderRankClass.BRONZE),
    SILVER(30, LadderRankClass.SILVER),
    GOLD(35, LadderRankClass.GOLD);

    fun toLadderRank() = when(this) {
        COPPER -> LadderRank.COPPER3
        BRONZE -> LadderRank.BRONZE3
        SILVER -> LadderRank.SILVER3
        GOLD -> LadderRank.GOLD3
    }
}

object PlacementRankSerializer: KSerializer<PlacementRank> {
    override val descriptor = PrimitiveSerialDescriptor("placementRank", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = PlacementRank.valueOf(decoder.decodeString().uppercase())
    override fun serialize(encoder: Encoder, value: PlacementRank) = encoder.encodeString(value.name.lowercase())
}
