package com.perrigogames.life4ddr.nextgen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.io.File

fun main() = application {
    addTempDirectoryRemovalHook()
    initKoin(
        makeNativeModule(
            appInfo = object : AppInfo {
                override val appId: String get() = "LIFE4Desktop"
                override val isDebug: Boolean get() = true
                override val version: String get() = "0.1"
                override val platform: PlatformType get() = PlatformType.DESKTOP
            },
            motdReader = JvmDataReader(MR.files.motd_json.readText(), File("../desktopData/$MOTD_FILE_NAME")),
            placementsReader = JvmUncachedDataReader(MR.files.placements_json.readText()),
            ranksReader = JvmDataReader(MR.files.ranks_json.readText(), File("../desktopData/$RANKS_FILE_NAME")),
            songsReader = JvmDataReader(MR.files.songs_json.readText(), File("../desktopData/$SONGS_FILE_NAME")),
            trialsReader = JvmDataReader(MR.files.trials_json.readText(), File("../desktopData/$TRIALS_FILE_NAME")),
        ) {
            single<DataStore<Preferences>> {
                PreferenceDataStoreFactory.createWithPath(
                    produceFile = {
                        File("../desktopData", "life4.preferences_pb").absolutePath.toPath()
                    }
                )
            }
            single<FlowSettings> { DataStoreSettings(get()) }
        },
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "LIFE4DDR",
    ) {
        var restartRequired by remember { mutableStateOf(false) }
        var initialized by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(-1F) }
        val bundleLocation = File("../desktopData")

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { // IO scope recommended but not required
                KCEF.init(
                    builder = {
                        installDir(File(bundleLocation, "kcef-bundle")) // recommended, but not necessary
                        progress {
                            onDownloading { downloadProgress = it }
                            onInitialized { initialized = true }
                        }
                    },
                    onError = { it?.printStackTrace() },
                    onRestartRequired = { restartRequired = true }
                )
            }
        }

        Box {
            LIFE4App(
                onExit = ::exitApplication
            )

            when {
                restartRequired -> {
                    SystemText("Restart required.")
                }
                !initialized -> {
                    SystemText("Downloading ${downloadProgress.toInt()}%")
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}

@Composable
private fun BoxScope.SystemText(
    text: String
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier
            .align(Alignment.Center)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    )
}
