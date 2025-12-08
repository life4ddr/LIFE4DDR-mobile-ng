package com.perrigogames.life4ddr.nextgen.navigation

import androidx.navigation.NavController

fun NavController.popAndNavigate(destination: String) {
    popBackStack()
    navigate(destination)
}

fun <T: Any> NavController.popAndNavigate(destination: T) {
    popBackStack()
    navigate(destination)
}
