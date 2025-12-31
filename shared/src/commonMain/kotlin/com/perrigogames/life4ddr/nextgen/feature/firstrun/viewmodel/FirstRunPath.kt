package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState.DONE
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.Completed
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.InitialRankSelection
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.Password
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.RivalCode
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.SelectGameVersion
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.Username
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.UsernamePassword
import kotlin.reflect.KClass

enum class FirstRunPath(
    val isNewUser: Boolean,
    vararg val steps: KClass<out FirstRunStep>
) {
    NEW_USER_LOCAL (isNewUser = true, Username::class, RivalCode::class, SelectGameVersion::class, InitialRankSelection::class, Completed::class),
    NEW_USER_REMOTE (isNewUser = true, Username::class, Password::class, RivalCode::class, SelectGameVersion::class, InitialRankSelection::class),
    EXISTING_USER_LOCAL (isNewUser = false, Username::class, RivalCode::class, SelectGameVersion::class, InitialRankSelection::class, Completed::class),
    EXISTING_USER_REMOTE (isNewUser = false, UsernamePassword::class, SelectGameVersion::class, Completed::class),
    ;

    fun allowedRankSelectionTypes(): List<InitState> = when (this) {
        NEW_USER_LOCAL -> listOf(DONE, InitState.PLACEMENTS, InitState.RANKS)
        NEW_USER_REMOTE -> listOf(DONE, InitState.PLACEMENTS)
        EXISTING_USER_LOCAL -> listOf(DONE, InitState.RANKS)
        EXISTING_USER_REMOTE -> listOf()
    }
}
