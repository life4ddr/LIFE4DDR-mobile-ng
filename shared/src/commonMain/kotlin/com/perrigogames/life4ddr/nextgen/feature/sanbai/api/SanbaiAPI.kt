package com.perrigogames.life4ddr.nextgen.feature.sanbai.api

import com.perrigogames.life4ddr.nextgen.api.SanbaiAuthTokenRequest
import com.perrigogames.life4ddr.nextgen.api.SanbaiAuthTokenResponse
import com.perrigogames.life4ddr.nextgen.feature.deeplink.DeeplinkManager.Companion.SANBAI_AUTH_RETURN_PATH_FULL
import com.perrigogames.life4ddr.nextgen.feature.sanbai.data.SanbaiScoreResult
import com.perrigogames.life4ddr.nextgen.feature.sanbai.data.SongListResponse
import com.perrigogames.life4ddr.nextgen.feature.sanbai.data.SongListResponseItem
import com.perrigogames.life4ddr.nextgen.injectLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface SanbaiAPI {
    suspend fun getSongData(): SongListResponse

    fun getAuthorizeUrl(): String
    suspend fun getSessionToken(code: String): SanbaiAuthTokenResponse
    suspend fun getScores(): List<SanbaiScoreResult>?
    suspend fun getPlayerId(): String

    companion object {
        fun areKeysValid() =
            SanbaiSecrets.SANBAI_APP_ID.let { it.isNotEmpty() && !it.startsWith("NO") } &&
            SanbaiSecrets.SANBAI_APP_SECRET.let { it.isNotEmpty() && !it.startsWith("NO") }
    }
}

@OptIn(ExperimentalTime::class)
class DefaultSanbaiAPI : SanbaiAPI, KoinComponent {

    private val json: Json by inject()
    private val logger by injectLogger("SanbaiAPI")
    private val sanbaiSettings: SanbaiAPISettings by inject()
    private val client: HttpClient = sanbaiHttpClient(sanbaiSettings, logger)

    override suspend fun getSongData(): SongListResponse {
        val response = client.get("https://3icecream.com/js/songdata.js") {
            contentType(ContentType.Application.JavaScript)
        }
        val lines = response.bodyAsText().split(";")
        val songData = lines[0].let { it.substring(startIndex = it.indexOf('[')) }
        val songs = json.decodeFromString<List<SongListResponseItem>>(songData)
        val lastUpdatedLine = lines.subList(1, lines.size).find { it.contains("SONG_DATA_LAST_UPDATED") }!!
        val lastUpdated = lastUpdatedLine.substring(startIndex = lastUpdatedLine.indexOf('=') + 1).toLong()
        return SongListResponse(
            songs = songs,
            lastUpdated = Instant.fromEpochMilliseconds(lastUpdated)
        )
    }

    override fun getAuthorizeUrl(): String {
        return "https://3icecream.com/oauth/authorize" +
                "?client_id=${SanbaiSecrets.SANBAI_APP_ID}" +
                "&response_type=code" +
                "&scope=read_scores" +
                "&redirect_uri=$SANBAI_AUTH_RETURN_PATH_FULL"
    }

    override suspend fun getSessionToken(code: String): SanbaiAuthTokenResponse {
        val response = client.post("https://3icecream.com/oauth/token") {
            setBody(
                SanbaiAuthTokenRequest(
                    clientId = SanbaiSecrets.SANBAI_APP_ID,
                    clientSecret = SanbaiSecrets.SANBAI_APP_SECRET,
                    code = code,
                    redirectUri = SANBAI_AUTH_RETURN_PATH_FULL,
                    grantType = "authorization_code"
                )
            )
            contentType(ContentType.Application.Json)
        }
        return response.body<SanbaiAuthTokenResponse>().also { responseBody ->
            sanbaiSettings.setUserProperties(responseBody)
        }
    }

    override suspend fun getScores(): List<SanbaiScoreResult>? {
        val response: HttpResponse = client.post("https://3icecream.com/dev/api/v1/get_scores") {
            setBody(mapOf(
                "access_token" to sanbaiSettings.bearerToken
            ))
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Failed to fetch scores: ${response.status}")
        }

        return try {
            response.body<List<SanbaiScoreResult>>()
        } catch (e: Exception) {
            logger.e("Failed to parse scores: serialization error", e)
            throw e
        }
    }

    override suspend fun getPlayerId(): String {
        val response: HttpResponse = client.get("https://3icecream.com/dev/api/v1/get_player_id") {
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Failed to fetch player id: ${response.status}")
        }

        return response.body<String>()
    }
}
