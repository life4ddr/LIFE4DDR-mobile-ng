package com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertManager
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertType
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.settings.SettingsDestination
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsPageProvider
import com.perrigogames.life4ddr.nextgen.feature.settings.view.SettingsPage
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.util.ExternalActions
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalSettingsApi::class, ExperimentalCoroutinesApi::class)
class SettingsViewModel(
    private val appInfo: AppInfo,
    private val resultsManager: SongResultsManager,
    private val sanbaiManager: SanbaiManager,
    private val songDataManager: SongDataManager,
    private val settingsPageProvider: SettingsPageProvider,
    private val trialRecordsManager: TrialRecordsManager,
    private val flowSettings: FlowSettings,
    private val userInfoSettings: UserInfoSettings,
    private val ladderSettings: LadderSettings,
    private val externalActions: ExternalActions,
    private val alertManager: AlertManager,
) : ViewModel(), KoinComponent {

    private val pageStackState = MutableStateFlow(listOf(SettingsPage.ROOT))
    private val pageFlow = pageStackState.map { it.last() }

    val state: StateFlow<UISettingsData?> = pageFlow.flatMapLatest { createPage(it) }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = null)
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    fun handleAction(action: SettingsAction) {
        when (action) {
            SettingsAction.None -> {}
            is SettingsAction.Navigate -> pushPage(action.page)
            is SettingsAction.NavigateBack -> {
                if (pageStackState.value.size > 1) {
                    popPage()
                } else {
                    viewModelScope.launch {
                        _events.emit(SettingsEvent.Close)
                    }
                }
            }
            is SettingsAction.SetBoolean -> {
                viewModelScope.launch {
                    flowSettings.putBoolean(action.id, action.newValue)
                }
            }
            is SettingsAction.SetString -> {
                viewModelScope.launch {
                    flowSettings.putString(action.id, action.newValue)
                }
            }
            is SettingsAction.SetGameVersion -> {
                viewModelScope.launch {
                    ladderSettings.setSelectedGameVersion(action.newValue)
                }
            }
            is SettingsAction.Email -> {
                externalActions.openEmail(action.email)
            }
            is SettingsAction.WebLink -> {
                externalActions.openWeblink(action.url)
            }
            is SettingsAction.ShowCredits -> viewModelScope.launch {
                _events.emit(SettingsEvent.Navigate(SettingsDestination.Credits))
            }
            is SettingsAction.Sanbai.RefreshLibrary -> songDataManager.refreshSanbaiData(force = true)
            is SettingsAction.Sanbai.RefreshUserScores -> {
                viewModelScope.launch {
                    sanbaiManager.fetchScores()
                }
            }
            is SettingsAction.Debug.SongLockPage -> viewModelScope.launch {
                _events.emit(SettingsEvent.Navigate(SettingsDestination.SongLock))
            }
            SettingsAction.Debug.Life4FlareAlert -> alertManager.sendAlert(AlertType.LIFE4FlarePromo(force = true))
            SettingsAction.ClearData.Results -> {
                resultsManager.clearAllResults()
            }
            SettingsAction.ClearData.Trials -> {
                trialRecordsManager.clearSessions()
            }
            SettingsAction.ClearData.All -> {
                resultsManager.clearAllResults()
                trialRecordsManager.clearSessions()
                // TODO clear webview state
                viewModelScope.launch {
                    flowSettings.clear()
                    _events.emit(SettingsEvent.Navigate(FirstRunDestination.FirstRun))
                }
            }
        }
    }

    private fun pushPage(page: SettingsPage) {
        pageStackState.value += page
    }

    private fun popPage() {
        pageStackState.value = pageStackState.value.dropLast(1)
    }

    private fun createPage(page: SettingsPage): Flow<UISettingsData> {
        return when (page) {
            SettingsPage.ROOT -> settingsPageProvider.getRootPage(isDebug = appInfo.isDebug)
            SettingsPage.EDIT_USER_INFO -> settingsPageProvider.getEditUserPage()
            SettingsPage.SONG_LIST_SETTINGS -> settingsPageProvider.getSongListPage()
            SettingsPage.TRIAL_SETTINGS -> settingsPageProvider.getTrialPage()
            SettingsPage.SANBAI_SETTINGS -> settingsPageProvider.getSanbaiPage()
            SettingsPage.CLEAR_DATA -> settingsPageProvider.getClearDataPage()
            SettingsPage.DEBUG -> settingsPageProvider.getDebugPage()
        }
    }
}