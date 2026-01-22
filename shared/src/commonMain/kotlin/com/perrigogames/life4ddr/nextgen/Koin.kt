@file:OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)

package com.perrigogames.life4ddr.nextgen

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import com.perrigogames.life4ddr.nextgen.api.DefaultGithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.MOTD_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.PLACEMENTS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.RANKS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.SONGS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.GithubDataAPI.Companion.TRIALS_FILE_NAME
import com.perrigogames.life4ddr.nextgen.api.base.LocalDataReader
import com.perrigogames.life4ddr.nextgen.api.base.LocalUncachedDataReader
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.DefaultBannerManager
import com.perrigogames.life4ddr.nextgen.feature.banners.manager.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DefaultDeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.DefaultFirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.FirstRunSettings
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunInfoViewModel
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderGoalMapper
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRemoteData
import com.perrigogames.life4ddr.nextgen.feature.ladder.db.GoalDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.GoalStateManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderDataManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderGoalProgressManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.DefaultLadderSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.DefaultMASettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MASettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModel
import com.perrigogames.life4ddr.nextgen.feature.launch.viewmodel.LaunchViewModel
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MotdLocalRemoteData
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.DefaultMotdManager
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.DefaultMotdSettings
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdManager
import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdSettings
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertManager
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertSettings
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.DefaultAlertManager
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.DefaultAlertSettings
import com.perrigogames.life4ddr.nextgen.feature.placements.manager.PlacementManager
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementDetailsViewModel
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListViewModel
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.DefaultUserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.DefaultUserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserRankSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.MainScreenViewModel
import com.perrigogames.life4ddr.nextgen.feature.profile.viewmodel.PlayerProfileViewModel
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.DefaultSanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.DefaultSanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.DefaultSanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsPageProvider
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsViewModel
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.VersionsDialogViewModel
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.SongListRemoteData
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.DefaultSongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songlist.manager.SongDataManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.db.ResultDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.ChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.DefaultChartResultOrganizer
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.DefaultFilterPanelSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.DefaultSongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.DefaultSongResultsManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultsManager
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListViewModel
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialRemoteData
import com.perrigogames.life4ddr.nextgen.feature.trials.db.TrialDatabaseHelper
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.DefaultTrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.DefaultTrialListSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.DefaultTrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialListSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialRecordsManager
import com.perrigogames.life4ddr.nextgen.feature.trials.viewmodel.TrialListViewModel
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionViewModel
import com.perrigogames.life4ddr.nextgen.model.MajorUpdateManager
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

typealias NativeInjectionFactory<T> = Scope.() -> T

