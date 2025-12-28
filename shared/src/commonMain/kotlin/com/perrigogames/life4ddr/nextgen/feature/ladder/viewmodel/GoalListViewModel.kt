package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MAConfig
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MASettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderData
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoals
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.util.ViewState
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.listOf
import kotlin.to

@OptIn(ExperimentalCoroutinesApi::class)
class GoalListViewModel(private val config: GoalListConfig) : ViewModel(), KoinComponent {

    private val ladderDataManager: LadderDataManager by inject()
    private val goalStateManager: GoalStateManager by inject()
    private val ladderGoalProgressManager: LadderGoalProgressManager by inject()
    private val ladderGoalMapper: LadderGoalMapper by inject()
    private val ladderSettings: LadderSettings by inject()
    private val userRankSettings: UserRankSettings by inject()
    private val songResultSettings: SongResultSettings by inject()
    private val maSettings: MASettings by inject()
    private val logger by injectLogger("GoalListViewModel")

    private val targetRankFlow: Flow<LadderRank?> = config.targetRank
        ?.let { flowOf(it) }
        ?: userRankSettings.targetRank

    private val requirementsStateFlow: StateFlow<RankEntry?> =
        targetRankFlow
            .flatMapLatest { ladderDataManager.requirementsForRank(it) }
            .onEach { logger.v { "requirementsFlow -> $it" } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow<ViewState<UILadderData, String>>(ViewState.Loading)
    val state: StateFlow<ViewState<UILadderData, String>> = _state.asStateFlow()

    private val options = combine(
        maSettings.maConfig,
        songResultSettings.enableDifficultyTiers,
        ladderSettings.useMonospaceScore,
        ladderSettings.hideCompletedGoals,
    ) { maConfig, enableDifficultyTiers, useMonospaceScore, hideCompletedGoals ->
        Options(maConfig, enableDifficultyTiers, useMonospaceScore, hideCompletedGoals)
    }

    private val _showBottomSheet = MutableSharedFlow<Unit>()
    val showBottomSheet: SharedFlow<Unit> = _showBottomSheet.asSharedFlow()

    private val _expandedItems = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            combine(
                targetRankFlow,
                requirementsStateFlow,
                combine(
                    targetRankFlow,
                    requirementsStateFlow,
                ) { target, reqs -> target to reqs }.flatMapLatest { (target, reqs) ->
                    reqs?.let { ladderGoalProgressManager.getProgressMapFlow(it.allGoals + it.substitutionGoals, target) }
                        ?: flowOf(emptyMap())
                },
                _expandedItems,
//                goalStateManager.updated,
                options
            ) { targetRank, requirements, progress, expanded, options ->
                logger.d { "Updating to $targetRank, requirements = $requirements, expanded = $expanded" }
                val mapperOptions = LadderGoalMapper.Options(
                    maConfig = options.maConfig,
                    showDiffTiers = options.enableDifficultyTiers,
                    hideCompletedGoals = config.allowHidingCompletedGoals && options.hideCompletedGoals,
                )
                val substitutions = if (requirements?.substitutionGoals?.isNotEmpty() == true) {
                    val goalStates = goalStateManager.getGoalStateList(requirements.substitutionGoals)
                    UILadderGoals.CategorizedList(
                        categories = listOf(
                            UILadderGoals.CategorizedList.Category(
                                title = MR.strings.substitutions.desc()
                            ) to requirements.substitutionGoals.map { goal ->
                                ladderGoalMapper.toViewData(
                                    base = goal,
                                    goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
                                        ?: GoalStatus.INCOMPLETE,
                                    progress = progress[goal],
                                    options = mapperOptions.copy (
                                        allowHiding = false,
                                        allowCompleting = config.allowCompletingGoalsManually,
                                        isExpanded = expanded.contains(goal.id.toLong()),
                                    ),
                                )
                            }
                        )
                    )
                } else {
                    null
                }
                val hideCompletedToggle = if (config.allowHidingCompletedGoals) {
                    UILadderData.HideCompletedToggle(
                        enabled = options.hideCompletedGoals,
                        toggleAction = GoalListInput.ToggleShowCompleted(!options.hideCompletedGoals)
                    )
                } else {
                    null
                }
                when {
                    targetRank == null -> ViewState.Error("No higher goals found...")
                    requirements == null -> ViewState.Error("No goals found for ${targetRank.name}")
                    targetRank >= LadderRank.PLATINUM1 -> ViewState.Success(
                        UILadderData(
                            targetRankClass = targetRank.group,
                            goals = generateDifficultyCategories(requirements, progress, expanded, mapperOptions),
                            substitutions = substitutions,
                            hideCompleted = hideCompletedToggle,
                            useMonospaceFontForScore = options.useMonospaceScore,
                        )
                    )
                    else -> ViewState.Success(
                        UILadderData(
                            targetRankClass = targetRank.group,
                            goals = generateCommonCategories(requirements, progress, expanded, mapperOptions),
                            substitutions = substitutions,
                            hideCompleted = hideCompletedToggle,
                            useMonospaceFontForScore = options.useMonospaceScore,
                        )
                    )
                }
            }.collect { _state.value = it }
        }
    }

    private fun generateCommonCategories(
        requirements: RankEntry,
        progress: Map<BaseRankGoal, LadderGoalProgress?>,
        expanded: Set<Long>,
        mapperOptions: LadderGoalMapper.Options,
    ) : UILadderGoals.CategorizedList {
        val goalStates = goalStateManager.getGoalStateList(requirements.allGoals)
        val finishedGoalCount = requirements.allGoals.count { goal ->
            progress[goal]?.isComplete == true ||
                    goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status == GoalStatus.COMPLETE
        }
        val neededGoals = requirements.requirementsOpt ?: 0
        val canHide = config.allowHidingIndividualGoals
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
                ) to requirements.goals
                    .filterCompletedGoals(mapperOptions.hideCompletedGoals, progress)
                    .map { goal ->
                        ladderGoalMapper.toViewData(
                            base = goal,
                            progress = progress[goal],
                            options = mapperOptions.copy(
                                allowHiding = canHide,
                                allowCompleting = config.allowCompletingGoalsManually,
                                isExpanded = expanded.contains(goal.id.toLong()),
                            ),
                        )
                    },
                UILadderGoals.CategorizedList.Category(
                    title = MR.strings.mandatory_goals.desc()
                ) to requirements.mandatoryGoals
                    .filterCompletedGoals(mapperOptions.hideCompletedGoals, progress)
                    .map { goal ->
                        ladderGoalMapper.toViewData(
                            base = goal,
                            goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
                                ?: GoalStatus.INCOMPLETE,
                            progress = progress[goal],
                            options = mapperOptions.copy(
                                allowHiding = false,
                                allowCompleting = config.allowCompletingGoalsManually,
                                isExpanded = expanded.contains(goal.id.toLong()),
                            ),
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
        mapperOptions: LadderGoalMapper.Options,
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
                val title = level?.let { StringDesc.ResourceFormatted(MR.strings.rank_goal_category_level, it) }
                    ?: MR.strings.other_goals.desc()

                val goalCompletion = goals.countOverallGoalProgress(progress)
                val goalText = if (!goalCompletion.isComplete) {
                    StringDesc.ResourceFormatted(
                        MR.strings.goal_progress_completed_format,
                        goalCompletion.progress.toInt(),
                        goalCompletion.max.toInt(),
                    )
                } else { null }
                val goalIcon = if (goalCompletion.isComplete) { MR.images.check_circle_filled } else { null }

                UILadderGoals.CategorizedList.Category(
                    title = title,
                    goalText = goalText,
                    goalIcon = goalIcon,
                ) to goals
                    .filterCompletedGoals(mapperOptions.hideCompletedGoals, progress)
                    .map { goal ->
                        ladderGoalMapper.toViewData(
                            base = goal,
                            goalStatus = goalStates.firstOrNull { it.goalId == goal.id.toLong() }?.status
                                ?: GoalStatus.INCOMPLETE,
                            progress = progress[goal],
                            options = mapperOptions.copy(
                                allowHiding = !requirements.mandatoryGoalIds.contains(goal.id),
                                allowCompleting = config.allowCompletingGoalsManually,
                                isExpanded = expanded.contains(goal.id.toLong()),
                            ),
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

    fun handleInput(action: GoalListInput) {
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
                            _expandedItems.value += action.id
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
            is GoalListInput.ToggleShowCompleted -> {
                ladderSettings.setHideCompletedGoals(action.showCompleted)
            }
        }
    }

    private fun List<BaseRankGoal>.countOverallGoalProgress(
        progress: Map<BaseRankGoal, LadderGoalProgress?>
    ) = LadderGoalProgress(
        progress = count { progress[it]?.isComplete == true },
        max = size,
    )

    private fun List<BaseRankGoal>.filterCompletedGoals(
        shouldFilter: Boolean,
        progress: Map<BaseRankGoal, LadderGoalProgress?>
    ) = filterNot { shouldFilter && progress[it]?.isComplete == true }

    data class Options(
        val maConfig: MAConfig,
        val enableDifficultyTiers: Boolean,
        val useMonospaceScore: Boolean,
        val hideCompletedGoals: Boolean,
    )
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
        LadderRankClass.TOPAZ,
        LadderRankClass.AMETHYST,
        LadderRankClass.EMERALD,
        LadderRankClass.ONYX,
        LadderRankClass.RUBY-> 14
    }
