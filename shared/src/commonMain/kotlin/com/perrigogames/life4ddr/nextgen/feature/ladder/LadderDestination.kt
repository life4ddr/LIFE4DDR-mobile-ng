package com.perrigogames.life4ddr.nextgen.feature.ladder

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.util.Destination
import kotlinx.serialization.Serializable

/**
 * Sealed class representing different destinations related to the ladder in LIFE4.
 *
 * @param baseRoute The base route provided to the navigation framework.
 */
@Serializable
sealed class LadderDestination(override val baseRoute: String): Destination {

    @Serializable
    data object RankList: LadderDestination("rank_list")

    @Serializable
    data class RankDetails(val rank: LadderRank): LadderDestination(BASE_ROUTE) {
        override val route = BASE_ROUTE.replace("{$RANK_ID}", rank.stableId.toString())
        companion object {
            const val RANK_ID = "rank_id"
            const val BASE_ROUTE = "rank_details/{$RANK_ID}"
        }
    }
}