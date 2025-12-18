package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.perrigogames.life4ddr.nextgen.feature.trial.TrialSessionScreen
import com.perrigogames.life4ddr.nextgen.feature.trials.TrialDestination

fun NavGraphBuilder.trialNavigation(navController: NavController) {
    composable<TrialDestination.TrialDetails> { backStackEntry ->
        val trialId = backStackEntry.toRoute<TrialDestination.TrialDetails>().trialId
        TrialSessionScreen(
            trialId = trialId,
            modifier = Modifier.fillMaxSize(),
            onClose = { navController.popBackStack() }
        )
    }

    composable<TrialDestination.TrialRecords> {

    }
}