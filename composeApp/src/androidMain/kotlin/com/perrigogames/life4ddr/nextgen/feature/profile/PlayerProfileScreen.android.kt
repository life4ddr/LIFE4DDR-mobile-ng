package com.perrigogames.life4ddr.nextgen.feature.profile

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileEvent
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    profileViewModel: PlayerProfileViewModel = viewModel(
        factory = createViewModelFactory { PlayerProfileViewModel() }
    ),
    onBackPressed: () -> Unit = {},
    onAction: (PlayerProfileEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val playerInfoViewState by profileViewModel.playerInfoViewState.collectAsState()
    val goalListViewState by profileViewModel.goalListViewModel.state.collectAsState()
    val density = LocalDensity.current
    val bottomSheetState = remember {
        SheetState(
            initialValue = SheetValue.Hidden,
            skipPartiallyExpanded = false,
            positionalThreshold = { with(density) { 56.dp.toPx() }},
            velocityThreshold = { with(density) { 125.dp.toPx() }},
        )
    }

    BackHandler {
        if (bottomSheetState.isVisible) {
            scope.launch { bottomSheetState.hide() }
        } else {
            onBackPressed()
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.goalListViewModel.showBottomSheet.collect {
            bottomSheetState.expand()
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.events.collect { onAction(it) }
    }

    PlayerProfileContent(
        playerInfoViewState = playerInfoViewState,
        goalListViewState = goalListViewState,
        bottomSheetState = bottomSheetState,
        onInput = { profileViewModel.handleInput(it) },
    )
}

@Composable
@Preview(widthDp = 480)
fun PlayerProfilePreview() {
    LIFE4Theme {
        PlayerProfileScreen {}
    }
}
