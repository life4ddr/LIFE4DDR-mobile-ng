package com.perrigogames.life4ddr.nextgen

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME

fun main() = application {
    initKoin(
        makeNativeModule(
            appInfo = object : AppInfo {
                override val appId: String get() = "LIFE4Desktop"
                override val isDebug: Boolean get() = true
                override val version: String get() = "0.1"
            },
            motdReader = JvmDataReader(MR.files.motd_json.filePath, MOTD_FILE_NAME),
            placementsReader = JvmUncachedDataReader(MR.files.placements_json.filePath),
            ranksReader = JvmDataReader(MR.files.ranks_json.filePath, RANKS_FILE_NAME),
            songsReader = JvmDataReader(MR.files.songs_json.filePath, SONGS_FILE_NAME),
            trialsReader = JvmDataReader(MR.files.trials_json.filePath, TRIALS_FILE_NAME),
        )
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "LIFE4DDR",
    ) {
        LIFE4App()
    }
}