package com.perrigogames.life4ddr.nextgen.feature.sanbai.api

import com.perrigogames.life4ddr.nextgen.api.SanbaiAuthTokenRequest
import com.perrigogames.life4ddr.nextgen.api.SanbaiAuthTokenResponse
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager.Companion.SANBAI_AUTH_RETURN_PATH_FULL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

fun sanbaiHttpClient(
    sanbaiSettings: SanbaiAPISettings,
    log: co.touchlab.kermit.Logger
) = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                log.v { message }
            }
        }

        level = LogLevel.INFO
    }
    install(HttpTimeout) {
        val timeout = 30000L
        connectTimeoutMillis = timeout
        requestTimeoutMillis = timeout
        socketTimeoutMillis = timeout
    }
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(
                    accessToken = sanbaiSettings.bearerToken,
                    refreshToken = sanbaiSettings.refreshToken
                )
            }
            refreshTokens {
                val tokenResponse = client.post("https://3icecream.com/oauth/token") {
                    setBody(
                        SanbaiAuthTokenRequest(
                            clientId = SanbaiSecrets.SANBAI_APP_ID,
                            clientSecret = SanbaiSecrets.SANBAI_APP_SECRET,
                            grantType = "refresh_token",
                            refreshToken = sanbaiSettings.refreshToken,
                            redirectUri = SANBAI_AUTH_RETURN_PATH_FULL
                        )
                    )
                }
                val response = tokenResponse.body<SanbaiAuthTokenResponse>()
                sanbaiSettings.setUserProperties(response)
                BearerTokens(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                )
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun SanbaiAPISettings.setUserProperties(
    response: SanbaiAuthTokenResponse
) = setUserProperties(
    bearerToken = response.accessToken,
    refreshToken = response.refreshToken,
    refreshExpires = Clock.System.now().plus(response.expiresIn.seconds),
    playerId = response.playerId,
)
