package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.trial.TrialSessionScreen
import com.perrigogames.life4ddr.nextgen.feature.trials.TrialDestination
import com.perrigogames.life4ddr.nextgen.util.ExternalActions
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.koinInject

fun NavGraphBuilder.trialNavigation(navController: NavController) {
    composable<TrialDestination.TrialDetails> { backStackEntry ->
        val externalActions = koinInject<ExternalActions>()
        val trialId = backStackEntry.toRoute<TrialDestination.TrialDetails>().trialId
        val submissionUrl = stringResource(MR.strings.url_submission)
        TrialSessionScreen(
            trialId = trialId,
            modifier = Modifier.fillMaxSize(),
            onClose = { navController.popBackStack() },
            onSubmit = {
                // TODO automate the submission
                externalActions.openWeblink(submissionUrl)
            }
        )
    }

    composable<TrialDestination.TrialRecords> {

    }
}