package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.clearResShort
import com.perrigogames.life4ddr.nextgen.enums.colorRes
import com.perrigogames.life4ddr.nextgen.enums.nameRes
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerLocation
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.banners.UIBanner
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.IgnoreFilterType
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIFilterAction
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIFilterView
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.Composition
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScoreListViewModel: ViewModel(), KoinComponent {

    private val resultOrganizer: ChartResultOrganizer by inject()
    private val sanbaiAPI: SanbaiAPI by inject()
    private val sanbaiManager: SanbaiManager by inject()
    private val bannerManager: BannerManager by inject()
    private val songResultSettings: SongResultSettings by inject()

    private val filterViewModel = FilterPanelViewModel()

    private val _state = MutableStateFlow(UIScoreList()).cMutableStateFlow()
    val state: CStateFlow<UIScoreList> = _state.cStateFlow()

    init {
        viewModelScope.launch {
            combine(
                combine(
                    filterViewModel.dataState,
                    songResultSettings.enableDifficultyTiers,
                    songResultSettings.showRemovedSongs,
                ) { a, b, c -> Triple(a, b, c) }
                    .flatMapLatest { (config, enableDifficultyTiers, showRemovedSongs) ->
                        resultOrganizer.resultsForConfig(null, config.copy(
                            chartFilter = config.chartFilter.copy(
                                ignoreFilterType = if (showRemovedSongs) {
                                    IgnoreFilterType.ALL
                                } else {
                                    IgnoreFilterType.ALL_ACTIVE
                                }
                            )
                        ), enableDifficultyTiers)
                    },
                filterViewModel.uiState,
                bannerManager.getBannerFlow(BannerLocation.SCORES),
                songResultSettings.enableDifficultyTiers
            ) { results, filterView, banner, enableDifficultyTiers ->
                UIScoreList(
                    scores = results.resultsDone.map { it.toUIScore(enableDifficultyTiers) },
                    filter = filterView,
                    banner = banner
                )
            }.collect(_state)
        }
    }

    fun handleFilterAction(action: UIFilterAction) {
        filterViewModel.handleAction(action)
    }

    fun requiresAuthorization() = sanbaiManager.requiresAuthorization()

    fun getSanbaiUrl() = sanbaiAPI.getAuthorizeUrl()

    /**
     * @return [true] if the scores are able to be fetched, [false] if the user needs to reauth
     */
    suspend fun refreshSanbaiScores(): Boolean {
        return sanbaiManager.fetchScores()
    }
}

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

fun ChartResultPair.toUIScore(enableDifficultyTiers: Boolean) = UIScore(
    titleText = KsoupEntities.decodeHtml(chart.song.title),
    difficultyText = StringDesc.Composition(
        args = listOf(
            chart.difficultyClass.nameRes.desc(),
            if (enableDifficultyTiers) {
                chart.combinedDifficultyNumberString.desc()
            } else {
                chart.difficultyNumber.toString().desc()
            }
        ),
        separator = " - "
    ),
    scoreText = scoreText(result?.clearType, result?.score),
    difficultyColor = chart.difficultyClass.colorRes,
    scoreColor = (result?.clearType ?: ClearType.NO_PLAY).colorRes,
    flareLevel = result?.flare?.toInt()
)

fun scoreText(clearType: ClearType?, score: Long?) = when (clearType) {
    ClearType.MARVELOUS_FULL_COMBO -> MR.strings.clear_mfc_caps.desc()
    ClearType.SINGLE_DIGIT_PERFECTS,
    ClearType.PERFECT_FULL_COMBO -> {
        val perfects = (GameConstants.MAX_SCORE - (score ?: 0)) / 10
        StringDesc.Composition(
            args = listOf(
                clearType.clearResShort.desc(),
                StringDesc.ResourceFormatted(MR.strings.perfects_count, perfects)
            ),
            separator = " - "
        )
    }
    else -> StringDesc.Composition(
        args = listOf(
            (clearType ?: ClearType.NO_PLAY).clearResShort.desc(),
            (score ?: 0).toString().desc()
        ),
        separator = " - "
    )
}
