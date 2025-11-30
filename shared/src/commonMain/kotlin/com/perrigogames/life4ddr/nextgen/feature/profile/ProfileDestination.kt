package com.perrigogames.life4ddr.nextgen.feature.profile

import com.perrigogames.life4ddr.nextgen.util.Destination
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileDestination(override val baseRoute: String, override val title: StringDesc) : Destination {
    @Serializable data object Profile : ProfileDestination("profile", "Profile".desc())
    @Serializable data object Scores : ProfileDestination("scores", "Scores".desc())
    @Serializable data object Trials : ProfileDestination("trials", "Trials".desc())
    @Serializable data object Settings : ProfileDestination("settings", "Settings".desc())
}