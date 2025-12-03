package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.VersionsDialogViewModel
import dev.icerock.moko.mvvm.createViewModelFactory

@Composable
fun VersionsDialog(
    viewModel: VersionsDialogViewModel = viewModel(
        factory = createViewModelFactory { VersionsDialogViewModel() }
    ),
    onDismiss: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    VersionsDialog(
        state = state,
        onDismiss = onDismiss,
    )
}
