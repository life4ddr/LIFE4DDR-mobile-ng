package com.perrigogames.life4ddr.nextgen

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.currentLocale
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

private val numberFormatter = NSNumberFormatter().also { format ->
    format.usesGroupingSeparator = true
    format.locale = NSLocale.currentLocale
}

actual fun Int.longNumberString(): String = numberFormatter.stringFromNumber(NSNumber(this))!!