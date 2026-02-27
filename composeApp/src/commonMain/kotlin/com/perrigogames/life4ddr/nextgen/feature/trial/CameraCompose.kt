package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

// Thanks to ProAndroidDev: https://proandroiddev.com/compose-multi-platform-custom-camera-with-common-capture-design-386dbc2aa03e
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraBottomSheet(
    bottomSheetState: SheetState,
    onPhotoTaken: (Path) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = { onDismiss() }
    ) {
        ImageCaptureView(
            onImageCaptured = onPhotoTaken,
            onClose = onDismiss,
        )
    }
}

@Composable
fun ImageCaptureView(
    onImageCaptured: (Path) -> Unit,
    onClose: () -> Unit = {},
    onError: (String) -> Unit = {}
) {
    val logger = koinInject<Logger> { parametersOf("ImageCaptureView") }
    val scope = rememberCoroutineScope()
    val callback = remember {
        object : CameraCallback() {
            override fun onCaptureImage(image: Path?, error: String?) {
                if (error != null) {
                    logger.e { "Error capturing image: $error" }
                    onError(error)
                    return
                }
                if (image != null) {
                    logger.v { "Image captured: $image" }
                    onImageCaptured(image)
                }
            }
        }
    }

    fun takePicture() = scope.launch {
        callback.sendEvent(CameraEvent.CaptureImage)
    }

    fun switchCamera() = scope.launch {
        callback.sendEvent(CameraEvent.SwitchCamera)
    }

    Box(modifier = Modifier.fillMaxSize()) {
//      CameraView from each platform using expect/actual functionality
        CameraView(callback)
//      Custom Capture View Design
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .height(120.dp)
                .background(color = Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier
                    .size(60.dp),
                onClick = onClose, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(MR.images.close),
                    contentDescription = "Settings",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Red
                )
            }
            IconButton(
                onClick = ::takePicture,
                modifier = Modifier
                    .size(80.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(MR.images.photo_camera),
                    contentDescription = "Take photo",
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(
                modifier = Modifier
                    .size(60.dp),
                onClick = ::switchCamera, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    painter = painterResource(MR.images.camera_switch),
                    contentDescription = "Settings",
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

@Composable
expect fun CameraView(callback: CameraCallback)

sealed class CameraEvent {
    object CaptureImage : CameraEvent()
    object SwitchCamera : CameraEvent()
}

abstract class CameraCallback {
    private val _event = Channel<CameraEvent>()
    val eventFlow: Flow<CameraEvent> get() = _event.receiveAsFlow()

    suspend fun sendEvent(event: CameraEvent) {
        this._event.send(event)
    }

    abstract fun onCaptureImage(image: Path? = null, error: String? = null)
}

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

