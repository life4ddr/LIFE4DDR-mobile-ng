package com.perrigogames.life4ddr.nextgen.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.enums.nameRes
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerContainer
import com.perrigogames.life4ddr.nextgen.feature.ladder.LadderGoalsContent
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderData
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerInfoViewState
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileInput
import com.perrigogames.life4ddr.nextgen.util.ViewState
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileContent(
    playerInfoViewState: PlayerInfoViewState,
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

            if (goalData != null) {
                LadderGoalsContent(
                    goals = goalData.goals,
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
