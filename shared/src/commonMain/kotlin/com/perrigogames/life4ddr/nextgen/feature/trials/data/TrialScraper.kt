package com.perrigogames.life4ddr.nextgen.feature.trials.data

import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.feature.profile.manager.UserInfoSettings
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.manager.TrialDataManager
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.perrigogames.life4ddr.nextgen.model.BaseModel
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

interface TrialScraper {
    fun scrapeTrialData(): Flow<TrialScraperResult>
}

class DefaultTrialScraper(
    private val httpClient: HttpClient,
    private val trialDataManager: TrialDataManager,
    private val userInfoSettings: UserInfoSettings,
    private val logger: Logger
) : BaseModel(), TrialScraper {

    @OptIn(ExperimentalTime::class)
    override fun scrapeTrialData(): Flow<TrialScraperResult> {
        val username = userInfoSettings.userName.value
        val flow = MutableSharedFlow<TrialScraperResult>()

        mainScope.launch {
            val searchItems = scrapeProfile(username, flow)
            if (searchItems != null) {
                scrapeTrialPages(username, searchItems, flow)
            }
        }
        return flow
    }

    private suspend fun scrapeProfile(
        username: String,
        resultFlow: MutableSharedFlow<TrialScraperResult>,
    ): List<TrialSearchItem>? {
        logger.d { "Scraping profile for $username" }
        resultFlow.emit(TrialScraperResult.StartingProfile(username))

        val html = try {
            httpClient.get(profilePageUrl(username)).bodyAsText()
        } catch (e: Exception) {
            logger.d { "Failed to scrape profile for $username: ${e.message}" }
            resultFlow.emit(TrialScraperResult.ProfileError(username))
            return null
        }
        logger.d { "$username profile successfully fetched" }

        val trialTargets = mutableListOf<TrialSearchItem>()

        val handler = object : KsoupHtmlHandler {
            private var resultsAreaFound = false
            private var resultsCompleted = false
            private var trialFound = false
            private var currentTrial: Trial? = null
            private var rankingFound = false

            override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                if (resultsCompleted) return
                if (name == "ul" && attributes["class"] == "profile_trials_ranked_list") {
                    resultsAreaFound = true
                } else if (resultsAreaFound && name == "li" && attributes["class"] == "profile_rank_image") {
                    trialFound = true
                    currentTrial = null
                } else if (trialFound) {
                    if (name == "a") {
                        val trialId = attributes["href"]
                            ?.substringAfter("https://life4ddr.com/")
                            ?.trim('/')
                            ?: return
                        val trial = trialDataManager.findTrial(trialId)
                        if (trial != null) {
                            currentTrial = trial
                        } else {
                            logger.w { "Trial $trialId not found!" }
                        }
                    } else if (name == "div" && attributes["class"] == "profile_rank_image_ranking") {
                        rankingFound = true
                    }
                }
            }

            override fun onText(text: String) {
                if (resultsCompleted) return
                if (rankingFound) {
                    val ranking = text.trimStart('#').toIntOrNull()
                    if (ranking != null) {
                        trialTargets.add(TrialSearchItem(currentTrial!!, ranking))
                    } else {
                        logger.w { "Invalid ranking: $text" }
                    }
                    rankingFound = false
                }
            }

            override fun onCloseTag(name: String, isImplied: Boolean) {
                if (resultsCompleted) return
                if (trialFound && name == "li") {
                    trialFound = false
                    currentTrial = null
                } else if (resultsAreaFound && name == "ul") {
                    resultsCompleted = true
                }
            }
        }

        val parser = KsoupHtmlParser(handler)
        parser.write(html)
        parser.end()

        if (trialTargets.isEmpty()) {
            resultFlow.emit(TrialScraperResult.NoTrialsFound(username))
        }
        return trialTargets
    }

    private suspend fun scrapeTrialPages(
        username: String,
        items: List<TrialSearchItem>,
        resultFlow: MutableSharedFlow<TrialScraperResult>,
    ) {
        logger.d { "Scraping trial data for user $username" }
        var hits = 0

        items.forEachIndexed { idx, (trial, position) ->
            resultFlow.emit(
                TrialScraperResult.ProfileFound.Progress(
                    position = idx,
                    total = items.size,
                    hits = hits,
                    trial = trial
                )
            )

            logger.d { "Scraping ${trial.id}" }
            val html = try {
                httpClient.get(trialPageUrl(trial)).bodyAsText()
            } catch (e: Exception) {
                logger.d { "${trial.id}: ${e.message}" }
                return@forEachIndexed
            }
            logger.d { "${trial.id} successfully fetched" }

            var rankString: String? = null
            var exScore: Int? = null

            val handler = object : KsoupHtmlHandler {
                private var resultRowCount = 0
                private var inResultRow = false
                private var isCurrentUser = false
                private var currentTdIndex = 0
                private var rowFound = false

                override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                    if (rowFound) return
                    if (name == "tr") {
                        val className = attributes["class"] ?: ""
                        if (className.startsWith("ninja_table_row_")) {
                            resultRowCount++
                            inResultRow = true
                            isCurrentUser = false
                            currentTdIndex = 0
                        }
                    } else if (inResultRow && name == "td") {
                        currentTdIndex++
                    }
                }

                override fun onText(text: String) {
                    if (rowFound || !inResultRow) return
                    when (currentTdIndex) {
                        1 -> if (resultRowCount == position) {
                            isCurrentUser = true
                            if (text != username) {
                                logger.w { "User $username does not match the leaderboard name $text" }
                            }
                        }
                        2 -> if (isCurrentUser) {
                            rankString = text
                        }
                        5 -> if (isCurrentUser) {
                            exScore = text.trim().toIntOrNull()
                        }
                    }
                }

                override fun onCloseTag(name: String, isImplied: Boolean) {
                    if (rowFound || !inResultRow) return
                    when (name) {
                        "tr" -> {
                            if (isCurrentUser) {
                                rowFound = true
                            }
                            inResultRow = false
                        }
                    }
                }
            }

            val parser = KsoupHtmlParser(handler)
            parser.write(html)
            parser.end()

            if (rankString != null && exScore != null) {
                hits++
                val rank = try {
                    TrialRank.parse(rankString.uppercase()) ?: TrialRank.COPPER
                } catch (e: Exception) {
                    TrialRank.COPPER
                }
                logger.d { "${trial.id}: Entry found with rank $rank and EX score" }
                resultFlow.emit(
                    TrialScraperResult.ProfileFound.Success(
                        total = items.size,
                        hits = hits,
                        trial = trial,
                        rank = rank,
                        exScore = exScore!!
                    )
                )
            } else {
                logger.d { "${trial.id}: No entry found" }
            }
        }

        resultFlow.emit(TrialScraperResult.ProfileFound.Finished(total = items.size, hits = hits))
    }

    private fun profilePageUrl(username: String) = "https://life4ddr.com/profile/$username/"
    private fun trialPageUrl(trial: Trial) = "https://life4ddr.com/${trial.id}/"

    private data class TrialSearchItem(
        val trial: Trial,
        val position: Int
    )
}
