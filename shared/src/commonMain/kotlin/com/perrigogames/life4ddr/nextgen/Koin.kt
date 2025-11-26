package com.perrigogames.life4ddr.nextgen

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.perrigogames.life4ddr.nextgen.api.DefaultGithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.DefaultBannerManager
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DefaultDeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.DefaultFirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalMapper
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRemoteData
import com.perrigogames.life4ddr.nextgen.feature.ladder.db.GoalDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.GoalStateManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderDataManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderGoalProgressManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.DefaultLadderSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MotdLocalRemoteData
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.DefaultMotdManager
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.DefaultMotdSettings
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdManager
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdSettings
import com.perrigogames.life4ddr.nextgen.feature.placements.manager.PlacementManager
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.DefaultUserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.DefaultUserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.DefaultSanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.DefaultSanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.DefaultSanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsPageProvider
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.SongListRemoteData
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.DefaultSongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.db.ResultDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.DefaultSongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialRemoteData
import com.perrigogames.life4ddr.nextgen.feature.trials.db.TrialDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.DefaultTrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.DefaultTrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.model.MajorUpdateManager
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

typealias NativeInjectionFactory<T> = Scope.() -> T

fun initKoin(
    appModule: Module,
    extraAppModule: Module? = null,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(listOfNotNull(appModule, extraAppModule, platformModule, coreModule))
}.apply {
    // doOnStartup is a lambda which is implemented in Swift on iOS side
//    koin.get<() -> Unit>().invoke()
    koin.get<Logger> { parametersOf(null) }.also { kermit ->
        kermit.v { "App Id ${koin.get<AppInfo>().appId}" }
    }
}

val coreModule = module {
    single { GoalDatabaseHelper(get()) }
    single { ResultDatabaseHelper(get()) }
    single { TrialDatabaseHelper(get()) }

    single<GithubDataAPI> { DefaultGithubDataAPI() }
    single<SanbaiAPI> { DefaultSanbaiAPI() }
    single { Json { classDiscriminator = "t" } }

    single { PlacementManager() }
    single { MajorUpdateManager() }
    single<MotdManager> { DefaultMotdManager() }
    single { LadderDataManager() }
    single { SongResultsManager() }
    single { LadderGoalProgressManager() }
    single<TrialDataManager> { DefaultTrialDataManager() }
    single<TrialRecordsManager> { DefaultTrialRecordsManager() }
    single<SongDataManager> { DefaultSongDataManager() }
    single { ChartResultOrganizer() }
    single<UserInfoSettings> { DefaultUserInfoSettings() }
    single<FirstRunSettings> { DefaultFirstRunSettings() }
    single<SongResultSettings> { DefaultSongResultSettings() }
    single<UserRankSettings> { DefaultUserRankSettings() }
    single<LadderSettings> { DefaultLadderSettings() }
    single { SettingsPageProvider() }
    single { GoalStateManager() }
    single { LadderGoalMapper() }
    single<DeeplinkManager> { DefaultDeeplinkManager() }
    single<SanbaiAPISettings> { DefaultSanbaiAPISettings() }
    single<SanbaiManager> { DefaultSanbaiManager() }
    single<MotdSettings> { DefaultMotdSettings() }
    single<BannerManager> { DefaultBannerManager() }

    // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
    // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
    // that would likely go into platformModule expect/actual.
    // See https://github.com/touchlab/Kermit
    val baseLogger = Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "LIFE4")
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }

expect val platformModule: Module

fun makeNativeModule(
    appInfo: AppInfo,
    motdReader: LocalDataReader,
    placementsReader: LocalUncachedDataReader,
    ranksReader: LocalDataReader,
    songsReader: LocalDataReader,
    trialsReader: LocalDataReader,
    additionalItems: Module.() -> Unit = {},
): Module {
    return module {
        single { appInfo }
        single(named(GithubDataAPI.MOTD_FILE_NAME)) { motdReader }
        single(named(GithubDataAPI.PLACEMENTS_FILE_NAME)) { placementsReader }
        single(named(GithubDataAPI.RANKS_FILE_NAME)) { ranksReader }
        single(named(GithubDataAPI.SONGS_FILE_NAME)) { songsReader }
        single(named(GithubDataAPI.TRIALS_FILE_NAME)) { trialsReader }
        single { LadderRemoteData() }
        single { MotdLocalRemoteData() }
        single { SongListRemoteData() }
        single { TrialRemoteData() }
        additionalItems()
    }
}
