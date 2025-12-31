package com.perrigogames.life4ddr.nextgen.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.enums.nameRes
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerContainer
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderGoalsContent
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderData
import com.perrigogames.life4ddr.nextgen.feature.profile.data.ProfileHeader
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerInfoViewState
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileEvent
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileInput
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileViewModel
import com.perrigogames.life4ddr.nextgen.util.ViewState
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PlayerProfileScreen(
    onBackPressed: () -> Unit = {},
    onAction: (PlayerProfileEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val viewModel = koinViewModel<PlayerProfileViewModel>()

    val playerInfoViewState by viewModel.playerInfoViewState.collectAsState()
    val headerViewState by viewModel.headerViewState.collectAsState()
    val goalListViewState by viewModel.goalListViewModel.state.collectAsState()
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
        viewModel.goalListViewModel.showBottomSheet.collect {
            bottomSheetState.expand()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { onAction(it) }
    }

    PlayerProfileContent(
        playerInfoViewState = playerInfoViewState,
        headerViewState = headerViewState,
        goalListViewState = goalListViewState,
        bottomSheetState = bottomSheetState,
        onInput = { viewModel.handleInput(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileContent(
    playerInfoViewState: PlayerInfoViewState,
    headerViewState: ProfileHeader?,
    goalListViewState: ViewState<UILadderData, String>,
    bottomSheetState: SheetState,
    onInput: (PlayerProfileInput) -> Unit = {},
) {
    val goalData = (goalListViewState as? ViewState.Success)?.data
    val goalError = (goalListViewState as? ViewState.Error)?.error

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = bottomSheetState
        ),
        sheetContent = {
            if (goalData?.hasSubstitutions == true) {
                LadderGoalsContent(
                    goals = goalData.substitutions!!,
                    useMonospaceFontForScore = goalData.useMonospaceFontForScore,
                    hideCompletedToggle = null,
                    rankClass = goalData.targetRankClass,
                    onInput = { onInput(PlayerProfileInput.GoalList(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .weight(1f)
                )
            }
        },
        sheetPeekHeight = 0.dp,
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {
            PlayerProfileInfo(
                state = playerInfoViewState,
                modifier = Modifier.fillMaxWidth(),
                onRankClicked = { onInput(PlayerProfileInput.ChangeRankClicked) }
            )
            BannerContainer(playerInfoViewState.banner)

            if (headerViewState != null) {
                Surface(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = headerViewState.title.localized(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = headerViewState.subtitle.localized(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Button(
                            onClick = { onInput(headerViewState.buttonAction) }
                        ) {
                            Text(
                                text = headerViewState.buttonText.localized(),
                            )
                        }
                    }
                }
            }

            if (goalData != null) {
                LadderGoalsContent(
                    goals = goalData.goals,
                    hideCompletedToggle = goalData.hideCompleted,
                    useMonospaceFontForScore = goalData.useMonospaceFontForScore,
                    rankClass = goalData.targetRankClass,
                    onInput = { onInput(PlayerProfileInput.GoalList(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            if (goalError != null) {
                Text(
                    text = goalError,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
fun PlayerProfileInfo(
    state: PlayerInfoViewState,
    modifier: Modifier = Modifier,
    onRankClicked: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(16.dp)
            .then(modifier)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = state.username,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SizedSpacer(8.dp)
            state.rivalCode?.let {  rivalCode ->
                Text(
                    text = rivalCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RankImage(
                rank = state.rank,
                size = 64.dp,
                onClick = onRankClicked
            )
            Text(
                text = stringResource(state.rank.nameRes),
                style = MaterialTheme.typography.labelMedium,
                color = state.rank?.colorRes?.let { colorResource(it) } ?: Color.Unspecified
            )
        }
    }
}

@Composable
@Preview(widthDp = 480)
fun PlayerProfilePreview() {
    LIFE4Theme {
        PlayerProfileScreen {}
    }
}

@Composable
@Preview(widthDp = 480)
fun PlayerProfileInfoPreview() {
    LIFE4Theme {
        Column {
            PlayerProfileInfo(
                PlayerInfoViewState(
                    username = "KONNOR"
                )
            )
            SizedSpacer(16.dp)
            PlayerProfileInfo(
                PlayerInfoViewState(
                    username = "KONNOR",
                    rivalCode = "1234-5678"
                )
            )
        }
    }
}
