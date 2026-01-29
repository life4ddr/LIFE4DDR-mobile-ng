package com.perrigogames.life4ddr.nextgen.feature.settings.manager

import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.enums.GameVersion
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MASettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.settings.view.SettingsPage
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsItem
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterPanelSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialSettings
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.perrigogames.life4ddr.nextgen.util.formatRivalCode
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.inject

@OptIn(ExperimentalSettingsApi::class)
class SettingsPageProvider : BaseModel() {

    private val appInfo: AppInfo by inject()
    private val filterSettings: FilterPanelSettings by inject()
    private val userInfoSettings: UserInfoSettings by inject()
    private val ladderSettings: LadderSettings by inject()
    private val trialSettings: TrialSettings by inject()
    private val maSettings: MASettings by inject()
    private val songResultSettings: SongResultSettings by inject()

    fun getRootPage(isDebug: Boolean): Flow<UISettingsData> = flowOf(
        UISettingsData(
            isRoot = true,
            screenTitle = MR.strings.action_settings.desc(),
            settingsItems = listOfNotNull(
                UISettingsItem.Link(
                    key = KEY_NAV_USER_INFO,
                    title = MR.strings.edit_user_info.desc(),
                    icon = MR.images.person,
                    action = SettingsAction.Navigate(SettingsPage.EDIT_USER_INFO)
                ),
                UISettingsItem.Link(
                    key = KEY_NAV_SONG_LIST,
                    title = MR.strings.song_list_settings.desc(),
                    icon = MR.images.list,
                    action = SettingsAction.Navigate(SettingsPage.SONG_LIST_SETTINGS)
                ),
                UISettingsItem.Link(
                    key = KEY_NAV_TRIALS,
                    title = MR.strings.trial_settings.desc(),
                    icon = MR.images.trophy,
                    action = SettingsAction.Navigate(SettingsPage.TRIAL_SETTINGS)
                ),
                UISettingsItem.Link(
                    key = KEY_NAV_SANBAI,
                    title = MR.strings.sanbai_settings.desc(),
                    icon = MR.images.icecream,
                    action = SettingsAction.Navigate(SettingsPage.SANBAI_SETTINGS)
                ),
                UISettingsItem.Link(
                    key = KEY_NAV_CLEAR_DATA,
                    title = MR.strings.clear_data.desc(),
                    icon = MR.images.delete,
                    action = SettingsAction.Navigate(SettingsPage.CLEAR_DATA)
                ),
                if (isDebug) {
                    UISettingsItem.Link(
                        key = KEY_NAV_DEBUG,
                        title = StringDesc.Raw("Debug Options"),
                        icon = MR.images.terminal,
                        action = SettingsAction.Navigate(SettingsPage.DEBUG)
                    )
                } else {
                    null
                },
                UISettingsItem.Divider,
                UISettingsItem.Header(
                    key = "HEADER_HELP",
                    title = MR.strings.help_and_feedback.desc()
                ),
                UISettingsItem.Link(
                    key = "KEY_LINK_SHOP_LIFE4",
                    title = MR.strings.action_shop_life4.desc(),
                    subtitle = MR.strings.description_shop_life4.desc(),
                    icon = MR.images.shopping_cart,
                    action = SettingsAction.WebLink(URL_SHOP_LIFE4)
                ),
                UISettingsItem.Link(
                    key = "KEY_LINK_SHOP_DANGERSHARK",
                    title = MR.strings.action_shop_dangershark.desc(),
                    subtitle = MR.strings.description_shop_dangershark.desc(),
                    icon = MR.images.shopping_cart,
                    action = SettingsAction.WebLink(URL_SHOP_DANGERSHARK)
                ),
                UISettingsItem.Link(
                    key = "KEY_LINK_DISCORD",
                    title = MR.strings.join_discord.desc(),
                    icon = MR.images.brand_discord,
                    action = SettingsAction.WebLink(URL_JOIN_DISCORD)
                ),
                UISettingsItem.Link(
                    key = "KEY_LINK_X",
                    title = MR.strings.find_us_on_bluesky.desc(),
                    icon = MR.images.brand_bluesky,
                    action = SettingsAction.WebLink(URL_FIND_US_ON_BLUESKY)
                ),
                // NOSHIP Credits are required for full app launch
//                UISettingsItem.Link(
//                    key = "KEY_LINK_CREDITS",
//                    title = MR.strings.credits.desc(),
//                    icon = MR.images.info,
//                    action = SettingsAction.ShowCredits
//                ),
                UISettingsItem.Link(
                    key = "KEY_EMAIL_SUPPORT",
                    title = MR.strings.action_email_support.desc(),
                    icon = MR.images.help,
                    action = SettingsAction.Email(EMAIL_SUPPORT)
                ),
                UISettingsItem.Link(
                    key = "KEY_LINK_VERSIONS",
                    title = StringDesc.Raw("Version ${appInfo.version}"),
                    action = SettingsAction.None
                )
            )
        )
    )

