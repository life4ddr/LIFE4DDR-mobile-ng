package com.perrigogames.life4ddr.nextgen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.navigation.RootNavHost
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LIFE4App(
    onLoaded: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    LIFE4Theme {
        RootNavHost(
            modifier = Modifier.fillMaxSize(),
            onLoaded = onLoaded,
            onExit = onExit
        )
    }
}
