package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.GoalStatus
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.BaseRankGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.GoalListConfig
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalMapper
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalProgress
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.RankEntry
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.SongsClearGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.toViewData
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.GoalStateManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderDataManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderGoalProgressManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MASettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderData
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoals
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.util.ViewState
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class)
class GoalListViewModel(private val config: GoalListConfig) : ViewModel(), KoinComponent {

    private val ladderDataManager: LadderDataManager by inject()
    private val goalStateManager: GoalStateManager by inject()
    private val ladderGoalProgressManager: LadderGoalProgressManager by inject()
    private val ladderGoalMapper: LadderGoalMapper by inject()
    private val userRankSettings: UserRankSettings by inject()
    private val maSettings: MASettings by inject()
    private val logger by injectLogger("GoalListViewModel")

    private val targetRankFlow: Flow<LadderRank?> = config.targetRank
        ?.let { flowOf(it) }
        ?: userRankSettings.targetRank

    private val requirementsStateFlow: StateFlow<RankEntry?> = targetRankFlow
        .flatMapLatest { targetRank ->
            ladderDataManager.requirementsForRank(targetRank)
        }
        .onEach { logger.v { "requirementsFlow -> $it" } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow<ViewState<UILadderData, String>>(ViewState.Loading).cMutableStateFlow()
    val state: CStateFlow<ViewState<UILadderData, String>> = _state.cStateFlow()

    private val _showBottomSheet = MutableSharedFlow<Unit>()
    val showBottomSheet: SharedFlow<Unit> = _showBottomSheet.asSharedFlow()

    private val _expandedItems = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            // FIXME compile
//            combine(
//                targetRankFlow,
//                requirementsStateFlow,
//                combine(
//                    targetRankFlow,
//                    requirementsStateFlow,
//                ) { target, reqs -> target to reqs }.flatMapLatest { (target, reqs) ->
//                    reqs?.let { ladderGoalProgressManager.getProgressMapFlow(it.allGoals + it.substitutionGoals, target) }
//                        ?: flowOf(emptyMap())
//                },
//                _expandedItems,
//                goalStateManager.updated,
//                maSettings.combineMFCs,
//                maSettings.combineSDPs
//            ) { targetRank, requirements, progress, expanded, _, combineMFCs, combineSDPs ->
//                logger.d { "Updating to $targetRank, requirements = $requirements" }
//                val substitutions = if (requirements != null && requirements.substitutionGoals.isNotEmpty()) {
//                    val goalStates = goalStateManager.getGoalStateList(requirements.substitutionGoals)
//                    UILadderGoals.CategorizedList(
//                        categories = listOf(
//                            UILadderGoals.CategorizedList.Category(
//                                title = MR.strings.substitutions.desc()
//                            ) to requirements.substitutionGoals.map { goal ->
//                                ladderGoalMapper.toViewData(
//                                    base = goal,
//                                    goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
//                                        ?: GoalStatus.INCOMPLETE,
//                                    progress = progress[goal],
//                                    allowHiding = false,
//                                    allowCompleting = config.allowCompleting,
//                                    isExpanded = expanded.contains(goal.id.toLong()),
//                                )
//                            }
//                        )
//                    )
//                } else {
//                    null
//                }
//                when {
//                    targetRank == null -> ViewState.Error("No higher goals found...")
//                    requirements == null -> ViewState.Error("No goals found for ${targetRank.name}")
//                    targetRank >= LadderRank.PLATINUM1 -> ViewState.Success(
//                        UILadderData(
//                            targetRankClass = targetRank.group,
//                            goals = generateDifficultyCategories(requirements, progress, expanded),
//                            substitutions = substitutions
//                        )
//                    )
//                    else -> ViewState.Success(
//                        UILadderData(
//                            targetRankClass = targetRank.group,
//                            goals = generateCommonCategories(requirements, progress, expanded),
//                            substitutions = substitutions
//                        )
//                    )
//                }
//            }.collect { _state.value = it }
        }
    }

    private fun generateCommonCategories(
        requirements: RankEntry,
        progress: Map<BaseRankGoal, LadderGoalProgress?>,
        expanded: Set<Long>,
    ) : UILadderGoals.CategorizedList {
        val goalStates = goalStateManager.getGoalStateList(requirements.allGoals)
        val finishedGoalCount = requirements.allGoals.count { goal ->
            progress[goal]?.isComplete == true ||
                    goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status == GoalStatus.COMPLETE
        }
        val neededGoals = requirements.requirementsOpt ?: 0
        val canHide = config.allowHiding
                && goalStates.count { it.status == GoalStatus.IGNORED } < requirements.allowedIgnores
        return UILadderGoals.CategorizedList(
            categories = listOf(
                UILadderGoals.CategorizedList.Category(
                    title = MR.strings.goals.desc(),
                    goalText = StringDesc.ResourceFormatted(
                        MR.strings.goal_progress_format,
                        finishedGoalCount,
                        neededGoals
                    ),
                ) to requirements.goals.map { goal ->
                    ladderGoalMapper.toViewData(
                        base = goal,
                        progress = progress[goal],
                        allowHiding = canHide,
                        allowCompleting = config.allowCompleting,
                        isExpanded = expanded.contains(goal.id.toLong()),
                    )
                },
                UILadderGoals.CategorizedList.Category(
                    title = MR.strings.mandatory_goals.desc()
                ) to requirements.mandatoryGoals.map { goal ->
                    ladderGoalMapper.toViewData(
                        base = goal,
                        goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
                            ?: GoalStatus.INCOMPLETE,
                        progress = progress[goal],
                        allowHiding = false,
                        allowCompleting = config.allowCompleting,
                        isExpanded = expanded.contains(goal.id.toLong()),
                    )
                }
            )
                .filterNot { it.second.isEmpty() }
        )
    }

    private fun generateDifficultyCategories(
        requirements: RankEntry,
        progress: Map<BaseRankGoal, LadderGoalProgress?>,
        expanded: Set<Long>,
    ) : UILadderGoals.CategorizedList {
        val goalStates = goalStateManager.getGoalStateList(requirements.allGoals)
        val songsClearGoals = requirements.allGoals.filterIsInstance<SongsClearGoal>()
        val remainingGoals = mutableListOf<BaseRankGoal>()
        val substitutionProgress = LadderGoalProgress(
            progress = requirements.substitutionGoals
                .mapNotNull { progress[it] }
                .count { it.isComplete },
            max = requirements.substitutionGoals.size,
            showProgressBar = false,
        )

        val categories = (songsClearGoals.groupBy { it.diffNum }
            .toList()
            as List<Pair<Int?, List<BaseRankGoal>>>)
            .sortedBy { it.first ?: Int.MAX_VALUE }
            .toMutableList()
        val categoryIterator = categories.iterator()
        while (categoryIterator.hasNext()) {
            val (level, goals) = categoryIterator.next()
            if (level == null || level < requirements.rank.group.minDiscreteDifficultyCategory) {
                remainingGoals.addAll(goals)
                categoryIterator.remove()
            }
        }
        remainingGoals.addAll(
            requirements.allGoals
                .filterNot { it is SongsClearGoal }
                .toMutableList()
        )

        val otherIndex = categories.indexOfFirst { it.first == null }
        if (categories.any { it.first == null }) {
            val otherGoals = categories.removeAt(otherIndex).second
            categories.add(otherIndex, null to (otherGoals + remainingGoals))
        } else {
            categories.add(null to remainingGoals)
        }

        return UILadderGoals.CategorizedList(
            categories = categories.map { (level, goals) ->
                val title = level?.let { StringDesc.ResourceFormatted(MR.strings.level_header, it) }
                    ?: MR.strings.other_goals.desc()
                UILadderGoals.CategorizedList.Category(title) to goals.map { goal ->
                    ladderGoalMapper.toViewData(
                        base = goal,
                        goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
                            ?: GoalStatus.INCOMPLETE,
                        progress = progress[goal],
                        allowHiding = !requirements.mandatoryGoalIds.contains(goal.id),
                        allowCompleting = config.allowCompleting,
                        isExpanded = expanded.contains(goal.id.toLong()),
                    )
                }
            } + substitutionsItem(substitutionProgress),
        )
    }

    private fun substitutionsItem(progress: LadderGoalProgress) = UILadderGoals.CategorizedList.Category() to listOf(
        UILadderGoal(
            id = 9999999,
            goalText = MR.strings.substitutions.desc(),
            completed = false,
            canComplete = false,
            showCheckbox = false,
            canHide = false,
            progress = progress.toViewData(),
            expandAction = GoalListInput.ShowSubstitutions,
        )
    )

    fun handleAction(action: GoalListInput) {
        when(action) {
            is GoalListInput.OnGoal -> {
                val state = goalStateManager.getOrCreateGoalState(action.id)

                when (action) {
                    is GoalListInput.OnGoal.ToggleComplete -> {
                        val newStatus = if (state.status == GoalStatus.COMPLETE) {
                            GoalStatus.INCOMPLETE
                        } else {
                            GoalStatus.COMPLETE
                        }
                        goalStateManager.setGoalState(action.id, newStatus)
                    }
                    is GoalListInput.OnGoal.ToggleExpanded -> {
                        if (_expandedItems.value.contains(action.id)) {
                            _expandedItems.value -= action.id
                        } else {
                            _expandedItems.value + action.id
                        }
                    }
                    is GoalListInput.OnGoal.ToggleHidden -> {
                        val newStatus = if (state.status == GoalStatus.IGNORED) {
                            GoalStatus.INCOMPLETE
                        } else {
                            GoalStatus.IGNORED
                        }
                        goalStateManager.setGoalState(action.id, newStatus)
                    }
                }
            }
            GoalListInput.ShowSubstitutions -> {
                viewModelScope.launch { _showBottomSheet.emit(Unit) }
            }
        }
    }
}

