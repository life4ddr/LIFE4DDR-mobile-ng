package com.perrigogames.life4ddr.nextgen.feature.songresults.view

import com.perrigogames.life4ddr.nextgen.feature.banners.view.UIBanner
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.StringDesc

data class UIScoreList(
    val scores: List<UIScore> = emptyList(),
    val filter: UIFilterView = UIFilterView(),
    val banner: UIBanner? = null,
)

data class UIScore(
    val titleText: String = "",
    val difficultyText: StringDesc,
    val scoreText: StringDesc,
    val difficultyColor: ColorResource,
    val scoreColor: ColorResource,
    val flareLevel: Int? = null,
)
