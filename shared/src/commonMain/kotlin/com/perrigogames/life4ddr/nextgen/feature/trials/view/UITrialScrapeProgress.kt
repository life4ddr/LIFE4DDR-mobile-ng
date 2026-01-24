package com.perrigogames.life4ddr.nextgen.feature.trials.view

import dev.icerock.moko.resources.desc.StringDesc

data class UITrialScrapeProgress(
    val topText: StringDesc,
    val bottomText: StringDesc,
    val progress: Float? = null,
)
