package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.PlacementDetailsScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.PlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.RankListScreen
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun NavGraphBuilder.firstRunNavigation(
    navController: NavController,
    deeplinkManager: DeeplinkManager,
    onExit: () -> Unit,
) {
    val fullScreenModifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .systemBarsPadding()

    composable<FirstRunDestination.Landing> {}

    composable<FirstRunDestination.FirstRun> {
        FirstRunScreen(
            modifier = fullScreenModifier,
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
            modifier = fullScreenModifier,
            onNavigate = { destination, popExisting ->
                if (popExisting) {
                    navController.popBackStack()
                }
                navController.navigate(destination)
            },
        )
    }

    composable<FirstRunDestination.PlacementDetails> { backStackEntry ->
    val placementId = backStackEntry.toRoute<FirstRunDestination.PlacementDetails>().placementId
        PlacementDetailsScreen(
            modifier = fullScreenModifier,
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
        RankListScreen(
            modifier = fullScreenModifier,
            isFirstRun = true
        ) { action ->
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
        val scope = rememberCoroutineScope { Dispatchers.Main }
        val state = rememberWebViewState(
            url = url,
            extraSettings = {
                androidWebSettings.apply {
                    textZoom = 50
                }
                iOSWebSettings.apply {
                    zoomLevel = 0.5
                    bounces = false
                    scrollEnabled = false
                }
            }
        )
        val navigator = rememberWebViewNavigator(
            requestInterceptor = object : RequestInterceptor {
                override fun onInterceptUrlRequest(
                    request: WebRequest,
                    navigator: WebViewNavigator
                ): WebRequestInterceptResult {
                    return if (request.url.startsWith("life4://")) {
                        val shouldClose = deeplinkManager.processDeeplink(request.url)
                        if (shouldClose) {
                            scope.launch {
                                navController.popBackStack()
                            }
                            WebRequestInterceptResult.Reject
                        } else {
                            WebRequestInterceptResult.Allow
                        }
                    } else {
                        WebRequestInterceptResult.Allow
                    }
                }
            }
        )
        val loadingState by derivedStateOf { state.loadingState }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            (loadingState as? LoadingState.Loading)?.let {
                LinearProgressIndicator(
                    progress = { it.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            WebView(
                state = state,
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}