package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraBottomSheet(
    bottomSheetState: SheetState,
    onPhotoTaken: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = { onDismiss() }
    ) {
        CameraBottomSheetContent(onPhotoTaken = onPhotoTaken)
    }
}

@Composable
expect fun CameraBottomSheetContent(
    onPhotoTaken: (String) -> Unit,
)

@Composable
fun PermissionReminder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(MR.strings.camera_permission_reminder_title),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        SizedSpacer(16.dp)
        Text(
            text = stringResource(MR.strings.camera_permission_reminder_body),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

