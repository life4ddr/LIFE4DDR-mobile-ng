package com.perrigogames.life4ddr.nextgen

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.navigation.RootNavHost
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The first launched activity, determines the path through the startup flow that should be taken
 * based on the current save state.
 */
class LaunchActivity: AppCompatActivity(), KoinComponent {

    private val deeplinkManager: DeeplinkManager by inject()

    private var loaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !loaded }

        intent?.data?.let { intentUri ->
            deeplinkManager.processDeeplink(intentUri.toString())
        }

        super.onCreate(savedInstanceState)
        setContent {
            LIFE4App(
                onLoaded = { loaded = true }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        println(uri)
        // Extract the authorization code from the URI and exchange it for an access token
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("loaded", loaded)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        loaded = savedInstanceState.getBoolean("loaded", false)
    }
}
