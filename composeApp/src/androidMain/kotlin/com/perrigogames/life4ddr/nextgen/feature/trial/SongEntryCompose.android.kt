package com.perrigogames.life4ddr.nextgen.feature.trial

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialBottomSheet
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionInput
import com.perrigogames.life4ddr.nextgen.util.InteractiveImage

@Composable
fun SongEntryBottomSheetContent(
    viewData: UITrialBottomSheet.Details,
    onAction: (TrialSessionInput) -> Unit,
) {
    BackHandler {
        onAction(TrialSessionInput.HideBottomSheet)
    }

    val context = LocalContext.current
    val decodedBitmap = remember(viewData.imagePath) {
        if (viewData.imagePath.isEmpty()) return@remember null
        try {
            viewData.imagePath.toUri()
                .let { uri ->
                    println(uri.toString())
                    context.contentResolver.openInputStream(uri)
                }
                ?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        decodedBitmap?.let {
            InteractiveImage(
                bitmap = decodedBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        SongEntryControls(
            fields = viewData.fields,
            shortcuts = viewData.shortcuts,
            isEdit = viewData.isEdit,
            submitAction = viewData.onDismissAction,
            onAction = onAction,
        )
    }
}
