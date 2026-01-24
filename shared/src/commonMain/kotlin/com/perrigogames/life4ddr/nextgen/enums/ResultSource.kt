package com.perrigogames.life4ddr.nextgen.enums

import com.perrigogames.life4ddr.nextgen.data.StableId

enum class ResultSource(override val stableId: Long): StableId {
    MANUAL(0),
    DDR_SCORE_MANAGER(1),
    SANBAI(2),
    TRIAL(3),
}