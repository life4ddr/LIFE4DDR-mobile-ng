package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialState
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialGoalStrings
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UIEXScoreBar
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITargetRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialBottomSheet
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.SatisfiedResult
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType
import com.perrigogames.life4ddr.nextgen.feature.trialsession.manager.TrialContentProvider
import com.perrigogames.life4ddr.nextgen.util.ViewState
import com.perrigogames.life4ddr.nextgen.util.toViewState
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.asImageDesc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class TrialSessionViewModel(
    trialId: String,
    private val userRankSettings: UserRankSettings,
    private val trialDataManager: TrialDataManager,
    private val trialRecordsManager: TrialRecordsManager,
    private val trialSettings: TrialSettings,
    private val logger: Logger
) : ViewModel(), KoinComponent {

    private val trial: Course.Trial =
        trialDataManager.trialsFlow.value
            .filterIsInstance<Course.Trial>()
            .firstOrNull { it.id == trialId }
            ?: throw IllegalStateException("Can't find trial with id $trialId")

    private val bestSession = trialRecordsManager.bestSessions.value
        .firstOrNull { it.trialId == trialId }

    private val contentProvider = TrialContentProvider(trial = trial)

    private val targetRank = MutableStateFlow(TrialRank.BRONZE)
    private val maxRank = MutableStateFlow(trial.goals.maxOf { it.rank })
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
    val uiExScoreFlow: StateFlow<UIEXScoreBar> =
        combine(
            stage,
            trialRecordsManager.bestSessions.map { sessions ->
                sessions.firstOrNull { it.trialId == trialId }
            },
            inProgressSessionFlow,
            trialSettings.showExLost,
        ) { stage, bestSession, inProgressSession, showExLost ->
            val started = stage != null
            val currentEx = if (started) {
                inProgressSession.results.sumOf { it?.exScore ?: 0 }
            } else {
                bestSession?.exScore?.toInt() ?: 0
            }
            val currentMaxEx = if (started) {
                inProgressSession.trial.songs.subList(0, stage).sumOf { it.ex }
            } else {
                trial.totalEx
            }
            UIEXScoreBar(
                trial = trial,
                currentEx = currentEx,
                currentExText = StringDesc.Raw(
                    if (showExLost) {
                        (currentEx - currentMaxEx).toString()
                    } else {
                        currentEx.toString()
                    }
                ),
                hintCurrentEx = currentMaxEx,
                exTextClickAction = TrialSessionInput.ToggleExLost(!showExLost),
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = UIEXScoreBar(trial = trial)
            )

    private val songEntryViewModel = MutableStateFlow<SongEntryViewModel?>(null)

    init {
        viewModelScope.launch {
            targetRank.collect { target ->
                val current = (_state.value as? ViewState.Success)?.data ?: return@collect
                if (current.targetRank.rank != target) {
                    _state.value = current.copy(
                        targetRank = current.targetRank.copy(
                            rank = target,
                            title = target.nameRes.desc(),
                            titleColor = target.colorRes,
                            rankGoalItems = TrialGoalStrings.generateGoalStrings(trial.goalSet(target)!!, trial),
                        ),
                    ).toViewState()
                }
            }
        }

        viewModelScope.launch {
            combine(
                inProgressSessionFlow.filterNotNull(),
                stage.filterNotNull(),
                targetRank
            ) { session, stage, _ ->
                logger.d { "Creating stage $stage" }
                val complete = stage >= 4
                val current = (_state.value as? ViewState.Success)?.data ?: return@combine
                val currentTarget = current.targetRank
                val targetRank = currentTarget.copy(
                    state = if (complete) {
                        UITargetRank.State.ACHIEVED
                    } else {
                        UITargetRank.State.IN_PROGRESS
                    },
                    availableRanks = if (currentTarget.state == UITargetRank.State.ACHIEVED) {
                        null
                    } else {
                        currentTarget.availableRanks
                    }
                )
                _state.value = if (complete) {
                    current.copy(
                        targetRank = targetRank,
                        content = contentProvider.provideFinalScreen(session),
                        footer = when {
                            session.finalPhotoUriString == null -> {
                                UITrialSession.Footer.Button(
                                    buttonText = MR.strings.take_results_photo.desc(),
                                    buttonAction = TrialSessionInput.TakeResultsPhoto,
                                )
                            }
                            session.isAllInfoPresent(targetRank.rank) -> {
                                UITrialSession.Footer.Message(
                                    "Some results need fixing".desc()
                                )
                            }
                            else -> {
                                UITrialSession.Footer.Button(
                                    buttonText = MR.strings.submit.desc(),
                                    buttonAction = TrialSessionInput.Finished,
                                )
                            }
                        },
                    )
                } else {
                    current.copy(
                        targetRank = targetRank,
                        content = contentProvider.provideMidSession(session, stage, targetRank.rank),
                        footer = UITrialSession.Footer.Button(
                            buttonText = MR.strings.take_photo.desc(),
                            buttonAction = TrialSessionInput.TakePhoto(stage),
                        ),
                    )
                }.toViewState()
            }.collect()
        }

        val rank = getStartingRank(
            playerRank = userRankSettings.rank.value,
            bestRank = bestSession?.goalRank,
            allowedRanks = trial.availableRanks
        )
        _state.value = ViewState.Success(
            UITrialSession(
                trialTitle = trial.name.desc(),
                trialLevel = StringDesc.ResourceFormatted(
                    MR.strings.level_short_format,
                    trial.difficulty
                ),
                backgroundImage = trial.coverResource ?: MR.images.trial_default.asImageDesc(),
                targetRank = UITargetRank(
                    rank = rank,
                    title = rank.nameRes.desc(),
                    titleColor = rank.colorRes,
                    availableRanks = trial.availableRanks,
                    rankGoalItems = TrialGoalStrings.generateGoalStrings(trial.goalSet(rank)!!, trial),
                    state = UITargetRank.State.SELECTION,
                ),
                content = contentProvider.provideSummary(),
                footer = when {
                    trial.state == TrialState.RETIRED -> {
                        UITrialSession.Footer.Message(MR.strings.trial_warning_message_retired.desc())
                    }
                    else -> UITrialSession.Footer.Button(
                        buttonText = MR.strings.placement_start.desc(),
                        buttonAction = TrialSessionInput.StartTrial(fromDialog = false),
                    )
                },
            )
        )
        targetRank.value = rank
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
                    _bottomSheetState.value = null
                }
            }

            TrialSessionInput.Finished -> {
                viewModelScope.launch {
                    inProgressSession.goalObtained = true // FIXME
                    trialRecordsManager.saveSession(inProgressSession, targetRank.value)
                    _events.emit(TrialSessionEvent.SubmitAndClose(inProgressSession))
                }
            }

            TrialSessionInput.HideBottomSheet -> {
                hideSongEntry()
            }

            is TrialSessionInput.EditItem -> {
                inProgressSession.results.getOrNull(action.index)?.let { result ->
                    showSongEntry(
                        index = action.index,
                        isEdit = true,
                        shortcut = result.shortcut,
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

            is TrialSessionInput.ToggleExLost -> {
                trialSettings.setShowExLost(action.enabled)
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
        val newRank = findTargetRank(allowIncrease) ?: return
        logger.d { "Rank changing from ${targetRank.value} to $newRank" }
        targetRank.value = newRank
    }

    private fun findTargetRank(allowIncrease: Boolean = false): TrialRank {
        var currIdx = when {
            allowIncrease -> trial.availableRanks.size - 1
            else -> trial.availableRanks.indexOf(targetRank.value)
        }
        fun currRank() = trial.availableRanks[currIdx]

        while (inProgressSession.isRankSatisfied(currRank()) == SatisfiedResult.UNSATISFIED && currIdx > 0) {
            currIdx--
        }
        return trial.availableRanks[currIdx]
    }
}
