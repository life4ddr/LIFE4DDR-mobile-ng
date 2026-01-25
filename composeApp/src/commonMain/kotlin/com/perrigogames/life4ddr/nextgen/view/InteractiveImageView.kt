package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest

@Composable
fun InteractiveImage(
    painter: Painter,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    // Remember state for scale, translation (drag), and rotation (if needed)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }
    var rotationState by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { _, pan, zoom, rotation ->
                    scale = (scale * zoom).coerceIn(0.7f, 3f) // Limit zoom
                    offset = Offset(offset.x + pan.x, offset.y + pan.y) // Update drag/translate
                    rotationState += rotation // Keep track of rotation if needed
                }
            )
        }
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                    rotationZ = rotationState // You can remove this if rotation is not required
                )
        )
    }
}