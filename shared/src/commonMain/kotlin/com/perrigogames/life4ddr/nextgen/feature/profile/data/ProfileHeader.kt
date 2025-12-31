package com.perrigogames.life4ddr.nextgen.feature.profile.data

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileInput
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class ProfileHeader(
    val title: StringDesc,
    val subtitle: StringDesc,
    val buttonText: StringDesc,
    val buttonAction: PlayerProfileInput,
) {

    companion object {
        val sanbaiReminder = ProfileHeader(
            title = MR.strings.profile_sanbai_reminder_title.desc(),
            subtitle = MR.strings.profile_sanbai_reminder_subtitle.desc(),
            buttonText = MR.strings.profile_sanbai_reminder_cta.desc(),
            buttonAction = PlayerProfileInput.SanbaiReminderClicked,
        )
    }
}
