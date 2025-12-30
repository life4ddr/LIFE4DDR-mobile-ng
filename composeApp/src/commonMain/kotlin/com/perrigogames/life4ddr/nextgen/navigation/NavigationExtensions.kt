package com.perrigogames.life4ddr.nextgen.navigation

import androidx.navigation.NavController

fun <T: Any> NavController.popAndNavigate(destination: T) {
    popBackStack()
    navigate(destination)
}
