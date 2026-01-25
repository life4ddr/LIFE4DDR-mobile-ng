package com.perrigogames.life4ddr.nextgen.feature.trials.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.db.SelectFullSessions
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialScraper
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialScraperResult
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialState
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialJacketCorner
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_NEW
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UIPlacementBanner
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialList
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialScrapeProgress
import com.perrigogames.life4ddr.nextgen.feature.trials.view.toUIJacket
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TrialListViewModel(
    private val trialDataManager: TrialDataManager,
    private val userRankSettings: UserRankSettings,
    private val trialRecordsManager: TrialRecordsManager,
    private val trialScraper: TrialScraper,
    private val trialSettings: TrialSettings,
    private val settings: Settings,
) : ViewModel(), KoinComponent {

    private val trialsStateFlow = trialDataManager.trialsFlow
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = emptyList())

    private val _state = MutableStateFlow(UITrialList())
    val state: StateFlow<UITrialList> = _state.asStateFlow()

    private val _scrapeState = MutableStateFlow<UITrialScrapeProgress?>(null)
    val scrapeState: StateFlow<UITrialScrapeProgress?> = _scrapeState.asStateFlow()

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

    fun scrapeTrialData() {
        val lastSyncTime = trialSettings.lastSyncTime ?: Instant.DISTANT_PAST
        val timeSinceLastSync = Clock.System.now() - lastSyncTime
        viewModelScope.launch {
            if (timeSinceLastSync < SCRAPE_COOLDOWN_DURATION) {
                showTrialCooldownError(
                    time = (lastSyncTime + SCRAPE_COOLDOWN_DURATION)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                )
            } else {
                trialScraper
                    .scrapeTrialData()
                    .collect { result ->
                        when (result) {
                            is TrialScraperResult.StartingProfile -> {
                                updateTrialScrapeProfile(result.username)
                            }
                            is TrialScraperResult.ProfileError -> {
                                updateTrialScrapeProfileError(result.username)
                            }
                            is TrialScraperResult.NoTrialsFound -> {
                                updateTrialScrapeNoTrials(result.username)
                            }
                            is TrialScraperResult.ProfileFound.Progress -> {
                                updateTrialScrapeProgress(result.position, result.total, result.hits, result.trial)
                            }
                            is TrialScraperResult.ProfileFound.Success -> {
                                addTrialPlay(
                                    trial = result.trial,
                                    targetRank = result.rank,
                                    exScore = result.exScore,
                                )
                            }
                            is TrialScraperResult.ProfileFound.Finished -> {
                                trialSettings.setLastSyncTime(Clock.System.now())
                                showTrialScrapeFinished(result.hits)
                            }
                        }
                    }
            }
        }
    }

    private fun updateTrialScrapeProfile(
        username: String
    ) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = MR.strings.scrape_profile_start_top.desc(),
            bottomText = ResourceFormattedStringDesc(MR.strings.scrape_profile_start_bottom_format, listOf(username)),
        )
    }

    private suspend fun updateTrialScrapeProfileError(
        username: String
    ) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = MR.strings.scrape_profile_start_top.desc(),
            bottomText = ResourceFormattedStringDesc(MR.strings.scrape_profile_error_bottom_format, listOf(username)),
        )
        delay(SCRAPE_FINISH_HIDE)
        _scrapeState.value = null
    }

    private suspend fun updateTrialScrapeNoTrials(
        username: String
    ) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = MR.strings.scrape_profile_start_top.desc(),
            bottomText = ResourceFormattedStringDesc(MR.strings.scrape_profile_no_trials_bottom_format, listOf(username)),
        )
        delay(SCRAPE_FINISH_HIDE)
        _scrapeState.value = null
    }

    private fun updateTrialScrapeProgress(
        position: Int,
        total: Int,
        hits: Int,
        trial: Trial,
    ) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = ResourceFormattedStringDesc(MR.strings.scrape_progress_format, listOf(position, total, trial.name)),
            bottomText = ResourceFormattedStringDesc(MR.strings.scrape_hits_format, listOf(hits)),
            progress = position / total.toFloat()
        )
    }

    private suspend fun showTrialScrapeFinished(hits: Int) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = MR.strings.scrape_complete.desc(),
            bottomText = ResourceFormattedStringDesc(MR.strings.scrape_hits_format, listOf(hits)),
            progress = 1.0f
        )
        delay(SCRAPE_FINISH_HIDE)
        _scrapeState.value = null
    }

    private suspend fun showTrialCooldownError(time: LocalDateTime) {
        _scrapeState.value = UITrialScrapeProgress(
            topText = MR.strings.scrape_cooldown_title.desc(),
            bottomText = ResourceFormattedStringDesc(
                MR.strings.scrape_cooldown_description_format,
                listOf(time.format(DATETIME_FORMAT))
            ),
            progress = null
        )
        delay(SCRAPE_FINISH_HIDE)
        _scrapeState.value = null
    }

    private fun matchTrials(trials: List<Trial>, sessions: List<SelectFullSessions>) = trials.associateWith { trial ->
        sessions.firstOrNull { it.trialId == trial.id }
    }

    private fun createDisplayTrials(
        trials: List<Trial>,
        sessions: List<SelectFullSessions>,
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

    companion object {
        val SCRAPE_COOLDOWN_DURATION = 12.hours
        val SCRAPE_FINISH_HIDE = 3000.milliseconds

        @OptIn(FormatStringsInDatetimeFormats::class)
        private val DATETIME_FORMAT = LocalDateTime.Format {
            year()
            char('/')
            monthNumber()
            char('/')
            day()
            char(' ')
            amPmHour()
            char(':')
            minute()
            char(' ')
            amPmMarker("AM", "PM")
        }
    }
}