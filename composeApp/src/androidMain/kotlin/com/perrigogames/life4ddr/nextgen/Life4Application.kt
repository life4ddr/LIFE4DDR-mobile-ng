package com.perrigogames.life4ddr.nextgen

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import okio.Path.Companion.toPath
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
                motdReader = AndroidDataReader(MR.files.motd_json, MOTD_FILE_NAME),
                placementsReader = AndroidUncachedDataReader(MR.files.placements_json),
                ranksReader = AndroidDataReader(MR.files.ranks_json, RANKS_FILE_NAME),
                songsReader = AndroidDataReader(MR.files.songs_json, SONGS_FILE_NAME),
                trialsReader = AndroidDataReader(MR.files.trials_json, TRIALS_FILE_NAME),
            ) {
                single<Context> { this@Life4Application }
                    single<DataStore<Preferences>> {
                        PreferenceDataStoreFactory.createWithPath(
                            produceFile = { filesDir.resolve("life4.preferences_pb").absolutePath.toPath() }
                        )
                    }
                    single<FlowSettings> { DataStoreSettings(get()) }
            },
        )
    }
}

object AndroidAppInfo : AppInfo {
    override val appId: String = BuildConfig.APPLICATION_ID
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val version: String = BuildConfig.VERSION_NAME
}
