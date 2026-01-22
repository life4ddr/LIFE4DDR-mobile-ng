package com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager

import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager.AlertSettings.Companion.KEY_SHOULD_SHOW_LIFE4_ALERT
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager

sealed class AlertType(
    val settingsKey: String,
    val canHide: Boolean = true,
    open val force: Boolean = false,
) {
    data class LIFE4FlarePromo(
        override val force: Boolean = false,
    ) : AlertType(KEY_SHOULD_SHOW_LIFE4_ALERT)
}

interface AlertSettings {

    fun shouldShowAlert(alertType: AlertType): Boolean

    fun setShouldShowAlert(alertType: AlertType, shouldShow: Boolean)

    companion object {
        const val KEY_SHOULD_SHOW_LIFE4_ALERT = "KEY_SHOULD_SHOW_LIFE4_ALERT"
    }
}

class DefaultAlertSettings : SettingsManager(), AlertSettings {

    override fun shouldShowAlert(alertType: AlertType): Boolean =
        basicSettings.getBoolean(alertType.settingsKey, true)

    override fun setShouldShowAlert(alertType: AlertType, shouldShow: Boolean) {
        basicSettings.putBoolean(alertType.settingsKey, shouldShow)
    }
}