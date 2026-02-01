package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import dev.icerock.moko.resources.desc.StringDesc

sealed class TrialSessionEvent {
    data object Close : TrialSessionEvent()
    data class SubmitAndClose(
        val session: InProgressTrialSession
    ) : TrialSessionEvent()
    data object HideBottomSheet : TrialSessionEvent()
    data class ShowWarningDialog(
        val title: StringDesc,
        val body: StringDesc,
        val ctaCancelText: StringDesc,
        val ctaConfirmText: StringDesc,
        val ctaConfirmInput: TrialSessionInput
    ) : TrialSessionEvent()
}
