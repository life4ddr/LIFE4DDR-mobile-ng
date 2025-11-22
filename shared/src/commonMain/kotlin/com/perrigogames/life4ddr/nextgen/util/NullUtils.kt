package com.perrigogames.life4ddr.nextgen.util

inline fun <T : Any> T?.orElse(block: () -> T) = this ?: block()

inline fun <T : Any> T?.ifNull(block: () -> Unit) = apply { if (this == null) block() }