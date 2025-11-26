package com.perrigogames.life4ddr.nextgen

import android.app.Application
import android.content.Context
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import org.koin.core.component.KoinComponent

class Life4Application: Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            defaultHandler!!.uncaughtException(thread, exception)
        }

        initKoin(
            makeNativeModule(
                appInfo = AndroidAppInfo,
                motdReader = AndroidDataReader(MR.files.motd_json.rawResId, MOTD_FILE_NAME),
                placementsReader = AndroidUncachedDataReader(MR.files.placements_json.rawResId),
                ranksReader = AndroidDataReader(MR.files.ranks_json.rawResId, RANKS_FILE_NAME),
                songsReader = AndroidDataReader(MR.files.songs_json.rawResId, SONGS_FILE_NAME),
                trialsReader = AndroidDataReader(MR.files.trials_json.rawResId, TRIALS_FILE_NAME),
            ) {
                single<Context> { this@Life4Application }
            }
        )
    }
}

//object AndroidAppInfo : AppInfo {
//    override val appId: String = BuildConfig.LIBRARY_PACKAGE_NAME // FIXME
//    override val isDebug: Boolean = BuildConfig.DEBUG // FIXME
//    override val version: String = BuildConfig.VERSION_NAME // FIXME
//}

object AndroidAppInfo : AppInfo {
    override val appId: String = "LIFE4DDR" // FIXME
    override val isDebug: Boolean = true // FIXME
    override val version: String = "<not specified>" // FIXME
}
