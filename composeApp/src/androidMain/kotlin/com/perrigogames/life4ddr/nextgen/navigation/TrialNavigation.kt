package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.perrigogames.life4ddr.nextgen.feature.trial.TrialSession
import com.perrigogames.life4ddr.nextgen.feature.trials.TrialDestination

fun NavGraphBuilder.trialNavigation(navController: NavController) {
    composable(
        route = TrialDestination.TrialDetails.BASE_ROUTE
    ) { entry ->
        val trialId = entry.arguments?.getString("trialId") ?: "empty"
        TrialSession(
            trialId = trialId,
            modifier = Modifier.fillMaxSize(),
            onClose = { navController.popBackStack() }
        )
    }

    composable(TrialDestination.TrialRecords.route) {
        Text("Trial Records TODO")
    }
}