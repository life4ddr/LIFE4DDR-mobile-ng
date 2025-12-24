package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceStringDesc
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

sealed class FirstRunStep(
    val showNextButton: Boolean = true
) {
    data object Landing : FirstRunStep(showNextButton = false)

    sealed class PathStep(
        showNextButton: Boolean = true,
    ) : FirstRunStep(showNextButton) {

        abstract val path: FirstRunPath

        data class Username(
            override val path: FirstRunPath,
            val username: String = "",
            val usernameError: FirstRunError.UsernameError? = null,
        ) : PathStep() {

            val headerText: ResourceStringDesc = when (path.isNewUser) {
                true -> MR.strings.first_run_username_new_header
                false -> MR.strings.first_run_username_existing_header
            }.desc()

            val descriptionText: ResourceStringDesc? = when (path.isNewUser) {
                true -> StringDesc.Resource(MR.strings.first_run_username_description)
                else -> null
            }
        }

        data class Password(override val path: FirstRunPath) : PathStep()

        data class UsernamePassword(override val path: FirstRunPath) : PathStep()

        data class RivalCode(
            override val path: FirstRunPath,
            val rivalCode: String = "",
            val rivalCodeError: FirstRunError.RivalCodeError? = null,
        ) : PathStep()

        data class SocialHandles(override val path: FirstRunPath) : PathStep()

        data class InitialRankSelection(
            override val path: FirstRunPath,
            val availableMethods: List<InitState> = path.allowedRankSelectionTypes(),
        ) : PathStep(showNextButton = false)

        data class Completed(
            override val path: FirstRunPath,
            val initStep: InitState,
        ) : PathStep(showNextButton = false)
    }
}
