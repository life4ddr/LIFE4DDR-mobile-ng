package com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState

data class UILaunchScreen(
    val requireSignin: Boolean,
    val initState: InitState?
)
