package com.perrigogames.life4ddr.nextgen.feature.ladder.manager

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.russhwolf.settings.ExperimentalSettingsApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface MASettings {
    val combineMFCs: StateFlow<Boolean>
    val combineSDPs: StateFlow<Boolean>
    val maConfig: StateFlow<MAConfig>
    fun setCombineMFCs(enabled: Boolean)
    fun setCombineSDPs(enabled: Boolean)
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultMASettings : SettingsManager(), MASettings {

    override val combineMFCs: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_COMBINE_MFCS_GOALLIST, false)
            .stateIn(mainScope, SharingStarted.Lazily, false)

    override val combineSDPs: StateFlow<Boolean> =
        settings.getBooleanFlow(KEY_COMBINE_SDPS_GOALLIST, false)
            .stateIn(mainScope, SharingStarted.Lazily, false)

    override val maConfig: StateFlow<MAConfig> =
        combine(
            combineMFCs,
            combineSDPs
        ) { combineMFCs, combineSDPs -> MAConfig(combineMFCs, combineSDPs) }
            .stateIn(mainScope, SharingStarted.Lazily, initialValue = MAConfig())

    override fun setCombineMFCs(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_COMBINE_MFCS_GOALLIST, enabled)
        }
    }

    override fun setCombineSDPs(enabled: Boolean) {
        mainScope.launch {
            settings.putBoolean(KEY_COMBINE_SDPS_GOALLIST, enabled)
        }
    }

    companion object {
        const val KEY_COMBINE_MFCS_GOALLIST = "combine_mfcs_goallist"
        const val KEY_COMBINE_SDPS_GOALLIST = "combine_sdps_goallist"
    }
}

data class MAConfig(
    val combineMFCs: Boolean = false,
    val combineSDPs: Boolean = false,
)
