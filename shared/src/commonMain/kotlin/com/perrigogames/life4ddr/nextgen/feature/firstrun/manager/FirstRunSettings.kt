package com.perrigogames.life4ddr.nextgen.feature.firstrun.manager

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings.Companion.KEY_INIT_STATE
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceStringDesc
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface FirstRunSettings {
    val initState: Flow<InitState?>
    val requireSignin: Flow<Boolean>

    fun setInitState(state: InitState?)

    companion object {
        const val KEY_INIT_STATE = "KEY_INIT_STATE"
    }
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultFirstRunSettings : SettingsManager(), FirstRunSettings {

    override val initState: Flow<InitState?> = settings.getStringOrNullFlow(KEY_INIT_STATE)
        .map { InitState.parse(it) }

    override val requireSignin: Flow<Boolean> = initState.map { it == null }

    override fun setInitState(state: InitState?) {
        mainScope.launch {
            state?.also {
                settings.putString(KEY_INIT_STATE, it.key)
            } ?: settings.remove(KEY_INIT_STATE)
        }
    }
}

enum class InitState(val key: String, val description: ResourceStringDesc) {
    PLACEMENTS("placements", description = StringDesc.Resource(MR.strings.first_run_rank_method_placement)),
    RANKS("ranks", description = StringDesc.Resource(MR.strings.first_run_rank_method_selection)),
    DONE("done", description = StringDesc.Resource(MR.strings.first_run_rank_method_no_rank)),
    ;

    companion object {
        fun parse(key: String?): InitState? = entries.firstOrNull { it.key == key }
    }
}