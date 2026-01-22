package com.perrigogames.life4ddr.nextgen.feature.trials.manager

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings.Companion.KEY_TRIAL_DETAILS_SHOW_EX_LOST
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_NEW
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface TrialSettings {

    val highlightNewFlow: StateFlow<Boolean>
    fun setHighlightNew(enabled: Boolean)

    val highlightUnplayedFlow: StateFlow<Boolean>
    fun setHighlightUnplayed(enabled: Boolean)

    val showExLost: StateFlow<Boolean>
    fun setShowExLost(enabled: Boolean)

    companion object {
        const val KEY_TRIAL_LIST_HIGHLIGHT_NEW = "KEY_TRIAL_LIST_HIGHLIGHT_NEW"
        const val KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED = "KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED"
        const val KEY_TRIAL_DETAILS_SHOW_EX_LOST = "KEY_TRIAL_DETAILS_SHOW_EX_LOST"
    }
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultTrialSettings : SettingsManager(), TrialSettings {
    override val highlightNewFlow: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_TRIAL_LIST_HIGHLIGHT_NEW, true)
            .stateIn(mainScope, SharingStarted.Eagerly, true)

    override fun setHighlightNew(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_TRIAL_LIST_HIGHLIGHT_NEW, enabled)
        }
    }

    override val highlightUnplayedFlow: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED, true)
            .stateIn(mainScope, SharingStarted.Eagerly, true)

    override fun setHighlightUnplayed(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED, enabled)
        }
    }

    override val showExLost: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_TRIAL_DETAILS_SHOW_EX_LOST, false)
            .stateIn(mainScope, SharingStarted.Eagerly, false)

    override fun setShowExLost(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_TRIAL_DETAILS_SHOW_EX_LOST, enabled)
        }       
    }
}
