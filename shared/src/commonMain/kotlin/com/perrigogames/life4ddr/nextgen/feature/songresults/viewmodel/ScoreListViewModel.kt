package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.api.base.CompositeData
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.feature.banners.enums.BannerLocation
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.IgnoreFilterType
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIScore
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIScoreList
import dev.icerock.moko.resources.desc.Composition
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class ScoreListViewModel(
    private val resultOrganizer: ChartResultOrganizer,
    private val sanbaiAPI: SanbaiAPI,
    private val sanbaiManager: SanbaiManager,
    private val ladderSettings: LadderSettings,
    private val bannerManager: BannerManager,
    private val songResultSettings: SongResultSettings,
): ViewModel(), KoinComponent {

    private val filterViewModel = FilterPanelViewModel()

    private val _state = MutableStateFlow<UIScoreList>(UIScoreList.Empty())
    val state: StateFlow<UIScoreList> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ScoreListEvent>()
    val events: Flow<ScoreListEvent> = _events.asSharedFlow()

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
                songResultSettings.enableDifficultyTiers,
                ladderSettings.useMonospaceScore,
            ) { results, filterView, banner, enableDifficultyTiers, useMonospaceScore ->
                if (results.resultsDone.isEmpty()) {
                    UIScoreList.Empty(
                        filter = filterView,
                        banner = banner
                    )
                } else {
                    UIScoreList.Loaded(
                        scores = results.resultsDone.map { it.toUIScore(enableDifficultyTiers) },
                        useMonospaceFontForScore = useMonospaceScore,
                        filter = filterView,
                        banner = banner
                    )
                }
            }.collect(_state)
        }
    }

    fun handleInput(input: ScoreListInput) = when(input) {
        is ScoreListInput.FilterInput -> filterViewModel.handleInput(input.input)
        ScoreListInput.RefreshSanbaiScores -> viewModelScope.launch {
            if (!sanbaiManager.fetchScores()) {
                _events.emit(ScoreListEvent.ShowSanbaiLogin(sanbaiAPI.getAuthorizeUrl()))
            }
        }
    }
}

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
        perfectsScoreText(clearType, score ?: 0L)
    }
    else -> StringDesc.Composition(
        args = listOf(
            (clearType ?: ClearType.NO_PLAY).clearResShort.desc(),
            (score ?: 0).toString().desc()
        ),
        separator = " - "
    )
}

fun perfectsScoreText(clearType: ClearType, score: Long): StringDesc {
    val perfects = (GameConstants.MAX_SCORE - score) / 10
    return StringDesc.Composition(
        args = listOf(
            clearType.clearResShort.desc(),
            StringDesc.ResourceFormatted(MR.strings.perfects_count, perfects)
        ),
        separator = " - "
    )
}
