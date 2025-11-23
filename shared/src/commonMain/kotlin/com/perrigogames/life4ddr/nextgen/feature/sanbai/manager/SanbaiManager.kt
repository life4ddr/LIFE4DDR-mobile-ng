package com.perrigogames.life4ddr.nextgen.feature.sanbai.manager

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPI
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerLocation
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerManager
import com.perrigogames.life4ddr.nextgen.feature.banners.UIBanner
import com.perrigogames.life4ddr.nextgen.feature.banners.UIBannerTemplates
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import dev.icerock.moko.resources.desc.desc
import org.koin.core.component.inject
import kotlin.getValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface SanbaiManager {

    fun requiresAuthorization(): Boolean
    suspend fun completeLogin(authCode: String): Boolean
    suspend fun fetchScores(): Boolean
}

// TODO SongResultsManager

@OptIn(ExperimentalTime::class)
class DefaultSanbaiManager : BaseModel(), SanbaiManager {

    private val sanbaiAPI: SanbaiAPI by inject()
    private val sanbaiAPISettings: SanbaiAPISettings by inject()
//    private val songResultsManager: SongResultsManager by inject()
    private val bannersManager: BannerManager by inject()

    override fun requiresAuthorization(): Boolean {
        return sanbaiAPISettings.refreshExpires < Clock.System.now()
    }

    override suspend fun completeLogin(authCode: String): Boolean {
        bannersManager.setBanner(BANNER_LOADING, BannerLocation.PROFILE, BannerLocation.SCORES)
        try {
            sanbaiAPI.getSessionToken(authCode)
        } catch (e: Exception) {
            bannersManager.setBanner(BANNER_ERROR, BannerLocation.PROFILE, BannerLocation.SCORES, durationSeconds = 3)
            return false
        }
        return true
    }

    override suspend fun fetchScores(): Boolean {
        if (requiresAuthorization()) {
            return false
        }
        bannersManager.setBanner(BANNER_LOADING, BannerLocation.PROFILE, BannerLocation.SCORES)
        try {
            sanbaiAPI.getScores()?.let { scores ->
//                songResultsManager.addScores(scores.map { it.toChartResult() })
            }
        } catch (e: Exception) {
            bannersManager.setBanner(BANNER_ERROR, BannerLocation.PROFILE, BannerLocation.SCORES, durationSeconds = 3)
            return false
        }
        bannersManager.setBanner(BANNER_SUCCESS, BannerLocation.PROFILE, BannerLocation.SCORES, durationSeconds = 3)
        return true
    }

    companion object {
        val BANNER_LOADING = UIBanner(
            text = MR.strings.sanbai_syncing_scores.desc()
        )
        val BANNER_SUCCESS = UIBannerTemplates.success(MR.strings.sanbai_syncing_success.desc())
        val BANNER_ERROR = UIBannerTemplates.error(MR.strings.sanbai_syncing_error.desc())
    }
}