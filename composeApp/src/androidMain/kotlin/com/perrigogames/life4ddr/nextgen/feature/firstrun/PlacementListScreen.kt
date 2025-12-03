package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.*
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementMocks
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListViewModel
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialMocks
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.mvvm.createViewModelFactory
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementListScreen(
    viewModel: PlacementListViewModel = viewModel(
        factory = createViewModelFactory { PlacementListViewModel() }
    ),
    onPlacementSelected: (String) -> Unit,
    onRanksClicked: () -> Unit,
    goToMainScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var closeConfirmShown by remember { mutableStateOf(false) }
    val modalBottomSheetState = rememberModalBottomSheetState()
    var selectedPlacement by remember { mutableStateOf<String?>(null) }

    BackHandler {
        scope.launch {
            if (modalBottomSheetState.isVisible) {
                modalBottomSheetState.hide()
            } else {
                modalBottomSheetState.show()
            }
        }
    }
    
    val data by viewModel.screenData.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = data.titleText.toString(context = context),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = Paddings.LARGE, start = Paddings.LARGE)
        )
        SizedSpacer(16.dp)
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = data.headerText.toString(context = context),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            items(data.placements) { placement ->
                SizedSpacer(16.dp)
                LadderRankClassTheme(ladderRankClass = placement.rankIcon.group) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large
                    ) {
                        PlacementListItem(
                            data = placement,
                            expanded = selectedPlacement == placement.id,
                            onExpand = {
                                selectedPlacement = when {
                                    selectedPlacement == placement.id -> null
                                    else -> placement.id
                                }
                            },
                            onPlacementSelected = { onPlacementSelected(placement.id) }
                        )
                    }
                }
            }
        }

        SizedSpacer(Paddings.LARGE)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.setFirstRunState(InitState.RANKS)
                onRanksClicked()
            },
        ) {
            Text(
                text = stringResource(MR.strings.select_rank_instead),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(
            onClick = { closeConfirmShown = true },
        ) {
            Text(
                text = stringResource(MR.strings.start_no_rank),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        SizedSpacer(Paddings.LARGE)
    }
    if (closeConfirmShown) {
        AlertDialog(
            title = {
                Text(
                    text = stringResource(MR.strings.placement_close_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Text(
                    text = stringResource(MR.strings.placement_close_confirm_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            onDismissRequest = {
                closeConfirmShown = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFirstRunState(InitState.DONE)
                        goToMainScreen()
                    }
                ) { Text(stringResource(MR.strings.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closeConfirmShown = false
                    }
                ) { Text(stringResource(MR.strings.cancel)) }
            }
        )
    }
}

@Composable
@LightDarkModeSystemPreviews
fun Preview_PlacementScreen() {
    LIFE4Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlacementListScreen(
                onPlacementSelected = {},
                onRanksClicked = {},
                goToMainScreen = {}
            )
        }
    }
}

@Composable
@LightDarkModePreviews
fun Preview_PlacementItem(
    @PreviewParameter(LadderRankLevel3ParameterProvider::class) rank: LadderRank,
) {
    ThemedRankSurface(rank) {
        PlacementListItem(
            data = UIPlacementMocks.createUIPlacementData(rankIcon = rank),
            onPlacementSelected = {}
        )
    }
}

@Composable
@LightDarkModePreviews
fun Preview_PlacementItemExpanded(
    @PreviewParameter(LadderRankLevel3ParameterProvider::class) rank: LadderRank,
) {
    ThemedRankSurface(rank) {
        PlacementListItem(
            data = UIPlacementMocks.createUIPlacementData(
                rankIcon = rank
            ),
            expanded = true,
            onPlacementSelected = {}
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun Preview_PlacementSongItem() {
    LIFE4Theme {
        Surface(color = MaterialTheme.colorScheme.primaryContainer) {
            PlacementSongItem(
                data = UITrialMocks.createUITrialSong()
            )
        }
    }
}

@Composable
private fun ThemedRankSurface(
    rank: LadderRank,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit,
) {
    LIFE4Theme {
        LadderRankClassTheme(ladderRankClass = rank.group) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = shape,
                modifier = modifier,
            ) {
                content()
            }
        }
    }
}