fun initKoin(
    appModule: Module,
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(listOfNotNull(appModule, platformModule, coreModule, loggerModule))
}.apply {
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

    single { LadderRemoteData(get(), get(), get(named(RANKS_FILE_NAME)), logger = get { parametersOf("LadderRemoteData") }) }
    single { MotdLocalRemoteData(get(), get(), get(named(MOTD_FILE_NAME)), logger = get { parametersOf("MotdLocalRemoteData") }) }
    single { SongListRemoteData(get(), get(), get(named(SONGS_FILE_NAME)), logger = get { parametersOf("SongListRemoteData") }) }
    single { TrialRemoteData(get(), get(), get(named(TRIALS_FILE_NAME)), logger = get { parametersOf("TrialRemoteData") }) }

    single { PlacementManager() }
    single { MajorUpdateManager() }
    single<MotdManager> { DefaultMotdManager() }
    single { LadderDataManager(get(), get()) }
    single<SongResultsManager> { DefaultSongResultsManager(get(), get(), getLogger("SongResultsManager")) }
    single { LadderGoalProgressManager(get()) }
    single<TrialDataManager> { DefaultTrialDataManager(get(), get(), get(), get(), getLogger("TrialDataManager")) }
    single<TrialRecordsManager> { DefaultTrialRecordsManager() }
    single<SongDataManager> { DefaultSongDataManager() }
    single<ChartResultOrganizer> { DefaultChartResultOrganizer(get()) }
    single<FilterPanelSettings> { DefaultFilterPanelSettings() }
    single<FirstRunSettings> { DefaultFirstRunSettings() }
    single<LadderSettings> { DefaultLadderSettings() }
    single<MASettings> { DefaultMASettings() }
    single<SongResultSettings> { DefaultSongResultSettings() }
    single<TrialListSettings> { DefaultTrialListSettings() }
    single<UserInfoSettings> { DefaultUserInfoSettings() }
    single<UserRankSettings> { DefaultUserRankSettings() }
    single<AlertSettings> { DefaultAlertSettings() }
    single { SettingsPageProvider() }
    single { GoalStateManager() }
    single { LadderGoalMapper() }
    single<DeeplinkManager> { DefaultDeeplinkManager() }
    single<SanbaiAPISettings> { DefaultSanbaiAPISettings() }
    single<SanbaiManager> { DefaultSanbaiManager() }
    single<MotdSettings> { DefaultMotdSettings() }
    single<BannerManager> { DefaultBannerManager() }
    single<AlertManager> { DefaultAlertManager(get()) }

    viewModel { LaunchViewModel(get()) }
    viewModel { FirstRunInfoViewModel(get(), get(), get(), getLogger("FirstRunInfoViewModel")) }
    viewModel { PlacementListViewModel(get(), get()) }
    viewModel { params -> PlacementDetailsViewModel(placementId = params.get(), get(), getLogger("PlacementDetailsViewModel")) }
    viewModel { params -> RankListViewModel(isFirstRun = params.get(), get(), get(), get()) }
    viewModel { MainScreenViewModel() }
    viewModel { PlayerProfileViewModel(get(), get(), get(), get()) }
    viewModel { ScoreListViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TrialListViewModel(get(), get(), get(), get()) }
    viewModel { params -> TrialSessionViewModel(trialId = params.get(), get(), get(), get(), getLogger("TrialSessionViewModel")) }
    viewModel { VersionsDialogViewModel() }
}

val loggerModule = module {
    // platformLogWriter() is a relatively simple config option, useful for local debugging. For production
    // uses you *may* want to have a more robust configuration from the native platform. In KaMP Kit,
    // that would likely go into platformModule expect/actual.
    // See https://github.com/touchlab/Kermit
    val baseLogger = Logger(config = StaticConfig(logWriterList = listOf(platformLogWriter())), "LIFE4")
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }
}

// Simple function to clean up the syntax a bit
fun KoinComponent.injectLogger(tag: String): Lazy<Logger> = inject { parametersOf(tag) }

fun Scope.getLogger(tag: String? = null): Logger = get { parametersOf(tag) }

expect val platformModule: Module

fun platformSettingsModule(producePath: (String) -> String) = module {
//    single<DataStore<Preferences>> {
//        PreferenceDataStoreFactory.createWithPath(
//            produceFile = { producePath("life4.preferences_pb").toPath() }
//        )
//    }
//    single<FlowSettings> { DataStoreSettings(get()) }
}

fun makeNativeModule(
    appInfo: AppInfo,
    motdReader: LocalDataReader,
    placementsReader: LocalUncachedDataReader,
    ranksReader: LocalDataReader,
    songsReader: LocalDataReader,
    trialsReader: LocalDataReader,
    additionalItems: Module.() -> Unit = {},
) = module {
    single { appInfo }
    single(named(MOTD_FILE_NAME)) { motdReader }
    single(named(PLACEMENTS_FILE_NAME)) { placementsReader }
    single(named(RANKS_FILE_NAME)) { ranksReader }
    single(named(SONGS_FILE_NAME)) { songsReader }
    single(named(TRIALS_FILE_NAME)) { trialsReader }
    additionalItems()
}
