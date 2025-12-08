package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

import com.perrigogames.life4ddr.nextgen.MR
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

sealed class FirstRunError {

    abstract val errorText: StringDesc

    class UsernameError(
        override val errorText: StringDesc = MR.strings.first_run_error_username.desc()
    ) : FirstRunError()

    class PasswordError(
        override val errorText: StringDesc = MR.strings.first_run_error_password.desc()
    ) : FirstRunError()

    class RivalCodeError(
        override val errorText: StringDesc = MR.strings.first_run_error_rival_code.desc()
    ) : FirstRunError()
}
