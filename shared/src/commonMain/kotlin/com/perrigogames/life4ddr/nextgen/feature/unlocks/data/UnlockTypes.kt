package com.perrigogames.life4ddr.nextgen.feature.unlocks.data

import com.perrigogames.life4ddr.nextgen.data.Versioned
import kotlinx.serialization.Serializable

@Serializable
data class UnlockTypes(
    override val version: Long,
    val types: List<UnlockType> = emptyList(),
) : Versioned

@Serializable
data class UnlockType(
    val key: Int,
    val name: String,
    val isExpanded: Boolean = true,
)