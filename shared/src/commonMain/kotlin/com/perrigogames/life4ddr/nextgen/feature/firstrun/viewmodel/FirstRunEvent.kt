package com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel

sealed class FirstRunEvent {

    data object Close : FirstRunEvent()
}