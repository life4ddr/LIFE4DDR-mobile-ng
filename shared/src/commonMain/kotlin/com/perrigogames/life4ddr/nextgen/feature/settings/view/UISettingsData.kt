package com.perrigogames.life4ddr.nextgen.feature.settings.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

/**
 * Describes the contents of the Settings screen.  A Flow of these items should
 * be expected, updating the content as the user navigates through it.
 */
data class UISettingsData(
    val screenTitle: StringDesc,
    val settingsItems: List<UISettingsItem>,
    val isRoot: Boolean = false,
)

/**
 * Describes a single clickable item in the Settings screen.
 */
sealed class UISettingsItem {

    abstract val key: String

    /**
     * A text header with no clickability.
     * @param title the text the header should display
     */
    data class Header(
        override val key: String,
        val title: StringDesc
    ) : UISettingsItem()

    /**
     * A link item that only consists of text. Usually performs a navigation action.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param action The [SettingsAction] to be taken when the checkbox item is clicked.
     * @param enabled Whether the item should be interactable. Defaults to true.
     */
    data class Link(
        override val key: String,
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val action: SettingsAction,
        val enabled: Boolean = true,
    ) : UISettingsItem()

    /**
     * A link item that only consists of text. Usually performs a navigation action.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param dropdownItems
     * @param enabled Whether the item should be interactable. Defaults to true.
     */
    data class Dropdown(
        override val key: String,
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val dropdownItems: List<Any>,
        val selectedIndex: Int,
        val createAction: (Any) -> SettingsAction,
        val createText: (Any) -> String,
    ) : UISettingsItem() {

        val currentItem get() = dropdownItems[selectedIndex]
    }

    /**
     * A checkbox item that controls a boolean flag in the settings.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param action The [SettingsAction] to be taken when the checkbox item is clicked.
     * @param enabled Whether the item should be interactable. Defaults to true.
     * @param toggled The current toggled state of checkbox. Defaults to false.
     */
    data class Checkbox(
        override val key: String,
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val enabled: Boolean = true,
        val toggled: Boolean = false,
    ) : UISettingsItem() {

        fun createAction(input: Boolean) = SettingsAction.SetBoolean(key, input)
    }

    /**
     * An editable text item that saves its value in shared settings.
     * @param title The main text to be shown.
     * @param subtitle An optional less emphasized text to show.
     * @param enabled Whether the item should be interactable. Defaults to true.
     * @param initialValue The initial data this text item contains.
     */
    data class Text(
        override val key: String,
        val title: StringDesc,
        val subtitle: StringDesc? = null,
        val enabled: Boolean = true,
        val initialValue: String,
        val transform: (String) -> String = { it },
    ) : UISettingsItem() {

        fun createAction(input: String) = SettingsAction.SetString(key, input)
    }

    /**
     * A divider object.
     */
    data object Divider : UISettingsItem() {
        override val key: String = "divider"
    }
}

/**
 * Enum describing the different pages available to the settings flow. Typically sent with
 * [SettingsAction]s.
 */
enum class SettingsPage(val nameDesc: StringDesc) {
    ROOT(StringDesc.Resource(MR.strings.action_settings)),
    EDIT_USER_INFO(StringDesc.Resource(MR.strings.edit_user_info)),
    SONG_LIST_SETTINGS(StringDesc.Resource(MR.strings.song_list_settings)),
    TRIAL_SETTINGS(StringDesc.Resource(MR.strings.trial_settings)),
    SANBAI_SETTINGS(StringDesc.Resource(MR.strings.sanbai_settings)),
    CLEAR_DATA(StringDesc.Resource(MR.strings.clear_data)),
    DEBUG(StringDesc.Raw("Debug"))
}

sealed class SettingsPageModal {
    data class Text(val key: String) : SettingsPageModal()
    data object RivalCode : SettingsPageModal()
    data object GameVersion : SettingsPageModal()
    data object AppVersion : SettingsPageModal()
}