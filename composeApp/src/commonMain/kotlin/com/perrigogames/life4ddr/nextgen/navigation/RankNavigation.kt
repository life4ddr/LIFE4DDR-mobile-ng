package com.perrigogames.life4ddr.nextgen.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderDestination
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderGoalsScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.RankListScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent

fun NavGraphBuilder.ladderNavigation(navController: NavController) {

    composable<LadderDestination.RankList> {
        RankListScreen { action ->
            when(action) {
                RankListViewModelEvent.NavigateToMainScreen -> navController.popBackStack()
                RankListViewModelEvent.NavigateToPlacements -> {} // we should never do this outside first run
            }
        }
    }

    composable<LadderDestination.RankDetails> { backStackEntry ->
        LadderGoalsScreen(
            targetRank = LadderRank.parse(backStackEntry.toRoute<LadderDestination.RankDetails>().rankId)
        )
    }
}