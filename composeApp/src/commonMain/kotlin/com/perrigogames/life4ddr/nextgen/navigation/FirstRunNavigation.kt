package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.PlacementDetailsScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.PlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.RankListScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListEvent

fun NavGraphBuilder.firstRunNavigation(
    navController: NavController,
    onExit: () -> Unit,
) {
    composable<FirstRunDestination.Landing> {}

    composable<FirstRunDestination.FirstRun> {
        FirstRunScreen(
            modifier = Modifier.fillMaxSize(),
            onComplete = { when (it) {
                InitState.PLACEMENTS -> navController.popAndNavigate(FirstRunDestination.PlacementList)
                InitState.RANKS -> navController.popAndNavigate(FirstRunDestination.InitialRankList)
                InitState.DONE -> navController.popAndNavigate(FirstRunDestination.MainScreen)
            } },
            onClose = { onExit() },
        )
    }

    composable<FirstRunDestination.PlacementList> {
        PlacementListScreen(
            onEvent = {
                when(it) {
                    is PlacementListEvent.NavigateToPlacementDetails -> {
                        navController.navigate("placement_details/${it.placementId}")
                    }
                    PlacementListEvent.NavigateToRanks -> {
                        navController.popAndNavigate(FirstRunDestination.InitialRankList)
                    }
                    PlacementListEvent.NavigateToMainScreen -> {
                        navController.popAndNavigate(FirstRunDestination.MainScreen)
                    }
                }
            },
        )
    }

    composable<FirstRunDestination.PlacementDetails> { backStackEntry ->
    val placementId = backStackEntry.toRoute<FirstRunDestination.PlacementDetails>().placementId
        PlacementDetailsScreen(
            placementId = placementId,
            onBackPressed = { navController.popBackStack() },
            onNavigateToMainScreen = { url ->
                navController.popBackStack()
                navController.popAndNavigate(FirstRunDestination.MainScreen)

                url?.let {
                    // TODO Web
//                    navController.context.openWebUrl(it)
                }
            }
        )
    }

    composable<FirstRunDestination.InitialRankList> {
        RankListScreen(isFirstRun = true) { action ->
            when(action) {
                RankListViewModelEvent.NavigateToPlacements -> navController.popAndNavigate(FirstRunDestination.PlacementList)
                RankListViewModelEvent.NavigateToMainScreen -> navController.popAndNavigate(FirstRunDestination.MainScreen)
            }
        }
    }

    composable<FirstRunDestination.MainScreen> {
        MainScreen(
            mainNavController = navController
        )
    }

    composable<FirstRunDestination.SanbaiImport> { backStackEntry ->
        val url = backStackEntry.toRoute<FirstRunDestination.SanbaiImport>().url
//        val state = rememberWebViewState(url = url)
//        val navigator = rememberWebViewNavigator()
//
//        Column {
//            val loadingState = state.loadingState
//            if (loadingState is LoadingState.Loading) {
//                LinearProgressIndicator(
//                    progress = { loadingState.progress },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            // A custom WebViewClient and WebChromeClient can be provided via subclassing
//            val webClient = remember {
//                object : AccompanistWebViewClient() {
//                    override fun onPageStarted(
//                        view: WebView,
//                        url: String?,
//                        favicon: Bitmap?
//                    ) {
//                        super.onPageStarted(view, url, favicon)
//                        Log.d("Accompanist WebView", "Page started loading for $url")
//                    }
//                }
//            }
//
//            WebView(
//                state = state,
//                modifier = Modifier
//                    .weight(1f),
//                navigator = navigator,
//                onCreated = { webView ->
//                    webView.settings.javaScriptEnabled = true
//                },
//                client = webClient
//            )
//        }
    }
}