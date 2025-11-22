package com.perrigogames.life4ddr.nextgen.feature.trials.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TrialJacketCorner {
    @SerialName("new") NEW,
    @SerialName("event") EVENT,
    @SerialName("none") NONE
}