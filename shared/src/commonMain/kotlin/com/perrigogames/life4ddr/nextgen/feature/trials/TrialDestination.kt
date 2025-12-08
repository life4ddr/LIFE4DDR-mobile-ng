package com.perrigogames.life4ddr.nextgen.feature.trials

import com.perrigogames.life4ddr.nextgen.util.Destination
import kotlinx.serialization.Serializable

/**
 * Sealed class representing different destinations within the Trials feature.
 */
@Serializable
sealed class TrialDestination(override val baseRoute: String) : Destination {

    /**
     * Destination that shows the details for a specific Trial, as well as let
     * you play it.
     * @property trial The Trial to show details for
     */
    @Serializable
    data class TrialDetails(val trialId: String) : TrialDestination(BASE_ROUTE) {
        override val route = baseRoute.replace("{trialId}", trialId)
        companion object {
            const val BASE_ROUTE = "trial/details/{trialId}"
        }
    }

    /**
     * Destination that shows a list of all the Trials this user has played.
     */
    @Serializable
    data object TrialRecords : TrialDestination("trial/records")
}