package com.perrigogames.life4ddr.nextgen

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun Int.longNumberString(): String {
    return NSNumberFormatter().let { format ->
        format.usesGroupingSeparator = true
        //format.locale = NSLocale.currentLocale() TODO figure out locale
        format.stringFromNumber(NSNumber(this))!!
    }
}