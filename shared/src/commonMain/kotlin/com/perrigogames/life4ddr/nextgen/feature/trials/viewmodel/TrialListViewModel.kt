package com.perrigogames.life4ddr.nextgen.feature.trials.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.db.SelectBestSessions
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialState
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialJacketCorner
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialListSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_NEW
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialListSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UIPlacementBanner
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialList
import com.perrigogames.life4ddr.nextgen.feature.trials.view.toUIJacket
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class TrialListViewModel(
    private val trialDataManager: TrialDataManager,
    private val userRankSettings: UserRankSettings,
    private val trialRecordsManager: TrialRecordsManager,
    private val settings: Settings,
) : ViewModel(), KoinComponent {

    private val trialsStateFlow = trialDataManager.trialsFlow
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    private val _state = MutableStateFlow(UITrialList())
    val state: StateFlow<UITrialList> = _state.asStateFlow()

    init {
        val highlightNew = settings.getBoolean(KEY_TRIAL_LIST_HIGHLIGHT_NEW, true)
        val highlightUnplayed = settings.getBoolean(KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED, false)

        viewModelScope.launch {
            combine(
                trialsStateFlow,
                trialRecordsManager.bestSessions,
                userRankSettings.rank,
            ) { trials, sessions, rank ->
                UITrialList(
                    placementBanner = if (rank == null) {
                        UIPlacementBanner()
                    } else {
                        null
                    },
                    trials = createDisplayTrials(
                        trials = trials,
                        sessions = sessions,
                        featureNew = highlightNew,
                        featureUnplayed = highlightUnplayed,
                    ),
                )
            }.collect(_state)
        }
    }

    fun addTrialPlay(trial: Trial, targetRank: TrialRank, exScore: Int) {
        trialRecordsManager.saveFakeSession(
            trial = trial,
            targetRank = targetRank,
            exScore = exScore
        )
    }

    fun clearTrialData(trialId: String) {
        trialRecordsManager.deleteSessions(trialId)
    }

    private fun matchTrials(trials: List<Trial>, sessions: List<SelectBestSessions>) = trials.associateWith { trial ->
        sessions.firstOrNull { it.trialId == trial.id }
    }

    private fun createDisplayTrials(
        trials: List<Trial>,
        sessions: List<SelectBestSessions>,
        featureNew: Boolean,
        featureUnplayed: Boolean,
    ): List<UITrialList.Item> {
        val matchedTrials = matchTrials(trials, sessions)

        val retired = mutableListOf<UITrialList.Item.Trial>()
        val event = mutableListOf<UITrialList.Item.Trial>()
        val new = mutableListOf<UITrialList.Item.Trial>()
        val unplayed = mutableListOf<UITrialList.Item.Trial>()
        val active = mutableListOf<UITrialList.Item.Trial>()

        trials.map { it.toUIJacket(bestSession = matchedTrials[it]) }
            .forEach { item ->
                when {
                    item.trial.state == TrialState.RETIRED -> retired
                    item.cornerType == TrialJacketCorner.EVENT -> event
                    featureNew && item.cornerType == TrialJacketCorner.NEW -> new
                    featureUnplayed && item.session == null -> unplayed
                    else -> active
                }.add(UITrialList.Item.Trial(item))
            }

        return mutableListOf<UITrialList.Item>(
            UITrialList.Item.Header(MR.strings.active_trials.desc())
        ).apply {
            addAll(event)
            addAll(new)
            addAll(unplayed)
            addAll(active)
            add(UITrialList.Item.Header(MR.strings.retired_trials.desc()))
            addAll(retired)
        }
    }
}