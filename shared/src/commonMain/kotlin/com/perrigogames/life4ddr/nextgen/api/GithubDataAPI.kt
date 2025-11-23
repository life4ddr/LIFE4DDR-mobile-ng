package com.perrigogames.life4ddr.nextgen.api

import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.api.base.baseHttpClient
import com.perrigogames.life4ddr.nextgen.feature.ladder.data.LadderRankData
import com.perrigogames.life4ddr.nextgen.feature.motd.data.MessageOfTheDay
import com.perrigogames.life4ddr.nextgen.feature.trials.data.TrialData
import com.perrigogames.life4ddr.nextgen.injectLogger
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

/**
 * API interface for obtaining core application files from Github
 */
interface GithubDataAPI {

    suspend fun getLadderRanks(): LadderRankData
    suspend fun getSongList(): String
    suspend fun getTrials(): TrialData
    suspend fun getMotd(): MessageOfTheDay

    companion object {
        const val MOTD_FILE_NAME = "motd.json"
        const val PLACEMENTS_FILE_NAME = "placements.json"
        const val RANKS_FILE_NAME = "ranks.json"
        const val SONGS_FILE_NAME = "songs.json"
        const val TRIALS_FILE_NAME = "trials.json"
    }
}

class DefaultGithubDataAPI: GithubDataAPI, KoinComponent {

    private val appInfo: AppInfo by inject()
    private val logger by injectLogger("GithubData")
    private val json: Json by inject()
    private val client = baseHttpClient(json, logger)

    init {
//        ensureNeverFrozen() // FIXME EnsureNeverFrozen
    }

    override suspend fun getLadderRanks(): LadderRankData =
        client.get { appGithub(GithubDataAPI.RANKS_FILE_NAME) }.body()

    override suspend fun getSongList(): String =
        client.get { appGithub(GithubDataAPI.SONGS_FILE_NAME) }.body()

    override suspend fun getTrials(): TrialData =
        client.get { appGithub(GithubDataAPI.TRIALS_FILE_NAME) }.body()

    override suspend fun getMotd(): MessageOfTheDay =
        client.get { appGithub(GithubDataAPI.MOTD_FILE_NAME) }.body()

    private fun HttpRequestBuilder.appGithub(
        filename: String,
        contentType: ContentType = ContentType.Application.Json,
    ) {
        val githubTarget = if (appInfo.isDebug) "remote-data-test" else "remote-data"
        contentType(contentType)
        accept(contentType)
        url {
            takeFrom("https://raw.githubusercontent.com/")
            encodedPath = "life4ddr/Life4DDR/refs/heads/$githubTarget/json/$filename"
        }
    }
}
