package com.perrigogames.life4ddr.nextgen.model

import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import com.perrigogames.life4ddr.nextgen.model.MajorUpdateSettings.Companion.KEY_MAJOR_UPDATE

interface MajorUpdateSettings {
    var lastMajorUpdateSeen: Int

    companion object {
        const val KEY_MAJOR_UPDATE = "KEY_MAJOR_UPDATE"
    }
}

class DefaultMajorUpdateSettings : SettingsManager(), MajorUpdateSettings {
    override var lastMajorUpdateSeen: Int
        get() = basicSettings.getInt(KEY_MAJOR_UPDATE, -1)
        set(value) = basicSettings.putInt(KEY_MAJOR_UPDATE, value)
}
