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
import com.perrigogames.life4ddr.nextgen.longNumberString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LadderGoalMapper : KoinComponent {

    private val goalStateManager: GoalStateManager by inject()

    fun toViewData(
        base: BaseRankGoal,
        goalStatus: GoalStatus = goalStateManager.getOrCreateGoalState(base).status,
        progress: LadderGoalProgress?,
        isExpanded: Boolean,
        allowCompleting: Boolean,
        allowHiding: Boolean,
        maConfig: MAConfig = MAConfig(),
    ): UILadderGoal {
        val isComplete = goalStatus == GoalStatus.COMPLETE || progress?.isComplete == true
        val isMFC = base is MAPointsGoal ||
                (base is StackedRankGoalWrapper && base.mainGoal is MAPointsStackedGoal)

        fun ChartResultPair.formatResultItem() : UILadderDetailItem.Entry {
            return if (isMFC) {
                UILadderDetailItem.Entry(
                    leftText = KsoupEntities.decodeHtml(chart.song.title),
                    leftColor = chart.difficultyClass.colorRes,
                    leftWeight = 0.75f,
                    rightText = "L${chart.difficultyNumber} > ${maPoints()}",
                    rightColor = result!!.clearType.colorRes,
                    rightWeight = 0.25f
                )
            } else {
                UILadderDetailItem.Entry(
                    leftText = KsoupEntities.decodeHtml(chart.song.title),
                    leftColor = chart.difficultyClass.colorRes,
                    rightText = (result?.score ?: 0).toInt().longNumberString(),
                )
            }
        }

        fun List<ChartResultPair>.formatCombinedMAPointsEntries(
            clearType: ClearType
        ) : List<UILadderDetailItem.Entry> {
            return groupBy { it.maPointsThousandths() }
                .map { (points, results) ->
                    val totalPoints = this.sumOf { it.maPoints() }
                    UILadderDetailItem.Entry(
                        leftText = points.toMAPointsCategoryString(),
                        leftWeight = 0.75f,
                        rightText = "${this.count()} * ${points.toMAPointsDouble()} > $totalPoints",
                        rightColor = clearType.colorRes,
                        rightWeight = 0.25f
                    )
                }
        }

        fun List<ChartResultPair>?.formatResultList() : List<UILadderDetailItem.Entry> {
            return if (isMFC && (maConfig.combineMFCs || maConfig.combineSDPs)) {
                val mfcs = this?.filter { it.result?.clearType == ClearType.MARVELOUS_FULL_COMBO } ?: emptyList()
                val mfcEntries = if (maConfig.combineMFCs) {
                    mfcs.formatCombinedMAPointsEntries(ClearType.MARVELOUS_FULL_COMBO)
                } else {
                    mfcs.map { it.formatResultItem() }
                }

                val sdps = this?.filter { it.result?.clearType == ClearType.SINGLE_DIGIT_PERFECTS } ?: emptyList()
                val sdpEntries = if (maConfig.combineSDPs) {
                    sdps.formatCombinedMAPointsEntries(ClearType.SINGLE_DIGIT_PERFECTS)
                } else {
                    sdps.map { it.formatResultItem() }
                }
                mfcEntries + sdpEntries
            } else {
                this?.map { it.formatResultItem() } ?: emptyList()
            }
        }

        val resultItems = progress?.results?.formatResultList() ?: emptyList()
        val resultBottomItems = progress?.resultsBottom.formatResultList()

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
                if (!resultItems.isEmpty() && !resultBottomItems.isEmpty()) {
                    resultItems + listOf(UILadderDetailItem.Spacer) + resultBottomItems
                } else {
                    resultItems + resultBottomItems
                }
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