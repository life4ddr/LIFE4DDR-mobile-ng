package com.perrigogames.life4ddr.nextgen.feature.trials.manager

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialListSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_NEW
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialListSettings.Companion.KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface TrialListSettings {

    val highlightNewFlow: StateFlow<Boolean>
    fun setHighlightNew(enabled: Boolean)

    val highlightUnplayedFlow: StateFlow<Boolean>
    fun setHighlightUnplayed(enabled: Boolean)

    companion object {
        const val KEY_TRIAL_LIST_HIGHLIGHT_NEW = "KEY_TRIAL_LIST_HIGHLIGHT_NEW"
        const val KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED = "KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED"
    }
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultTrialListSettings : SettingsManager(), TrialListSettings {
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
}
