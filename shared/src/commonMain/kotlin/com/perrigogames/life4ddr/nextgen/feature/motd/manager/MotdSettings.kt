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
    fun shouldShowMotd(version: Long): Boolean
    fun setLastVersion(version: Long)

    companion object {
        const val KEY_LAST_MOTD = "KEY_LAST_MOTD"
    }
}

class DefaultMotdSettings : SettingsManager(), MotdSettings {
    private var lastMotdId: Long
        get() = basicSettings.getLong(KEY_LAST_MOTD, defaultValue = -1)
        set(value) = basicSettings.putLong(KEY_LAST_MOTD, value)

    override fun shouldShowMotd(version: Long): Boolean {
        return version < lastMotdId
    }

    override fun setLastVersion(version: Long) {
        lastMotdId = version
    }
}