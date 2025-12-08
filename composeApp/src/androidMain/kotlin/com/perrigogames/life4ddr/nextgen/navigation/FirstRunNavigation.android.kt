package com.perrigogames.life4ddr.nextgen.navigation

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.kevinnzou.web.AccompanistWebViewClient
import com.kevinnzou.web.LoadingState
import com.kevinnzou.web.WebView
import com.kevinnzou.web.rememberWebViewNavigator
import com.kevinnzou.web.rememberWebViewState
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.PlacementDetailsScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.RankListScreen
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent
import com.perrigogames.life4ddr.nextgen.feature.mainscreen.MainScreen

fun NavGraphBuilder.firstRunNavigation(
    navController: NavController,
    onFinish: () -> Unit,
) {
    composable(
        route = FirstRunDestination.PlacementDetails.BASE_ROUTE,
        arguments = listOf(
            navArgument(FirstRunDestination.PlacementDetails.PLACEMENT_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val placementId = backStackEntry.arguments?.getString(FirstRunDestination.PlacementDetails.PLACEMENT_ID)
        if (placementId != null) {
            PlacementDetailsScreen(
                placementId = placementId,
                onBackPressed = { navController.popBackStack() },
                onNavigateToMainScreen = { url ->
                    navController.popBackStack()
                    navController.popAndNavigate("main_screen")

                    url?.let { navController.context.openWebUrl(it) }
                }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No placement ID provided",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Paddings.HUGE)
                )
            }
        }
    }

    composable(FirstRunDestination.InitialRankList.baseRoute) {
        RankListScreen(isFirstRun = true) { action ->
            when(action) {
                RankListViewModelEvent.NavigateToPlacements -> navController.popAndNavigate("placement_list")
                RankListViewModelEvent.NavigateToMainScreen -> navController.popAndNavigate("main_screen")
            }
        }
    }

    composable(FirstRunDestination.MainScreen.baseRoute) {
        MainScreen(
            mainNavController = navController
        )
    }

    composable<FirstRunDestination.SanbaiImport> { backStackEntry ->
        val url = backStackEntry.toRoute<FirstRunDestination.SanbaiImport>().url
        val state = rememberWebViewState(url = url)
        val navigator = rememberWebViewNavigator()

        Column {
            val loadingState = state.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = { loadingState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // A custom WebViewClient and WebChromeClient can be provided via subclassing
            val webClient = remember {
                object : AccompanistWebViewClient() {
                    override fun onPageStarted(
                        view: WebView,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("Accompanist WebView", "Page started loading for $url")
                    }
                }
            }

            WebView(
                state = state,
                modifier = Modifier
                    .weight(1f),
                navigator = navigator,
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true
                },
                client = webClient
            )
        }
    }
}