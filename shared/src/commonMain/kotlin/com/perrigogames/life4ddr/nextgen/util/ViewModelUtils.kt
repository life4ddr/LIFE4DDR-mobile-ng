package com.perrigogames.life4ddr.nextgen.util

import dev.icerock.moko.mvvm.flow.CMutableStateFlow

fun <T> CMutableStateFlow<T>.mutate(block: T.() -> T) {
    value = block(value)
}