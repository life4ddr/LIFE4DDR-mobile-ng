package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.profile.ProfileDestination
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.MainScreenViewModel
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    mainNavController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val profileNavController = rememberNavController()
    val profileState by viewModel.state.collectAsState()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by profileNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                profileState.tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                ProfileDestination.Profile -> Icon(painterResource(MR.images.home), contentDescription = null)
                                ProfileDestination.Scores -> Icon(painterResource(MR.images.list), contentDescription = null)
                                ProfileDestination.Settings -> Icon(painterResource(MR.images.settings), contentDescription = null)
                                ProfileDestination.Trials -> Icon(painterResource(MR.images.trophy), contentDescription = null)
                            }
                        },
                        label = { Text(screen.title.localized()) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            profileNavController.navigate(screen) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(profileNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
//        fun enterTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? {
//            slideInHorizontally(
//                initialOffsetX = { 1000 },
//                animationSpec = tween(500)
//            )
//        }
//        fun exitTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? {
//            slideOutHorizontally(
//                targetOffsetX = { -1000 },
//                animationSpec = tween(500)
//            )
//        }

        NavHost(
            navController = profileNavController,
            startDestination = ProfileDestination.Profile,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            profileNavigation(
                mainNavController = mainNavController,
                profileNavController = profileNavController,
            )
        }
    }
}