    fun getEditUserPage(): Flow<UISettingsData> = combine(
        userInfoSettings.userName,
        userInfoSettings.rivalCode,
        ladderSettings.selectedGameVersion
    ) { userName, rivalCode, gameVersion ->
        UISettingsData(
            screenTitle = MR.strings.edit_user_info.desc(),
            settingsItems = listOf(
                UISettingsItem.Text( // Player name
                    key = UserInfoSettings.KEY_INFO_NAME,
                    title = MR.strings.action_name.desc(),
                    subtitle = userName.desc(),
                    initialValue = userName,
                ),
                UISettingsItem.Text( // Rival code
                    key = UserInfoSettings.KEY_INFO_RIVAL_CODE,
                    title = MR.strings.action_rival_code.desc(),
                    subtitle = rivalCode.formatRivalCode().let { formatted ->
                        if (formatted.isEmpty()) {
                            MR.strings.not_set.desc()
                        } else {
                            formatted.desc()
                        }
                    },
                    initialValue = rivalCode,
                    transform = { it.formatRivalCode() }
                ),
                UISettingsItem.Dropdown( // Game version
                    key = LadderSettings.KEY_GAME_VERSION,
                    title = MR.strings.action_game_version.desc(),
                    subtitle = gameVersion.printName.desc(),
                    dropdownItems = GameConstants.SUPPORTED_VERSIONS,
                    selectedIndex = GameConstants.SUPPORTED_VERSIONS.indexOfFirst { it == gameVersion },
                    createText = { (it as GameVersion).printName },
                    createAction = { SettingsAction.SetGameVersion(it as GameVersion) }
                ),
            )
        )
    }

    fun getSongListPage(): Flow<UISettingsData> = combine(
        songResultSettings.enableDifficultyTiers,
        songResultSettings.showRemovedSongs,
        filterSettings.filterFlags,
        maSettings.maConfig,
        combine(
            ladderSettings.useMonospaceScore,
            ladderSettings.hideCompletedGoals
        ) { a, b -> a to b }
    ) { diffTierEnabled, showRemovedSongs, filterFlags, maConfig, (useMonospaceScore, hideCompletedGoals) ->
        UISettingsData(
            screenTitle = MR.strings.song_list_settings.desc(),
            settingsItems = listOf(
                UISettingsItem.Checkbox(
                    key = SongResultSettings.KEY_ENABLE_DIFFICULTY_TIERS,
                    title = MR.strings.action_enable_difficulty_tiers.desc(),
                    subtitle = if (diffTierEnabled) {
                        MR.strings.action_enable_difficulty_tiers_subtitle_enabled.desc()
                    } else {
                        MR.strings.action_enable_difficulty_tiers_subtitle_disabled.desc()
                    },
                    toggled = diffTierEnabled
                ),
                UISettingsItem.Checkbox(
                    key = LadderSettings.KEY_HIDE_COMPLETED_GOALS,
                    title = MR.strings.action_profile_hide_completed.desc(),
                    toggled = hideCompletedGoals
                ),
                // TODO this isn't currently used anywhere
//                UISettingsItem.Checkbox(
//                    key = SongResultSettings.KEY_SHOW_REMOVED_SONGS,
//                    title = MR.strings.show_removed_songs.desc(),
//                    toggled = showRemovedSongs
//                ),
                UISettingsItem.Checkbox(
                    key = LadderSettings.KEY_USE_MONOSPACE_SCORE,
                    title = MR.strings.use_monospace_font.desc(),
                    toggled = useMonospaceScore
                ),
                UISettingsItem.Header(
                    key = "KEY_HEADER_FILTERS",
                    title = MR.strings.action_header_filters.desc()
                ),
                UISettingsItem.Checkbox(
                    key = FilterPanelSettings.KEY_FILTER_SHOW_DIFF_CLASSES,
                    title = MR.strings.action_filter_show_diff_class.desc(),
                    toggled = filterFlags.showDiffClasses,
                ),
                UISettingsItem.Checkbox(
                    key = FilterPanelSettings.KEY_FILTER_DIFFICULTY_RANGE,
                    title = MR.strings.action_show_difficulty_number_range.desc(),
                    toggled = filterFlags.useDifficultyRange,
                ),
                UISettingsItem.Header(
                    key = "KEY_HEADER_MA",
                    title = MR.strings.action_header_ma_points.desc()
                ),
                UISettingsItem.Checkbox(
                    key = MASettings.KEY_COMBINE_MFCS_GOALLIST,
                    title = MR.strings.action_combine_mfc.desc(),
                    subtitle = if (maConfig.combineMFCs) {
                        MR.strings.action_combine_mfc_subtitle_enabled.desc()
                    } else {
                        MR.strings.action_combine_mfc_subtitle_disabled.desc()
                    },
                    toggled = maConfig.combineMFCs,
                ),
                UISettingsItem.Checkbox(
                    key = MASettings.KEY_COMBINE_SDPS_GOALLIST,
                    title = MR.strings.action_combine_sdp.desc(),
                    subtitle = if (maConfig.combineSDPs) {
                        MR.strings.action_combine_sdp_subtitle_enabled.desc()
                    } else {
                        MR.strings.action_combine_sdp_subtitle_disabled.desc()
                    },
                    toggled = maConfig.combineSDPs,
                ),
            )
        )
    }

