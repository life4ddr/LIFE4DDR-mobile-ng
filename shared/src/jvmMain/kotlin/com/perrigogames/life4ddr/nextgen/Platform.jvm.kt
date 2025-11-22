package com.perrigogames.life4ddr.nextgen

import java.text.DecimalFormat

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun Int.longNumberString(): String = DecimalFormat("#,###,###").format(this)
