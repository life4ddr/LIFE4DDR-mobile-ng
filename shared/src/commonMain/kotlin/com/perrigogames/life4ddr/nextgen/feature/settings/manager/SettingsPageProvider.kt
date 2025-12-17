package com.perrigogames.life4ddr.nextgen.feature.settings.manager

import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.LadderSettings
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.feature.settings.view.SettingsPage
import com.perrigogames.life4ddr.nextgen.feature.settings.view.SettingsPageModal
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsItem
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings.Companion.KEY_ENABLE_DIFFICULTY_TIERS
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.SongResultSettings.Companion.KEY_SHOW_REMOVED_SONGS
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.perrigogames.life4ddr.nextgen.util.formatRivalCode
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
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
    private val flowSettings: FlowSettings by inject()
    private val userInfoSettings: UserInfoSettings by inject()
    private val ladderSettings: LadderSettings by inject()
    private val songResultSettings: SongResultSettings by inject()

    private val difficultyTierFlow = songResultSettings.enableDifficultyTiers
    private val removedSongsFlow = songResultSettings.showRemovedSongs

    fun getRootPage(isDebug: Boolean): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.tab_settings.desc(),
            settingsItems = listOfNotNull(
                UISettingsItem.Link( // Edit User Info
                    key = KEY_NAV_USER_INFO,
                    title = MR.strings.edit_user_info.desc(),
                    action = SettingsAction.Navigate(SettingsPage.EDIT_USER_INFO)
                ),
                UISettingsItem.Link( // Song List Settings
                    key = KEY_NAV_SONG_LIST,
                    title = MR.strings.song_list_settings.desc(),
                    action = SettingsAction.Navigate(SettingsPage.SONG_LIST_SETTINGS)
                ),
                UISettingsItem.Link( // Trial Settings
                    key = KEY_NAV_TRIALS,
                    title = MR.strings.trial_settings.desc(),
                    action = SettingsAction.Navigate(SettingsPage.TRIAL_SETTINGS)
                ),
                UISettingsItem.Link( // Sanbai Settings
                    key = KEY_NAV_SANBAI,
                    title = MR.strings.sanbai_settings.desc(),
                    action = SettingsAction.Navigate(SettingsPage.SANBAI_SETTINGS)
                ),
                UISettingsItem.Link( // Clear Data
                    key = KEY_NAV_CLEAR_DATA,
                    title = MR.strings.clear_data.desc(),
                    action = SettingsAction.Navigate(SettingsPage.CLEAR_DATA)
                ),
                if (isDebug) {
                    UISettingsItem.Link( // Debug Options
                        key = KEY_NAV_DEBUG,
                        title = StringDesc.Raw("Debug Options"),
                        action = SettingsAction.Navigate(SettingsPage.DEBUG)
                    )
                } else {
                    null
                },
                UISettingsItem.Divider,
                UISettingsItem.Header( // Help and Feedback
                    key = "HEADER_HELP",
                    title = MR.strings.help_and_feedback.desc()
                ),
                UISettingsItem.Link( // Shop LIFE4
                    key = "KEY_LINK_SHOP_LIFE4",
                    title = MR.strings.action_shop_life4.desc(),
                    subtitle = MR.strings.description_shop_life4.desc(),
                    action = SettingsAction.WebLink(URL_SHOP_LIFE4)
                ),
                UISettingsItem.Link( // Shop Dangershark
                    key = "KEY_LINK_SHOP_DANGERSHARK",
                    title = MR.strings.action_shop_dangershark.desc(),
                    subtitle = MR.strings.description_shop_dangershark.desc(),
                    action = SettingsAction.WebLink(URL_SHOP_DANGERSHARK)
                ),
                UISettingsItem.Link( // Discord Link
                    key = "KEY_LINK_DISCORD",
                    title = MR.strings.join_discord.desc(),
                    action = SettingsAction.WebLink(URL_JOIN_DISCORD)
                ),
                UISettingsItem.Link( // X Link
                    key = "KEY_LINK_X",
                    title = MR.strings.find_us_on_x.desc(),
                    action = SettingsAction.WebLink(URL_FIND_US_ON_X)
                ),
                UISettingsItem.Link( // Credits
                    key = "KEY_LINK_CREDITS",
                    title = MR.strings.credits.desc(),
                    action = SettingsAction.ShowCredits
                ),
                UISettingsItem.Link( // Version String
                    key = "KEY_LINK_VERSIONS",
                    title = StringDesc.Raw("Version ${appInfo.version}"),
                    action = SettingsAction.Modal(SettingsPageModal.AppVersion)
                ),
                // TODO support link
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
                    subtitle = rivalCode.formatRivalCode().desc(),
                    initialValue = rivalCode,
                    transform = { it.formatRivalCode() }
                ),
                UISettingsItem.Link( // Game version
                    key = LadderSettings.KEY_GAME_VERSION,
                    title = MR.strings.action_game_version.desc(),
                    subtitle = gameVersion.printName.desc(),
                    action = SettingsAction.Modal(SettingsPageModal.GameVersion)
                ),
            )
        )
    }

    fun getSongListPage(): Flow<UISettingsData> = combine(
        songResultSettings.enableDifficultyTiers,
        songResultSettings.showRemovedSongs,
    ) { diffTierEnabled, showRemovedSongs, ->
        UISettingsData(
            screenTitle = MR.strings.song_list_settings.desc(),
            settingsItems = listOf(
                UISettingsItem.Checkbox( // Enable Difficulty Tiers
                    key = KEY_ENABLE_DIFFICULTY_TIERS,
                    title = MR.strings.enable_difficulty_tiers.desc(),
                    action = SettingsAction.SetBoolean(KEY_ENABLE_DIFFICULTY_TIERS, !diffTierEnabled),
                    toggled = diffTierEnabled
                ),
                UISettingsItem.Checkbox(
                    key = KEY_SHOW_REMOVED_SONGS,
                    title = MR.strings.show_removed_songs.desc(),
                    action = SettingsAction.SetBoolean(KEY_SHOW_REMOVED_SONGS, !showRemovedSongs),
                    toggled = showRemovedSongs
                ),
            )
        )
    }

    fun getTrialPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.trial_settings.desc(),
            settingsItems = listOf()
        )
    )

    fun getSanbaiPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.sanbai_settings.desc(),
            settingsItems = listOf(
                UISettingsItem.Link(
                    key = "KEY_REFRESH_SANBAI_LIBRARY",
                    title = MR.strings.refresh_sanbai_library_data.desc(),
                    action = SettingsAction.Sanbai.RefreshLibrary
                ),
                UISettingsItem.Link(
                    key = "KEY_REFRESH_SANBAI_SCORES",
                    title = MR.strings.refresh_sanbai_user_scores.desc(),
                    action = SettingsAction.Sanbai.RefreshUserScores
                )
            )
        )
    )

    fun getClearDataPage(): Flow<UISettingsData> = flowOf(
        UISettingsData(
            screenTitle = MR.strings.clear_data.desc(),
            settingsItems = listOf()
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
                )
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
        private const val URL_FIND_US_ON_X = "https://x.com/life4ddr/"
        private const val URL_JOIN_DISCORD = "https://discord.gg/sTYjWNn"
    }
}