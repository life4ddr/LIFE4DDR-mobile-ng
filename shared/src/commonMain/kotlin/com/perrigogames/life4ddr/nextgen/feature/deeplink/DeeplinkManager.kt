package com.perrigogames.life4ddr.nextgen.feature.deeplink

import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager.Companion.DEEPLINK_PREFIX
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager.Companion.SANBAI_AUTH_RETURN_PATH
import com.perrigogames.life4ddr.nextgen.feature.sanbai.manager.SanbaiManager
import com.perrigogames.life4ddr.nextgen.injectLogger
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import kotlinx.coroutines.launch
import org.koin.core.component.inject

interface DeeplinkManager {
    fun processDeeplink(deeplink: String)

    companion object {
        const val DEEPLINK_PREFIX = "life4://"
        const val SANBAI_AUTH_RETURN_PATH = "sanbai_auth"
        const val SANBAI_AUTH_RETURN_PATH_FULL = "$DEEPLINK_PREFIX$SANBAI_AUTH_RETURN_PATH"
    }
}

class DefaultDeeplinkManager : BaseModel(), DeeplinkManager {

    private val sanbaiManager: SanbaiManager by inject()
    private val logger by injectLogger("DeeplinkManager")

    override fun processDeeplink(deeplink: String) {
        val sections = deeplink
            .removePrefix(DEEPLINK_PREFIX)
            .split("/", "?")
        val queryParams = sections.lastOrNull()
            ?.split("&")
            ?.map { it.split("=") }
            ?.associate { it[0] to it.getOrNull(1) }
            ?: emptyMap()
        when (sections[0]) {
            SANBAI_AUTH_RETURN_PATH -> {
                val authCode = queryParams["code"]
//                val playerId = queryParams["player_id"]
                authCode?.let {
                    ktorScope.launch {
                        if (sanbaiManager.completeLogin(it)) {
                            sanbaiManager.fetchScores()
                        }
                    }
                }
            }
            else -> {
                logger.w("Unknown deeplink: ${sections[0]}")
            }
        }
    }
}