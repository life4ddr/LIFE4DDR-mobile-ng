package com.perrigogames.life4ddr.nextgen.feature.settings.manager

import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import org.koin.core.component.inject

@OptIn(ExperimentalSettingsApi::class, ExperimentalSettingsImplementation::class)
open class SettingsManager : BaseModel() {
    private val appInfo: AppInfo by inject()
    protected val basicSettings: Settings by inject()
    protected val settings: FlowSettings by inject()

    fun getDebugBoolean(key: String) = appInfo.isDebug && basicSettings.getBoolean(key, false)
}