    fun getTrialPage(): Flow<UISettingsData> = combine(
        trialSettings.highlightNewFlow,
        trialSettings.highlightUnplayedFlow,
    ) { highlightNew, highlightUnplayed ->
        UISettingsData(
            screenTitle = MR.strings.trial_settings.desc(),
            settingsItems = listOf(
                UISettingsItem.Checkbox(
                    key = TrialSettings.KEY_TRIAL_LIST_HIGHLIGHT_NEW,
                    title = MR.strings.highlight_new_trials.desc(),
                    toggled = highlightNew
                ),
                UISettingsItem.Checkbox(
                    key = TrialSettings.KEY_TRIAL_LIST_HIGHLIGHT_UNPLAYED,
                    title = MR.strings.highlight_unplayed_trials.desc(),
                    toggled = highlightUnplayed
                ),
            )
        )
    }

    fun getSanbaiPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.sanbai_settings.desc(),
            settingsItems = listOf(
                UISettingsItem.Link(
                    key = "KEY_REFRESH_SANBAI_LIBRARY",
                    title = MR.strings.action_refresh_sanbai_library_data.desc(),
                    subtitle = MR.strings.action_refresh_sanbai_library_data_subtitle.desc(),
                    action = SettingsAction.Sanbai.RefreshLibrary
                ),
                UISettingsItem.Link(
                    key = "KEY_REFRESH_SANBAI_SCORES",
                    title = MR.strings.action_refresh_sanbai_user_scores.desc(),
                    subtitle = MR.strings.action_refresh_sanbai_user_scores_subtitle.desc(),
                    action = SettingsAction.Sanbai.RefreshUserScores
                )
            )
        )
    )

    fun getClearDataPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.clear_data.desc(),
            settingsItems = listOf(
                UISettingsItem.Link(
                    key = "KEY_CLEAR_SONG_DATA",
                    title = MR.strings.action_clear_result_data.desc(),
                    action = SettingsAction.ClearData.Results
                ),
                UISettingsItem.Link(
                    key = "KEY_CLEAR_TRIAL_DATA",
                    title = MR.strings.action_clear_trial_data.desc(),
                    action = SettingsAction.ClearData.Trials
                ),
                UISettingsItem.Link(
                    key = "KEY_CLEAR_ALL_DATA",
                    title = MR.strings.action_clear_all_data.desc(),
                    subtitle = MR.strings.action_clear_all_data_subtitle.desc(),
                    action = SettingsAction.ClearData.All
                ),
            )
        )
    )

    fun getDebugPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = StringDesc.Raw("Debug"),
            settingsItems = listOf(
                UISettingsItem.Link(
                    key = "KEY_NAV_LOCKED_SONGS",
                    title = MR.strings.debug_locked_songs.desc(),
                    action = SettingsAction.Debug.SongLockPage
                ),
                UISettingsItem.Link(
                    key = "KEY_LIFE4_FLARE_ALERT",
                    title = "Show LIFE4 Flare Alert".desc(),
                    action = SettingsAction.Debug.Life4FlareAlert,
                ),
                UISettingsItem.Link(
                    key = "KEY_TRIAL_SYNC_UNLOCK",
                    title = "Reset last Trial sync time".desc(),
                    action = SettingsAction.Debug.TrialSyncReset,
                ),
                UISettingsItem.Link(
                    key = "KEY_SANBAI_VALID",
                    title = "Sanbai Keys are ${if (SanbaiAPI.areKeysValid()) "valid" else "not valid!"}".desc(),
                    action = SettingsAction.None
                ),
            )
        )
    )

    companion object {
        private const val KEY_NAV_USER_INFO = "KEY_NAV_USER_INFO"
        private const val KEY_NAV_SONG_LIST = "KEY_NAV_SONG_LIST"
        private const val KEY_NAV_TRIALS = "KEY_NAV_TRIALS"
        private const val KEY_NAV_SANBAI = "KEY_NAV_SANBAI"
        private const val KEY_NAV_CLEAR_DATA = "KEY_NAV_CLEAR_DATA"
        private const val KEY_NAV_DEBUG = "KEY_NAV_DEBUG"

        private const val URL_SHOP_LIFE4 = "https://life4.bigcartel.com/"
        private const val URL_SHOP_DANGERSHARK = "https://www.etsy.com/shop/DangerShark/"
        private const val URL_FIND_US_ON_BLUESKY = "https://bsky.app/profile/life4ddrbot.bsky.social"
        private const val URL_JOIN_DISCORD = "https://discord.gg/sTYjWNn"
        private const val EMAIL_SUPPORT = "cperrigolife4@gmail.com"
    }
}