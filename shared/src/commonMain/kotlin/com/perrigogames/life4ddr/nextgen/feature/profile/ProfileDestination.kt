package com.perrigogames.life4ddr.nextgen.feature.profile

import com.perrigogames.life4ddr.nextgen.util.Destination
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.serialization.Serializable

@Serializable
sealed class ProfileDestination(override val baseRoute: String, override val title: String) : Destination {

    @Serializable
    data object Profile : ProfileDestination("profile", "Profile")

    @Serializable
    data class Scores(val expandFAB: Boolean = false) : ProfileDestination("scores", "Scores")

    @Serializable
    data object Trials : ProfileDestination("trials", "Trials")

    @Serializable
    data object Settings : ProfileDestination("settings", "Settings")
}