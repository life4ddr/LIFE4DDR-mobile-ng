package com.perrigogames.life4ddr.nextgen.feature.notifications.alert.view

import com.perrigogames.life4ddr.nextgen.MR
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.ImageDesc

data class UIAlert(
    val title: StringDesc,
    val text: StringDesc,
    val image: ImageDesc? = null,
    val canHide: Boolean = false,
    val hideCheckboxText: StringDesc? = MR.strings.dont_show_again.desc().takeIf { canHide },
    val ctaConfirmText: StringDesc = MR.strings.okay.desc(),
    // TODO figure out generic actions later, for now just close the alert
//    val ctaConfirmAction: T,
//    val ctaCancelText: StringDesc? = null,
//    val ctaCancelAction: T? = null,
)
