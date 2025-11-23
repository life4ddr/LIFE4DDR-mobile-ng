package com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderDataManager
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdManager
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VersionsDialogViewModel : ViewModel(), KoinComponent {
    private val ladderDataManager: LadderDataManager by inject()
    private val motdManager: MotdManager by inject()
    private val songDataManager: SongDataManager by inject()
    private val trialDataManager: TrialDataManager by inject()

    private val _state = MutableStateFlow(VersionsDialogState()).cMutableStateFlow()
    val state: StateFlow<VersionsDialogState> = _state

    init {
        viewModelScope.launch {
            combine(
                ladderDataManager.dataVersionString,
                motdManager.dataVersionString,
                songDataManager.dataVersionString,
                trialDataManager.dataVersionString,
            ) { ladderVersion, motdVersion, songDataVersion, trialVersion ->
                _state.value = VersionsDialogState(
                    ladderDataVersion = ladderVersion,
                    motdVersion = motdVersion,
                    songListVersion = songDataVersion,
                    trialDataVersion = trialVersion
                )
            } // TODO use this
        }
    }
}

data class VersionsDialogState(
    val appVersion: String = "",
    val ladderDataVersion: String = "",
    val motdVersion: String = "",
    val songListVersion: String = "",
    val trialDataVersion: String = ""
)
