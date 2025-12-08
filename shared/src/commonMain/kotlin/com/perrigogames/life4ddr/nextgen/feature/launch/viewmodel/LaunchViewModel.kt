package com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class LaunchViewModel(
    private val firstRunSettingsManager: FirstRunSettings
) : ViewModel(), KoinComponent {

    private val _launchState = MutableSharedFlow<InitState?>()
    val launchState: SharedFlow<InitState?> = _launchState.asSharedFlow()

    init {
        viewModelScope.launch {
            firstRunSettingsManager.initState.collect(_launchState)
        }
    }
}
