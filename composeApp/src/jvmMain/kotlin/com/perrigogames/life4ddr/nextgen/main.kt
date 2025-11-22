package com.perrigogames.life4ddr.nextgen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LIFE4DDR",
    ) {
        App()
    }
}