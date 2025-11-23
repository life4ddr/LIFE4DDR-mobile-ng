package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

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
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.util.ViewState
import dev.icerock.moko.mvvm.flow.*
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RankListViewModel(
    isFirstRun: Boolean = false
) : ViewModel(), KoinComponent {

    private val firstRunSettingsManager: FirstRunSettings by inject()
    private val userInfoSettings: UserInfoSettings by inject()
    private val userRankSettings: UserRankSettings by inject()
    private val ladderDataManager: LadderDataManager by inject()

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

    private val _state = MutableStateFlow(UIRankList()).cMutableStateFlow()
    val state: CStateFlow<UIRankList> = _state.cStateFlow()

    private val _actions = MutableSharedFlow<RankListViewModelEvent>()
    val actions: CFlow<RankListViewModelEvent> = _actions.cFlow()

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
                        isFirstRun -> MR.strings.select_a_starting_rank
                        else -> MR.strings.select_a_new_rank
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
                goalListViewModel.value!!.handleAction(input.input)
            }
        }
    }
}
