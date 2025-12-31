package com.perrigogames.life4ddr.nextgen

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.perrigogames.life4ddr.nextgen.util.ExternalActions

class AndroidExternalActions(private val context: Context) : ExternalActions {

    override fun openEmail(email: String) {
        val emailUri = "mailto:$email".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, emailUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun openWeblink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
