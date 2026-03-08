package com.perrigogames.life4ddr.nextgen.compose

import androidx.compose.runtime.Composable

@Composable
actual fun LIFE4Theme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CoreLIFE4Theme(
        darkTheme = darkTheme,
        content = content,
    )
}