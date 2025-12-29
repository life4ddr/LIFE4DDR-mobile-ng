package com.perrigogames.life4ddr.nextgen

import androidx.compose.ui.window.ComposeUIViewController
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.icerock.moko.resources.FileResource
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults

@OptIn(ExperimentalSettingsApi::class)
fun MainViewController(
    defaults: NSUserDefaults,
) = ComposeUIViewController {
    initKoin(
        appModule = makeNativeModule(
            appInfo = object : AppInfo {
                override val appId: String get() = "LIFE4iOS"
                override val isDebug: Boolean get() = true
                override val version: String get() = "0.1"
            },
            motdReader = IosDataReader(MR.files.motd_json, MOTD_FILE_NAME),
            placementsReader = FileResourceDataReader(MR.files.placements_json),
            ranksReader = IosDataReader(MR.files.ranks_json, RANKS_FILE_NAME),
            songsReader = IosDataReader(MR.files.songs_json, SONGS_FILE_NAME),
            trialsReader = IosDataReader(MR.files.trials_json, TRIALS_FILE_NAME),
        ) {
            val settings = NSUserDefaultsSettings(defaults)
            single<Settings> { settings }
            single<FlowSettings> { settings.toFlowSettings() }
        },
    )
    
    LIFE4App()
}

open class FileResourceDataReader(val fileResource: FileResource) : LocalUncachedDataReader {
    override fun loadInternalString(): String {
        return fileResource.readText()
    }
}

class IosDataReader(
    fileResource: FileResource,
    val cachedFileName: String
): FileResourceDataReader(
    fileResource = fileResource
), LocalDataReader {
    override fun loadCachedString(): String? {
        return readFromFile(path = cachedFileName)
    }

    override fun saveNewCache(data: String): Boolean {
        return saveToFile(path = cachedFileName, content = data)
    }

    override fun deleteCache(): Boolean {
        return false // FIXME
//        val url = URL.documentsDirectory.appending(path = cachedFileName)
//        try {
//            NSFileManager.default.removeItem(at = url)
//                return true
//        } catch(e: Exception) {
//            print(e.message)
//            return false
//        }
    }
}

fun readFromFile(path: String): String? {
    return null // FIXME
//    val url = URL.documentsDirectory.appending(path: path)
//    var ret: String?
//    try {
//        val data = Data(contentsOf = url)
//        ret = String(data = data, encoding = .utf8)
//    } catch {
//        print(error.localizedDescription)
//    }
//    return ret
}

fun saveToFile(path: String, content: String): Boolean {
    return false // FIXME
//    val data = Data(content.utf8)
//    val url = URL.documentsDirectory.appending(path: path)
//    try {
//        data.write(to = url, options = [.atomic, .completeFileProtection])
//            return true
//    } catch {
//        print(error.localizedDescription)
//        return false
//    }
}
