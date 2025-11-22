package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

sealed class TrialSessionEvent {
    data object Close : TrialSessionEvent()
    data object HideBottomSheet : TrialSessionEvent()
}
