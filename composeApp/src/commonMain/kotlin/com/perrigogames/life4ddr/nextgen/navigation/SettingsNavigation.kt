package com.perrigogames.life4ddr.nextgen.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.perrigogames.life4ddr.nextgen.feature.settings.SongLockScreen
import com.perrigogames.life4ddr.nextgen.feature.settings.SettingsDestination

fun NavGraphBuilder.settingsNavigation(
    navController: NavController,
) {
    composable<SettingsDestination.SongLock> {
        SongLockScreen {
            navController.navigateUp()
        }
    }
}