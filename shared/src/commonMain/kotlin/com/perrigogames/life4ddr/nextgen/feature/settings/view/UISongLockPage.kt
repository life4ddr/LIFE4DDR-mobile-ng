package com.perrigogames.life4ddr.nextgen.feature.settings.view

import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class UISongLockPage(
    val title: StringDesc = "".desc(),
    val sections: List<UISongLockSection> = emptyList(),
)

data class UISongLockSection(
    val title: StringDesc,
    val charts: List<StringDesc>
)
