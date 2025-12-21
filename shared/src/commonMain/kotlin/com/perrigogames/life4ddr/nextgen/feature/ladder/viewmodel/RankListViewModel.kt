package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.GoalListConfig
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderDataManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UIFooterData
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderData
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderRankClass
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UINoRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UIRankList
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.util.ViewState
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RankListViewModel(
    isFirstRun: Boolean = false,
    private val firstRunSettingsManager: FirstRunSettings,
    private val userRankSettings: UserRankSettings,
    private val ladderDataManager: LadderDataManager,
) : ViewModel(), KoinComponent {

    private val selectedRankClass = MutableStateFlow<LadderRankClass?>(null)
    private val selectedRankIndex = MutableStateFlow<Int?>(null)
    private val selectedRank = combine(
        selectedRankClass,
        selectedRankIndex,
    ) { clazz, index ->
        if (clazz == null || index == null) {
            return@combine null
        }
        clazz.rankAtIndex(index)
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val selectedRankGoals = selectedRank
        .flatMapLatest { ladderDataManager.requirementsForRank(it) }
    private var startingRank: LadderRank? = null
    private var rankClassChanged: Boolean = !isFirstRun

    private val goalListViewModel = selectedRank.map { rank ->
        if (rank != null) {
            GoalListViewModel(
                GoalListConfig(
                    targetRank = rank,
                    allowCompleting = true,
                    allowHiding = false,
                )
            )
        } else {
            null
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow(UIRankList())
    val state: StateFlow<UIRankList> = _state.asStateFlow()

    private val _actions = MutableSharedFlow<RankListViewModelEvent>()
    val actions: SharedFlow<RankListViewModelEvent> = _actions.asSharedFlow()

    init {
        viewModelScope.launch {
            startingRank = userRankSettings.rank.value
            selectedRankClass.value = startingRank?.group
            selectedRankIndex.value = startingRank?.classPosition?.minus(1)

            combine(
                selectedRankClass,
                selectedRankGoals,
                goalListViewModel.flatMapLatest { it?.state ?: flowOf(null) }
            ) { rankClass, goalEntry, goalList ->
                val rank = selectedRank.value // base for selectedRankGoals
                val ladderData = (goalList as? ViewState.Success<UILadderData>)?.data
                UIRankList(
                    titleText = when {
                        isFirstRun -> MR.strings.select_a_starting_rank.desc()
                        else -> MR.strings.select_a_new_rank.desc()
                    },
                    showBackButton = !isFirstRun,
                    rankClasses = listOf(UILadderRankClass.NO_RANK) + LadderRankClass.entries.map {
                        UILadderRankClass(it, selected = it == rankClass)
                    },
                    selectedRankClass = rankClass,
                    showRankSelector = rankClassChanged,
                    isRankSelectorCompressed = goalEntry != null,
                    ranks = rankClass?.let {
                        LadderRank.entries.filter { it.group == rankClass }
                            .sortedBy { it.stableId }
                            .mapIndexed { index, r -> UILadderRank(r, index, selected = r == rank) }
                    } ?: emptyList(),
                    noRankInfo = when {
                        isFirstRun -> UINoRank.FIRST_RUN
                        else -> UINoRank.DEFAULT
                    },
                    footer = when {
                        isFirstRun && rank != null -> UIFooterData.firstRunRankSubmit(rank)
                        isFirstRun -> UIFooterData.FIRST_RUN_CANCEL
                        startingRank == rank -> null
                        rank != null -> UIFooterData.changeSubmit(rank)
                        else -> null
                    },
                    ladderData = ladderData
                )
            }.collect(_state)
        }
    }

    fun onInputAction(input: RankListViewModelInput) = viewModelScope.launch {
        when (input) {
            is RankListViewModelInput.RankClassTapped -> {
                rankClassChanged = true
                selectedRankClass.value = input.rankClass
            }
            is RankListViewModelInput.RankTapped -> {
                selectedRankIndex.value = input.index
            }
            is RankListViewModelInput.RankSelected -> {
                firstRunSettingsManager.setInitState(InitState.DONE)
                userRankSettings.setRank(input.rank)
                _actions.emit(RankListViewModelEvent.NavigateToMainScreen)
            }
            RankListViewModelInput.MoveToPlacements -> {
                firstRunSettingsManager.setInitState(InitState.PLACEMENTS)
                _actions.emit(RankListViewModelEvent.NavigateToPlacements)
            }
            RankListViewModelInput.RankRejected -> {
                firstRunSettingsManager.setInitState(InitState.DONE)
                userRankSettings.setRank(null)
                _actions.emit(RankListViewModelEvent.NavigateToMainScreen)
            }
            is RankListViewModelInput.GoalList -> {
                goalListViewModel.value!!.handleInput(input.input)
            }
        }
    }
}
