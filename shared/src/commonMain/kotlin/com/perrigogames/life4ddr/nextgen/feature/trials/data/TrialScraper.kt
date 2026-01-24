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
        logger.d { "Scraping trial data for user $username" }
        val trials = trialDataManager.trials
        var hits = 0

        val flow = MutableSharedFlow<TrialScraperResult>()
        mainScope.launch {
            trials.forEachIndexed { idx, trial ->
                flow.emit(
                    TrialScraperResult.Progress(
                        position = idx + 1,
                        total = trials.size,
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
                    private var inResultRow = false
                    private var isCurrentUser = false
                    private var currentTdIndex = 0
                    private var rowFound = false

                    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                        if (rowFound) return
                        if (name == "tr") {
                            val className = attributes["class"] ?: ""
                            if (className.startsWith("ninja_table_row_")) {
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
                            1 -> if (text == username) {
                                isCurrentUser = true
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
                    flow.emit(
                        TrialScraperResult.Success(
                            total = trials.size,
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

            flow.emit(TrialScraperResult.Finished(total = trials.size, hits = hits))
        }

        return flow
    }

    private fun trialPageUrl(trial: Trial) =
        "https://life4ddr.com/${trial.id}/"
}
