package com.perrigogames.life4ddr.nextgen

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform