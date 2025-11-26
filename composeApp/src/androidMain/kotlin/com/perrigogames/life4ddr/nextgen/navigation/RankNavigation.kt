package com.perrigogames.life4ddr.nextgen.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.perrigogames.life4ddr.nextgen.feature.firstrun.RankListScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderGoalsScreen
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderDestination
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent

fun NavGraphBuilder.ladderNavigation(navController: NavController) {

    composable(LadderDestination.RankList.baseRoute) {
        RankListScreen { action ->
            when(action) {
                RankListViewModelEvent.NavigateToMainScreen -> navController.popBackStack()
                RankListViewModelEvent.NavigateToPlacements -> {} // we should never do this outside first run
            }
        }
    }

    composable(
        route = LadderDestination.RankDetails.BASE_ROUTE,
        arguments = listOf(
            navArgument(LadderDestination.RankDetails.RANK_ID) { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val rankId = backStackEntry.arguments?.getLong(LadderDestination.RankDetails.RANK_ID)
        LadderGoalsScreen(
            targetRank = LadderRank.parse(rankId)
        )
    }
}