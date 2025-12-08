package com.perrigogames.life4ddr.nextgen

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.FirstRunDestination
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel.LaunchViewModel
import com.perrigogames.life4ddr.nextgen.navigation.RootNavHost
import com.perrigogames.life4ddr.nextgen.navigation.firstRunNavigation
import com.perrigogames.life4ddr.nextgen.navigation.ladderNavigation
import com.perrigogames.life4ddr.nextgen.navigation.popAndNavigate
import com.perrigogames.life4ddr.nextgen.navigation.settingsNavigation
import com.perrigogames.life4ddr.nextgen.navigation.trialNavigation
import dev.icerock.moko.mvvm.createViewModelFactory
import kotlinx.coroutines.flow.first
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
            RootNavHost(
                modifier = Modifier.fillMaxSize(),
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
}