val LadderRankClass.minDiscreteDifficultyCategory
    get() = when (this) {
        LadderRankClass.COPPER,
        LadderRankClass.BRONZE,
        LadderRankClass.SILVER,
        LadderRankClass.GOLD -> 0 // These don't use categories anyway
        LadderRankClass.PLATINUM -> 13
        LadderRankClass.DIAMOND,
        LadderRankClass.COBALT,
        LadderRankClass.PEARL,
        LadderRankClass.AMETHYST,
        LadderRankClass.EMERALD,
        LadderRankClass.ONYX -> 14
    }

// Taken from old LadderGoalsViewModel
//            val entry = ladderDataManager.findRankEntry(config.targetRank)
//            if (config.targetRank == null || entry == null) {
//                // TODO some kind of endgame/error handling text
//                return@launch
//            }
//
//            _stateFlow.value = _stateFlow.value.copy(
//                goals = UILadderGoals.SingleList(
//                    entry.allGoals.map {  goal ->
//                        val goalState = goalStateManager.getGoalState(goal)
//                        UILadderGoal(
//                            id = goal.id.toLong(),
//                            goalText = goal.goalString(platformStrings),
//                            completed = goalState?.status == GoalStatus.COMPLETE,
//                            hidden = goalState?.status == GoalStatus.IGNORED,
//                            canHide = false, // FIXME
//                            progress = null, // FIXME
//                            detailItems = emptyList() // FIXME
//                        )
//                    }
//                )
//            )