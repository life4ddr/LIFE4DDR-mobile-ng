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
