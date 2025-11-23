package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import dev.icerock.moko.mvvm.flow.CFlow
import dev.icerock.moko.mvvm.flow.cFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LaunchViewModel : ViewModel(), KoinComponent {

    private val firstRunSettingsManager: FirstRunSettings by inject()

    private val _launchState = MutableSharedFlow<InitState?>()
    val launchState: CFlow<InitState?> = _launchState.cFlow()

    init {
        viewModelScope.launch {
            firstRunSettingsManager.initState.collect(_launchState)
        }
    }
}

data class UILaunchScreen(
    val requireSignin: Boolean,
    val initState: InitState?
)