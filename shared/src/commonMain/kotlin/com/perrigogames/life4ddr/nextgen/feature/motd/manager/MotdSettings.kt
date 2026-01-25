package com.perrigogames.life4ddr.nextgen.feature.motd.manager

import com.perrigogames.life4ddr.nextgen.feature.motd.manager.MotdSettings.Companion.KEY_LAST_MOTD
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager

/**
 * Settings class to simplify reading and writing Message of the Day data.
 * MotD operates on an integer-based priority system, where a new message
 * is shown if the message's version number is higher than the one that is
 * currently saved.
 */
interface MotdSettings {
    fun shouldShowMotd(version: Int): Boolean
    fun setLastVersion(version: Int)

    companion object {
        const val KEY_LAST_MOTD = "KEY_LAST_MOTD"
    }
}

class DefaultMotdSettings : SettingsManager(), MotdSettings {
    private var lastMotdId: Int
        get() = basicSettings.getInt(KEY_LAST_MOTD, defaultValue = -1)
        set(value) = basicSettings.putInt(KEY_LAST_MOTD, value)

    override fun shouldShowMotd(version: Int): Boolean {
        return version < lastMotdId
    }

    override fun setLastVersion(version: Int) {
        lastMotdId = version
    }
}