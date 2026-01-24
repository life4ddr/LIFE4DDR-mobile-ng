package com.perrigogames.life4ddr.nextgen.feature.songresults.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.banners.view.UIBanner
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.FilterPanelInput
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListInput
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

sealed class UIScoreList {

    open val filter: UIFilterView = UIFilterView()
    open val banner: UIBanner? = null

    data class Loaded(
        val scores: List<UIScore> = emptyList(),
        val useMonospaceFontForScore: Boolean = false,
        override val filter: UIFilterView = UIFilterView(),
        override val banner: UIBanner? = null,
    ) : UIScoreList()

    data class Empty(
        val title: StringDesc = MR.strings.score_list_empty_title.desc(),
        val subtitle: StringDesc = MR.strings.score_list_empty_subtitle.desc(),
        val ctaText: StringDesc = MR.strings.score_list_empty_cta.desc(),
        val ctaInput: ScoreListInput = ScoreListInput.FilterInput(FilterPanelInput.ResetFilter),
        override val filter: UIFilterView = UIFilterView(),
        override val banner: UIBanner? = null,
    ) : UIScoreList()
}

data class UIScore(
    val chart: Chart,
    val titleText: String = "",
    val difficultyText: StringDesc,
    val scoreText: StringDesc,
    val difficultyColor: ColorResource,
    val scoreColor: ColorResource,
    val flareLevel: Int? = null,
)
