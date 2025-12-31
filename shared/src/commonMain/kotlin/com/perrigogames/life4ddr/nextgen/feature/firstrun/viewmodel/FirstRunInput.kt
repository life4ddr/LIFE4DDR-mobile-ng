package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

import com.perrigogames.life4ddr.nextgen.enums.GameVersion
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState

sealed class FirstRunInput {

    data class NewUserSelected(val isNewUser: Boolean) : FirstRunInput()

    data class UsernameUpdated(val name: String) : FirstRunInput()

    data class RivalCodeUpdated(val rivalCode: String) : FirstRunInput()

    data class GameVersionUpdated(val version: GameVersion) : FirstRunInput()

    data object NavigateBack : FirstRunInput()

    data object NavigateNext : FirstRunInput()

    data class RankMathodSelected(val method: InitState) : FirstRunInput()
}