package com.perrigogames.life4ddr.nextgen

import android.os.Build
import java.text.DecimalFormat

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun Int.longNumberString(): String = DecimalFormat("#,###,###").format(this)
