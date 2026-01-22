package com.perrigogames.life4ddr.nextgen.feature.notifications.alert.manager

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.notifications.alert.view.UIAlert
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.asImageDesc
import kotlinx.coroutines.flow.*

interface AlertManager {
    val alerts: StateFlow<UIAlert?>

    fun sendAlert(type: AlertType)

    fun dismissAlert(hideChecked: Boolean)
}

class DefaultAlertManager(
    private val settings: AlertSettings,
) : BaseModel(), AlertManager {

    private val _alerts = MutableStateFlow<List<AlertType>>(emptyList())
    override val alerts: StateFlow<UIAlert?> = _alerts
        .map { it.firstOrNull() }
        .map { alertType ->
            when {
                alertType == null -> null
                !settings.shouldShowAlert(alertType) && !alertType.force -> null
                else -> when (alertType) {
                    is AlertType.LIFE4FlarePromo -> UIAlert(
                        title = MR.strings.sanbai_import_life4_flare_promo_title.desc(),
                        text = MR.strings.sanbai_import_life4_flare_promo_body.desc(),
                        image = MR.images.flare_life4_alert.asImageDesc(),
                        canHide = alertType.canHide,
                    )
                }
            }
        }
        .stateIn(mainScope, SharingStarted.Lazily, null)

    override fun sendAlert(type: AlertType) {
        _alerts.value += type
    }

    override fun dismissAlert(hideChecked: Boolean) {
        val alert = _alerts.value.firstOrNull() ?: return
        _alerts.value = _alerts.value.subList(1, _alerts.value.size)
        if (hideChecked && alert.canHide) {
            settings.setShouldShowAlert(alert, false)
        }
    }
}
