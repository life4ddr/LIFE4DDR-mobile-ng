package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.feature.ladder.RankListContent
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModel
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent
import dev.icerock.moko.mvvm.createViewModelFactory

@Composable
fun RankListScreen(
    isFirstRun: Boolean = false,
    viewModel: RankListViewModel = viewModel(
        factory = createViewModelFactory { RankListViewModel(isFirstRun) }
    ),
    onAction: (RankListViewModelEvent) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.actions.collect(onAction)
    }
    RankListContent(
        state = state,
        onInput = { viewModel.onInputAction(it) },
    )
}
