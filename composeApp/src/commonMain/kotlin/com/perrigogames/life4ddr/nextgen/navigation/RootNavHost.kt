package com.perrigogames.life4ddr.nextgen.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel.LaunchViewModel
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertManager
import com.perrigogames.life4ddr.nextgen.util.MokoImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    onLoaded: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    val navController = rememberNavController()

    val viewModel = koinViewModel<LaunchViewModel>()
    val deeplinkManager: DeeplinkManager = koinInject()
    val alertManager: AlertManager = koinInject()

    val alert by alertManager.alerts.collectAsState()

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

    alert?.let { safeAlert ->
        var hideChecked by remember { mutableStateOf(false) }
        BasicAlertDialog(
            onDismissRequest = { alertManager.dismissAlert(hideChecked) }
        ) {
            Card {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = safeAlert.title.localized(),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    SizedSpacer(16.dp)

                    safeAlert.image?.let { image ->
                        MokoImage(desc = image)
                        SizedSpacer(16.dp)
                    }

                    Text(
                        text = safeAlert.text.localized(),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (safeAlert.canHide && safeAlert.hideCheckboxText != null) {
                        SizedSpacer(8.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(
                                text = safeAlert.hideCheckboxText!!.localized(),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Checkbox(
                                checked = hideChecked,
                                onCheckedChange = { hideChecked = it },
                            )
                        }
                    }
                    TextButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { alertManager.dismissAlert(hideChecked) }
                    ) {
                        Text(
                            text = safeAlert.ctaConfirmText.localized(),
                        )
                    }
                }
            }
        }
    }
}