package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.util.ExternalActions
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosExternalActions : ExternalActions {
    override fun openEmail(email: String) {
        NSURL.URLWithString("mailto:$email")?.let { openUrl(it) }
    }

    override fun openWeblink(url: String) {
        NSURL.URLWithString(url)?.let { openUrl(it) }
    }

    private fun openUrl(url: NSURL) {
        val app = UIApplication.sharedApplication()
        if (app.canOpenURL(url)) {
            app.openURL(
                url = url,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null
            )
        }
    }
}
