package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialGoalStrings
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UIEXScoreBar
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITargetRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialBottomSheet
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.toAchieved
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.toInProgress
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType
import com.perrigogames.life4ddr.nextgen.feature.trialsession.manager.TrialContentProvider
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.util.ViewState
import com.perrigogames.life4ddr.nextgen.util.toViewState
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.asImageDesc
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class TrialSessionViewModel(
    trialId: String,
    private val userRankSettings: UserRankSettings,
    private val trialDataManager: TrialDataManager,
    private val trialRecordsManager: TrialRecordsManager,
    private val logger: Logger
) : ViewModel(), KoinComponent {

    private val trial = trialDataManager.trialsFlow.value.firstOrNull { it.id == trialId }
        ?: throw IllegalStateException("Can't find trial with id $trialId")

    private val contentProvider = TrialContentProvider(trial = trial)

    private val targetRank = MutableStateFlow(TrialRank.BRONZE)
    private val _state = MutableStateFlow<ViewState<UITrialSession, Unit>>(ViewState.Loading)
    val state: StateFlow<ViewState<UITrialSession, Unit>> = _state.asStateFlow()

    private val _bottomSheetState = MutableStateFlow<UITrialBottomSheet?>(null)
    val bottomSheetState: StateFlow<UITrialBottomSheet?> = _bottomSheetState
        .flatMapLatest { state ->
            if (state is UITrialBottomSheet.DetailsPlaceholder) {
                songEntryViewModel.filterNotNull()
                    .flatMapLatest { it.state }
                    .map { details ->
                        details.copy(onDismissAction = state.onDismissAction)
                    }
            } else flowOf(state)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    

    private val _events = MutableSharedFlow<TrialSessionEvent>()
    val events: SharedFlow<TrialSessionEvent> = _events.asSharedFlow()

    private val stage = MutableStateFlow<Int?>(null)
    private val inProgressSessionFlow = MutableStateFlow(InProgressTrialSession(trial))
    private val inProgressSession get() = inProgressSessionFlow.value

    private val songEntryViewModel = MutableStateFlow<SongEntryViewModel?>(null)

    init {
        viewModelScope.launch {
            targetRank.collect { target ->
                val trial = trialDataManager.trialsFlow.value
                    .firstOrNull { it.id == trialId }
                    ?: return@collect
                val current = (_state.value as? ViewState.Success)?.data ?: return@collect
                if (current.targetRank.rank != target) {
                    _state.value = when (val curr = current.targetRank) {
                        is UITargetRank.Selection -> current.copy(
                            targetRank = curr.copy(
                                rank = target,
                                title = target.nameRes.desc(),
                                titleColor = target.colorRes,
                                availableRanks = curr.availableRanks,
                                rankGoalItems = TrialGoalStrings.generateGoalStrings(trial.goalSet(target)!!, trial)
                            )
                        )
                        is UITargetRank.InProgress -> current.copy(
                            targetRank = curr.copy(
                                rank = target,
                                title = target.nameRes.desc(),
                                titleColor = target.colorRes,
                                rankGoalItems = TrialGoalStrings.generateGoalStrings(trial.goalSet(target)!!, trial)
                            )
                        )
                        is UITargetRank.Achieved -> current.copy(
                            targetRank = curr.copy(
                                rank = target,
                                title = target.nameRes.desc(),
                                titleColor = target.colorRes,
                            )
                        )
                    }.toViewState()
                }
            }
        }

        viewModelScope.launch {
            combine(
                inProgressSessionFlow.filterNotNull(),
                stage.filterNotNull(),
            ) { session, stage ->
                logger.d { "Creating stage $stage" }
                val complete = stage >= 4
                val current = (_state.value as? ViewState.Success)?.data ?: return@combine
                val currentEx = session.results.sumOf { it?.exScore ?: 0 }
                val targetRank = when (val target = current.targetRank) {
                    is UITargetRank.Selection -> target.toInProgress()
                    is UITargetRank.InProgress -> target
                    is UITargetRank.Achieved -> throw IllegalStateException("Can't move from Achieved to In Progress")
                }
                _state.value = if (complete) {
                    current.copy(
                        targetRank = targetRank.toAchieved(), // FIXME calculate the user's actual rank
                        exScoreBar = current.exScoreBar.copy(
                            currentEx = currentEx,
                            currentExText = StringDesc.Raw(currentEx.toString())
                        ),
                        content = contentProvider.provideFinalScreen(session),
                        buttonText = MR.strings.take_results_photo.desc(),
                        buttonAction = TrialSessionInput.TakeResultsPhoto,
                    )
                } else {
                    current.copy(
                        targetRank = targetRank,
                        exScoreBar = current.exScoreBar.copy(
                            currentEx = currentEx,
                            currentExText = StringDesc.Raw(currentEx.toString())
                        ),
                        content = contentProvider.provideMidSession(session, stage, targetRank.rank),
                        buttonText = MR.strings.take_photo.desc(),
                        buttonAction = TrialSessionInput.TakePhoto(stage),
                    )
                }.toViewState()
            }.collect()
        }

        val trial = trialDataManager.trialsFlow.value
            .firstOrNull { it.id == trialId }
        if (trial == null) {
            _state.value = ViewState.Error(Unit)
        } else {
            val bestSession = trialRecordsManager.bestSessions.value
                .firstOrNull { it.trialId == trialId }
            val allowedRanks = trial.goals?.map { it.rank } ?: emptyList()
            val rank = getStartingRank(
                playerRank = userRankSettings.rank.value,
                bestRank = bestSession?.goalRank,
                allowedRanks = allowedRanks
            )
            val bestSessionExScore = bestSession?.exScore?.toInt() ?: 0
            _state.value = ViewState.Success(
                UITrialSession(
                    trialTitle = trial.name.desc(),
                    trialLevel = StringDesc.ResourceFormatted(
                        MR.strings.level_short_format,
                        trial.difficulty ?: 0
                    ),
                    backgroundImage = trial.coverResource ?: MR.images.trial_default.asImageDesc(),
                    exScoreBar = UIEXScoreBar(
                        labelText = MR.strings.ex.desc(),
                        currentEx = bestSessionExScore,
                        currentExText = StringDesc.Raw(bestSessionExScore.toString()),
                        maxEx = trial.totalEx,
                        maxExText = StringDesc.Raw("/" + trial.totalEx)
                    ),
                    targetRank = UITargetRank.Selection(
                        rank = rank,
                        title = rank.nameRes.desc(),
                        titleColor = rank.colorRes,
                        availableRanks = allowedRanks,
                        rankGoalItems = TrialGoalStrings.generateGoalStrings(trial.goalSet(rank)!!, trial),
                    ),
                    content = contentProvider.provideSummary(),
                    buttonText = MR.strings.placement_start.desc(),
                    buttonAction = TrialSessionInput.StartTrial(fromDialog = false),
                )
            )
            targetRank.value = rank
        }
    }

    fun handleAction(action: TrialSessionInput) {
        logger.d { "Handling action $action" }
        when (action) {
            is TrialSessionInput.ChangeTargetRank -> {
                targetRank.value = action.target
            }

            is TrialSessionInput.StartTrial -> {
                if (action.fromDialog) {
                    stage.value = 0
                } else {
                    viewModelScope.launch {
                        _events.emit(
                            TrialSessionEvent.ShowWarningDialog(
                                title = MR.strings.trial_warning_dialog_title.desc(),
                                body = MR.strings.trial_warning_dialog_body.desc(),
                                ctaCancelText = MR.strings.cancel.desc(),
                                ctaConfirmText = MR.strings.trial_warning_dialog_play.desc(),
                                ctaConfirmInput = TrialSessionInput.StartTrial(fromDialog = true),
                            )
                        )
                    }
                }
            }

            is TrialSessionInput.TakePhoto -> {
                _bottomSheetState.value = UITrialBottomSheet.ImageCapture(action.index)
            }

            is TrialSessionInput.TakeResultsPhoto -> {
                _bottomSheetState.value = UITrialBottomSheet.ImageCapture(null)
            }

            is TrialSessionInput.PhotoTaken -> {
                inProgressSessionFlow.update { session ->
                    session.createOrUpdateSongResult(action.index, action.photoUri)
                }
                showSongEntry(
                    index = action.index,
                    isEdit = false,
                    onDismissAction = TrialSessionInput.AdvanceStage
                )
            }

            is TrialSessionInput.ResultsPhotoTaken -> {
                updateTargetRank(allowIncrease = true)
                viewModelScope.launch {
                    inProgressSessionFlow.update { session ->
                        session.copy(
                            finalPhotoUriString = action.photoUri
                        )
                    }
                    // TODO acquire the images and upload them to the API
                    inProgressSession.goalObtained = true // FIXME
                    trialRecordsManager.saveSession(inProgressSession, targetRank.value)
                    _events.emit(TrialSessionEvent.Close)
                }
            }

            TrialSessionInput.HideBottomSheet -> {
                hideSongEntry()
            }

            is TrialSessionInput.EditItem -> {
                if (inProgressSession.results.getOrNull(action.index) != null) {
                    showSongEntry(
                        index = action.index,
                        isEdit = true,
                    )
                }
            }

            is TrialSessionInput.ChangeText -> {
                songEntryViewModel.value!!.changeText(action.id, action.text)
            }

            TrialSessionInput.AdvanceStage -> {
                hideSongEntry()
                stage.value = (stage.value ?: 0) + 1
            }

            is TrialSessionInput.UseShortcut -> {
                songEntryViewModel.value!!.setShortcutState(action.shortcut)
            }

            is TrialSessionInput.ManualScoreEntry -> {
                viewModelScope.launch {
                    trialRecordsManager.saveFakeSession(
                        trial = trial,
                        targetRank = action.rank,
                        exScore = action.ex,
                    )
                    _events.emit(TrialSessionEvent.Close)
                }
            }
        }
    }

    private fun getStartingRank(
        playerRank: LadderRank?,
        bestRank: TrialRank?,
        allowedRanks: List<TrialRank>
    ): TrialRank {
        var curr = if (bestRank != null) {
            bestRank.next
        } else {
            TrialRank.fromLadderRank(playerRank, parsePlatinum = true)
        }
        while (curr != null) {
            if (allowedRanks.contains(curr)) {
                return curr
            }
            curr = curr.next
        }
        return bestRank ?: allowedRanks.first()
    }

    private fun showSongEntry(
        index: Int,
        shortcut: ShortcutType? = null,
        isEdit: Boolean,
        onDismissAction: TrialSessionInput = TrialSessionInput.HideBottomSheet,
    ) {
        logger.d { "Showing song entry for index $index" }
        songEntryViewModel.value = SongEntryViewModel(
            session = inProgressSession,
            targetRank = targetRank.value,
            index = index,
            shortcut = shortcut,
            isEdit = isEdit,
        )
        _bottomSheetState.value = UITrialBottomSheet.DetailsPlaceholder(onDismissAction)
    }

    private fun hideSongEntry() {
        songEntryViewModel.value?.commitChanges()?.let { inProgressSessionFlow.value = it }
        updateTargetRank()
        songEntryViewModel.value = null
        viewModelScope.launch {
            _events.emit(TrialSessionEvent.HideBottomSheet)
            delay(500)
            _bottomSheetState.value = null
            logger.d { "Song entry hidden" }
        }
    }

    private fun updateTargetRank(allowIncrease: Boolean = false) {
        var currIdx = if (allowIncrease) {
            (trial.goals?.size ?: return) - 1
        } else {
            (trial.goals ?: return).map { it.rank }.indexOf(targetRank.value)
        }
        fun currRank() = trial.goals[currIdx].rank

        while (inProgressSession.isRankSatisfied(currRank()) == false) {
            currIdx--
        }
        logger.d { "Rank changing from ${targetRank.value} to ${currRank()}" }
        targetRank.value = currRank()
    }
}

typealias SubmitFieldsItem = Pair<String, String>
