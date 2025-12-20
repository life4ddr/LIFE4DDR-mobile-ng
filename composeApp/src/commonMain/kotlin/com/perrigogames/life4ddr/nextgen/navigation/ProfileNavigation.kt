package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderDestination
import com.perrigogames.life4ddr.nextgen.feature.profile.PlayerProfileScreen
import com.perrigogames.life4ddr.nextgen.feature.profile.ProfileDestination
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileEvent
import com.perrigogames.life4ddr.nextgen.feature.scorelist.ScoreListScreen
import com.perrigogames.life4ddr.nextgen.feature.settings.SettingsScreen
import com.perrigogames.life4ddr.nextgen.feature.trial.TrialListScreen
import com.perrigogames.life4ddr.nextgen.feature.trials.TrialDestination
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.profileNavigation(
    mainNavController: NavController,
    profileNavController: NavController
) {
    composable<ProfileDestination.Profile> {
        PlayerProfileScreen(
            onBackPressed = { mainNavController.popBackStack() },
            onAction = { action ->
                when (action) {
                    PlayerProfileEvent.NavigateToChangeRank -> {
                        mainNavController.navigate(LadderDestination.RankList)
                    }
                }
            },
        )
    }

    composable<ProfileDestination.Scores> {
        ScoreListScreen(
            showSanbaiLogin = { url ->
                mainNavController.navigate(FirstRunDestination.SanbaiImport(url))
            },
            onBackPressed = { profileNavController.navigate(ProfileDestination.Profile) }
        )
    }

    composable<ProfileDestination.Trials> {
        TrialListScreen(
            modifier = Modifier.fillMaxSize(),
            onTrialSelected = { selectedTrial ->
                mainNavController.navigate(TrialDestination.TrialDetails(selectedTrial.id))
            },
            onPlacementsSelected = {
                mainNavController.navigate(FirstRunDestination.PlacementList)
            }
        )
    }

    composable<ProfileDestination.Settings> {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            onClose = { mainNavController.popBackStack() },
            onNavigate = { destination ->
                mainNavController.navigate(destination)
            },
            logger = koinInject { parametersOf("Settings") }
        )
    }
}