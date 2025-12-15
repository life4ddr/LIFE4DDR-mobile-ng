package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel.LaunchViewModel
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    onLoaded: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    val navController = rememberNavController()

    val viewModel = koinViewModel<LaunchViewModel>()
    val deeplinkManager: DeeplinkManager = koinInject()

    LaunchedEffect(Unit) {
        val initialState = viewModel.launchState.first()
        navController.popAndNavigate(when(initialState) {
            null -> FirstRunDestination.FirstRun
            InitState.PLACEMENTS -> FirstRunDestination.PlacementList
            InitState.RANKS -> FirstRunDestination.InitialRankList
            InitState.DONE -> FirstRunDestination.MainScreen
        })
        onLoaded()
    }

    LIFE4Theme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            NavHost(
                navController = navController,
                startDestination = FirstRunDestination.Landing,
                modifier = modifier
            ) {
                firstRunNavigation(
                    navController = navController,
                    deeplinkManager = deeplinkManager,
                    onExit = onExit
                )
                ladderNavigation(navController)
                trialNavigation(navController)
                settingsNavigation(navController)
            }
        }
    }
}