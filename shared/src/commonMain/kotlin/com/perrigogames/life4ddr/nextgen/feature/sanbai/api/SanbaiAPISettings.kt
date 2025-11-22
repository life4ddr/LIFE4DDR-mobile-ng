package com.perrigogames.life4ddr.nextgen.feature.sanbai.api

import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings.Companion.KEY_SANBAI_DATA_REFRESHED
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings.Companion.KEY_SANBAI_BEARER_TOKEN
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings.Companion.KEY_SANBAI_REFRESH_TOKEN
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings.Companion.KEY_SANBAI_REFRESH_EXPIRES
import com.perrigogames.life4ddr.nextgen.feature.sanbai.api.SanbaiAPISettings.Companion.KEY_SANBAI_PLAYER_ID
import com.perrigogames.life4ddr.nextgen.feature.settings.manager.SettingsManager
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Holds information that the Sanbai integration needs to retain.
 */
@OptIn(ExperimentalTime::class)
interface SanbaiAPISettings {
    var songDataUpdated: Instant

    var bearerToken: String
    var refreshToken: String
    var refreshExpires: Instant
    var playerId: String?

    fun setUserProperties(
        bearerToken: String,
        refreshToken: String,
        refreshExpires: Instant,
        playerId: String?
    ) {
        this.bearerToken = bearerToken
        this.refreshToken = refreshToken
        this.refreshExpires = refreshExpires
        this.playerId = playerId
    }

    companion object {
        const val KEY_SANBAI_DATA_REFRESHED = "KEY_SANBAI_DATA_REFRESHED"
        const val KEY_SANBAI_BEARER_TOKEN = "KEY_SANBAI_BEARER_TOKEN"
        const val KEY_SANBAI_REFRESH_TOKEN = "KEY_SANBAI_REFRESH_TOKEN"
        const val KEY_SANBAI_REFRESH_EXPIRES = "KEY_SANBAI_REFRESH_EXPIRES"
        const val KEY_SANBAI_PLAYER_ID = "KEY_SANBAI_PLAYER_ID"
    }
}

@OptIn(ExperimentalTime::class)
class DefaultSanbaiAPISettings : SettingsManager(), SanbaiAPISettings {

    override var songDataUpdated: Instant
        get() = basicSettings.getStringOrNull(KEY_SANBAI_DATA_REFRESHED)
            ?.let { Instant.parse(it) }
            ?: Instant.DISTANT_PAST
        set(value) {
            basicSettings.putString(KEY_SANBAI_DATA_REFRESHED, value.toString())
        }

    override var bearerToken: String
        get() = basicSettings.getString(KEY_SANBAI_BEARER_TOKEN, defaultValue = "")
        set(value) {
            basicSettings.putString(KEY_SANBAI_BEARER_TOKEN, value)
        }

    override var refreshToken: String
        get() = basicSettings.getString(KEY_SANBAI_REFRESH_TOKEN, defaultValue = "")
        set(value) {
            basicSettings.putString(KEY_SANBAI_REFRESH_TOKEN, value)
        }

    override var refreshExpires: Instant
        get() = basicSettings.getStringOrNull(KEY_SANBAI_REFRESH_EXPIRES)
            ?.let { Instant.parse(it) }
            ?: Instant.DISTANT_PAST
        set(value) {
            basicSettings.putString(KEY_SANBAI_REFRESH_EXPIRES, value.toString())
        }

    override var playerId: String?
        get() = basicSettings.getStringOrNull(KEY_SANBAI_PLAYER_ID)
        set(value) {
            if (value != null) {
                basicSettings.putString(KEY_SANBAI_PLAYER_ID, value)
            } else {
                basicSettings.remove(KEY_SANBAI_PLAYER_ID)
            }
        }
}