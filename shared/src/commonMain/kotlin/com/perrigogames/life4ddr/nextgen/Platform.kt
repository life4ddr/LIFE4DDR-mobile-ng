package com.perrigogames.life4ddr.nextgen

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

/**
 * Formats an integer with separators (1234567 -> 1,234,567)
 */
expect fun Int.longNumberString(): String