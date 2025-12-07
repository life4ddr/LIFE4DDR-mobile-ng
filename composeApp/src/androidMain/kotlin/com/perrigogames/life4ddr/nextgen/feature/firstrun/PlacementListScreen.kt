package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.compose.*
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementMocks
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListEvent
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListViewModel
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialMocks
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementListScreen(
    modifier: Modifier = Modifier,
    viewModel: PlacementListViewModel = viewModel(
        factory = createViewModelFactory { PlacementListViewModel() }
    ),
    onEvent: (PlacementListEvent) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState()

    BackHandler {
        scope.launch {
            if (modalBottomSheetState.isVisible) {
                modalBottomSheetState.hide()
            } else {
                modalBottomSheetState.show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { onEvent(it) }
    }
    
    val data by viewModel.screenData.collectAsState()
    PlacementListContent(
        data = data,
        modifier = modifier,
        onInput = { viewModel.handleInput(it) }
    )
}

@Composable
@LightDarkModeSystemPreviews
fun Preview_PlacementScreen() {
    LIFE4Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlacementListContent(
                data = UIPlacementMocks.createUIPlacementScreen()
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
            onInput = {}
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
            onInput = {}
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
