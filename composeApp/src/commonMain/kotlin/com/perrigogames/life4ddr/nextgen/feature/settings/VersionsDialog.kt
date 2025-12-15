package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UIVersionsDialog
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.VersionsDialogViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VersionsDialog(
    onDismiss: () -> Unit = {}
) {
    val viewModel: VersionsDialogViewModel = koinViewModel<VersionsDialogViewModel>()
    val state by viewModel.state.collectAsState()
    VersionsDialog(
        state = state,
        onDismiss = onDismiss,
    )
}

@Composable
fun VersionsDialog(
    state: UIVersionsDialog,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Column {
            Text(text = "App version: ${state.appVersion}")
            Text(text = "Ladder data version: ${state.ladderDataVersion}")
            Text(text = "MOTD version: ${state.motdVersion}")
            Text(text = "Song list version: ${state.songListVersion}")
            Text(text = "Trial data version: ${state.trialDataVersion}")
        }
    }
}

@Composable
@Preview
fun VersionsDialogPreview() {
    LIFE4Theme {
        VersionsDialog(
            state = UIVersionsDialog(
                appVersion = "1.0.0",
                ladderDataVersion = "1.0.0",
                motdVersion = "1.0.0",
                songListVersion = "1.0.0",
                trialDataVersion = "1.0.0",
            )
        )
    }
}