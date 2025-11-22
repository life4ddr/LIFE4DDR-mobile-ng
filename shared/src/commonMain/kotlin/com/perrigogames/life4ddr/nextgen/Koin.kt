package com.perrigogames.life4ddr.nextgen

import com.perrigogames.life4ddr.nextgen.feature.ladder.db.GoalDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.songresults.db.ResultDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.trials.db.TrialDatabaseHelper
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

// TODO Ktor
// TODO Logger

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
    // AppInfo is a Kotlin interface with separate Android and iOS implementations
//    koin.get<Logger> { parametersOf(null) }.also { kermit ->
//        kermit.v { "App Id ${koin.get<AppInfo>().appId}" }
//    }
}

val coreModule = module {
    single { GoalDatabaseHelper(get()) }
    single { ResultDatabaseHelper(get()) }
    single { TrialDatabaseHelper(get()) }

//    single<GithubDataAPI> { GithubDataImpl() }
//    single<SanbaiAPI> { SanbaiAPIImpl() }
    single { Json { classDiscriminator = "t" } }

//    single { PlacementManager() }
//    single { MajorUpdateManager() }
//    single<MotdManager> { DefaultMotdManager() }
//    single { LadderDataManager() }
//    single { SongResultsManager() }
//    single { LadderGoalProgressManager() }
//    single<TrialDataManager> { DefaultTrialDataManager() }
//    single<TrialRecordsManager> { DefaultTrialRecordsManager() }
//    single<SongDataManager> { DefaultSongDataManager() }
//    single { ChartResultOrganizer() }
//    single { UserInfoSettings() }
//    single { FirstRunSettingsManager() }
//    single { SongResultSettings() }
//    single { UserRankSettings() }
//    single { LadderSettings() }
//    single { SettingsPageProvider() }
//    single<UserRankManager> { DefaultUserRankManager() }
//    single { GoalStateManager() }
//    single { LadderGoalMapper() }
//    single<IDeeplinkManager> { DeeplinkManager() }
//    single<ISanbaiAPISettings> { SanbaiAPISettings() }
//    single<ISanbaiManager> { SanbaiManager() }
//    single<MotdSettings> { DefaultMotdSettings() }
//    single<IBannerManager> { BannerManager() }

    // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
    // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
    // that would likely go into platformModule expect/actual.
    // See https://github.com/touchlab/Kermit
//    val baseLogger = Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "LIFE4")
//    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }
}

// Simple function to clean up the syntax a bit
//fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }

expect val platformModule: Module

//fun makeNativeModule(
//    appInfo: AppInfo,
//    motdReader: LocalDataReader,
//    partialDifficultyReader: LocalDataReader,
//    placementsReader: LocalUncachedDataReader,
//    ranksReader: LocalDataReader,
//    songsReader: LocalDataReader,
//    trialsReader: LocalDataReader,
//    additionalItems: Module.() -> Unit = {},
//): Module {
//    return module {
//        single { appInfo }
//        single(named(GithubDataAPI.MOTD_FILE_NAME)) { motdReader }
//        single(named(GithubDataAPI.PARTIAL_DIFFICULTY_FILE_NAME)) { partialDifficultyReader }
//        single(named(GithubDataAPI.PLACEMENTS_FILE_NAME)) { placementsReader }
//        single(named(GithubDataAPI.RANKS_FILE_NAME)) { ranksReader }
//        single(named(GithubDataAPI.SONGS_FILE_NAME)) { songsReader }
//        single(named(GithubDataAPI.TRIALS_FILE_NAME)) { trialsReader }
//        single { LadderRemoteData() }
//        single { MotdLocalRemoteData() }
//        single { SongListRemoteData() }
//        single { TrialRemoteData() }
//        additionalItems()
//    }
//}
