package com.perrigogames.life4ddr.nextgen

import androidx.compose.ui.window.ComposeUIViewController
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import dev.icerock.moko.resources.FileResource
import platform.Foundation.NSFileManager

fun MainViewController() = ComposeUIViewController {
    initKoin(
        appModule = makeNativeModule(
            appInfo = object : AppInfo {
                override val appId: String get() = "LIFE4iOS"
                override val isDebug: Boolean get() = true
                override val version: String get() = "0.1"
            },
            motdReader = IosDataReader(MR.files.motd_json, MOTD_FILE_NAME),
            placementsReader = IosUncachedDataReader(MR.files.placements_json),
            ranksReader = IosDataReader(MR.files.ranks_json, RANKS_FILE_NAME),
            songsReader = IosDataReader(MR.files.songs_json, SONGS_FILE_NAME),
            trialsReader = IosDataReader(MR.files.trials_json, TRIALS_FILE_NAME),
        ),
        extraAppModule = platformSettingsModule {
//            // Get the documents directory URL
//            guard let documentsDirectory = NSFileManager.default.urls(for: .documentDirectory,
//                in: .userDomainMask).first else {
//            return
//        }
//
//            // Create file URL
//            let fileURL = documentsDirectory.appendingPathComponent("myFile.txt")
//
//            // Create file content
//            let content = "Hello, iOS file system!"
//
//            // Write to file
//            do {
//                try content.write(to: fileURL, atomically: true, encoding: .utf8)
//                    print("File created at: \(fileURL)")
//                } catch {
//                    print("Error creating file: \(error)")
//                }
            "FIXME"
            },
    )
    
    LIFE4App()
}

open class IosUncachedDataReader(
    val fileResource: FileResource
): LocalUncachedDataReader {

    override fun loadInternalString(): String {
        return fileResource.readText()
    }
}

class IosDataReader(
    fileResource: FileResource,
    val cachedFileName: String
): IosUncachedDataReader(
    fileResource = fileResource
), LocalDataReader {
    override fun loadCachedString(): String? {
        return readFromFile(path = cachedFileName)
    }

    override fun saveCachedString(data: String): Boolean {
        return saveToFile(path = cachedFileName, content = data)
    }

    override fun deleteCachedString(): Boolean {
        val url = URL.documentsDirectory.appending(path = cachedFileName)
        try {
            NSFileManager.default.removeItem(at = url)
                return true
        } catch(e: Exception) {
            print(e.message)
            return false
        }
    }
}

fun readFromFile(path: String): String? {
    val url = URL.documentsDirectory.appending(path: path)
    var ret: String?
    try {
        val data = Data(contentsOf = url)
        ret = String(data = data, encoding = .utf8)
    } catch {
        print(error.localizedDescription)
    }
    return ret
}

fun saveToFile(path: String, content: String): Boolean {
    val data = Data(content.utf8)
    val url = URL.documentsDirectory.appending(path: path)
    try {
        data.write(to = url, options = [.atomic, .completeFileProtection])
            return true
    } catch {
        print(error.localizedDescription)
        return false
    }
}

