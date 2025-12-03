package com.perrigogames.life4ddr.nextgen.feature.scorelist

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListEvent
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListViewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreListScreen(
    modifier: Modifier = Modifier,
    viewModel: ScoreListViewModel = viewModel(
        factory = createViewModelFactory { ScoreListViewModel() }
    ),
    onBackPressed: () -> Unit,
    showSanbaiLogin: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
        )
    )

    BackHandler {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        } else {
            onBackPressed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when(it) {
                is ScoreListEvent.ShowSanbaiLogin -> showSanbaiLogin(it.url)
            }
        }
    }

    ScoreListContent(
        scope = scope,
        state = state,
        scaffoldState = scaffoldState,
        modifier = modifier,
        onInput = { viewModel.handleInput(it) },
    )
}
