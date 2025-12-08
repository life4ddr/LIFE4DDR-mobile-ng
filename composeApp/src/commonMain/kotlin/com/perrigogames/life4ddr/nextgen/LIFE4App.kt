package com.perrigogames.life4ddr.nextgen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.perrigogames.life4ddr.nextgen.navigation.RootNavHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun LIFE4App(
    onExit: () -> Unit = {}
) {
    RootNavHost(
        modifier = Modifier.fillMaxSize(),
        onLoaded = {},
        onExit = onExit
    )
}
