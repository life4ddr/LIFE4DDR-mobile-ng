package com.perrigogames.life4ddr.nextgen.feature.firstrun

import com.perrigogames.life4ddr.nextgen.util.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class FirstRunDestination(override val baseRoute: String) : Destination {

    @Serializable
    data object Landing : FirstRunDestination("landing")

    @Serializable
    data object FirstRun : FirstRunDestination("first_run")

    @Serializable
    data object PlacementList : FirstRunDestination("placement_list")

    @Serializable
    data class PlacementDetails(val placementId: String) : FirstRunDestination(BASE_ROUTE) {
        override val route = baseRoute.replace("{$PLACEMENT_ID}", placementId)
        companion object {
            const val PLACEMENT_ID = "placement_id"
            const val BASE_ROUTE = "placement_details/{$PLACEMENT_ID}"
        }
    }

    @Serializable
    data object InitialRankList : FirstRunDestination("initial_rank_list")

    @Serializable
    data object MainScreen : FirstRunDestination("main_screen")

    @Serializable
    data class SanbaiImport(val url: String) : FirstRunDestination("sanbaiImport")
}