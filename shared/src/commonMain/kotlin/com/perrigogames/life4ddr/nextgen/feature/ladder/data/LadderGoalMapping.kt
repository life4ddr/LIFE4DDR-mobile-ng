package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.GoalStatus
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.GoalStateManager
import com.perrigogames.life4ddr.nextgen.feature.ladder.manager.MAConfig
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderDetailItem
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderProgress
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.GoalListInput
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.toMAPointsCategoryString
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.toMAPointsDouble
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.perfectsScoreText
import com.perrigogames.life4ddr.nextgen.longNumberString
import dev.icerock.moko.resources.desc.desc
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class DiffExtra {
    NONE, DIFF, TIER, DIFF_TIER
}

class LadderGoalMapper : KoinComponent {

    private val goalStateManager: GoalStateManager by inject()

    fun toViewData(
        base: BaseRankGoal,
        goalStatus: GoalStatus = goalStateManager.getOrCreateGoalState(base).status,
        progress: LadderGoalProgress?,
        isExpanded: Boolean,
        allowCompleting: Boolean,
        allowHiding: Boolean,
        showDiffTiers: Boolean,
        maConfig: MAConfig,
    ): UILadderGoal {
        val isComplete = goalStatus == GoalStatus.COMPLETE || progress?.isComplete == true
        val isMFC = base is MAPointsGoal ||
                (base is StackedRankGoalWrapper && base.mainGoal is MAPointsStackedGoal)
        val diffExtra = when {
            progress == null -> DiffExtra.NONE
            showDiffTiers -> when (progress.diffTiersOnly) {
                true -> DiffExtra.TIER
                false -> DiffExtra.DIFF_TIER
            }
            !progress.diffTiersOnly -> DiffExtra.DIFF
            else -> DiffExtra.NONE
        }

        fun ChartResultPair.formatResultItem(diffExtra: DiffExtra) : UILadderDetailItem.Entry {
            val clearType = result?.clearType ?: ClearType.NO_PLAY
            val isPFCOrHigher = clearType >= ClearType.PERFECT_FULL_COMBO

            val title = KsoupEntities.decodeHtml(chart.song.title)
            val subtitle = when (diffExtra) {
                DiffExtra.NONE -> null
                DiffExtra.DIFF -> "(${chart.difficultyNumber})"
                DiffExtra.TIER -> "(.${chart.difficultyTierString})"
                DiffExtra.DIFF_TIER -> "(${chart.combinedDifficultyNumberString})"
            }
            return if (isMFC) {
                UILadderDetailItem.Entry(
                    leftText = title,
                    leftColor = chart.difficultyClass.colorRes,
                    leftSubtitle = subtitle,
                    rightText = "L${chart.difficultyNumber} > ${maPoints()}".desc(),
                    rightColor = result!!.clearType.colorRes,
                )
            } else {
                UILadderDetailItem.Entry(
                    leftText = title,
                    leftColor = chart.difficultyClass.colorRes,
                    leftSubtitle = subtitle,
                    rightText = if (isPFCOrHigher) {
                        perfectsScoreText(result!!.clearType, result.score)
                    } else {
                        (result?.score ?: 0).toInt().longNumberString().desc()
                    },
                    rightColor = result?.clearType?.colorRes,
                )
            }
        }

        fun List<ChartResultPair>.formatCombinedMAPointsEntries(
            clearType: ClearType
        ) : List<UILadderDetailItem.Entry> {
            return groupBy { it.baseMAPoints() }
                .entries.sortedBy { it.key }
                .map { (points, results) ->
                    val totalPoints = results.sumOf { it.maPointsThousandths() }.toMAPointsDouble()
                    UILadderDetailItem.Entry(
                        leftText = points.toMAPointsCategoryString(),
                        leftColor = clearType.colorRes,
                        leftSubtitle = "${results.first().maPoints()} Points",
                        rightText = "x${results.count()} > $totalPoints".desc(),
                        rightColor = clearType.colorRes,
                    )
                }
        }

        fun List<ChartResultPair>.formatResultList(diffExtra: DiffExtra) : List<UILadderDetailItem.Entry> {
            return if (isMFC && (maConfig.combineMFCs || maConfig.combineSDPs)) {
                val mfcs = this.filter { it.result?.clearType == ClearType.MARVELOUS_FULL_COMBO }
                val mfcEntries = if (maConfig.combineMFCs) {
                    mfcs.formatCombinedMAPointsEntries(ClearType.MARVELOUS_FULL_COMBO)
                } else {
                    mfcs.map { it.formatResultItem(diffExtra) }
                }

                val sdps = this.filter { it.result?.clearType == ClearType.SINGLE_DIGIT_PERFECTS }
                val sdpEntries = if (maConfig.combineSDPs) {
                    sdps.formatCombinedMAPointsEntries(ClearType.SINGLE_DIGIT_PERFECTS)
                } else {
                    sdps.map { it.formatResultItem(diffExtra) }
                }
                mfcEntries + sdpEntries
            } else {
                this.map { it.formatResultItem(diffExtra) }
            }
        }

        return UILadderGoal(
            id = base.id.toLong(),
            goalText = base.goalString(),
            completed = isComplete,
            canComplete = allowCompleting && progress == null, // if we can't illustrate progress, it has to be user-driven
            hidden = goalStatus == GoalStatus.IGNORED,
            canHide = !isComplete && // don't allow hiding completed goals
                    (allowHiding || goalStatus == GoalStatus.IGNORED), // must be able to unhide
            showCheckbox = true,
            progress = progress?.toViewData(),
            expandAction = if (progress?.hasResults == true) {
                GoalListInput.OnGoal.ToggleExpanded(base.id.toLong())
            } else {
                null
            },
            detailItems = if (isExpanded && progress != null) {
                val resultItems = progress.results?.formatResultList(diffExtra) ?: emptyList()
                val resultBottomItems = progress.resultsBottom?.formatResultList(diffExtra) ?: emptyList()
                if (!resultItems.isEmpty() && !resultBottomItems.isEmpty()) {
                    resultItems + listOf(UILadderDetailItem.Spacer) + resultBottomItems
                } else {
                    resultItems + resultBottomItems
                }
            } else {
                emptyList()
            },
            altDetailItems = if (isExpanded && progress != null) {
                progress.altResults?.formatResultList(diffExtra) ?: emptyList()
            } else {
                emptyList()
            },
            debugText = base.toString().let { text ->
                val mainSplit = text.split('(', ')')
                val title = "${mainSplit[0]} (${base.id})"
                val body = mainSplit[1].split(", ")
                    .filterNot { it.contains("=null") }
                    .filterNot { it.contains("allowsHigherDiffNum=false") }
                    .filterNot { it.contains("id=") }
                    .joinToString("\n") { "\t$it" }

                listOf(title, body).joinToString("\n")
            }, // FIXME don't show on release mode
        )
    }
}

fun LadderGoalProgress.toViewData(): UILadderProgress? {
    if (max == 0.0) return null
    return UILadderProgress(
        count = progress,
        max = max,
        showMax = showMax,
        showProgressBar = showProgressBar,
    )
}