package com.perrigogames.life4ddr.nextgen.feature.ladder.data

import com.perrigogames.life4ddr.nextgen.feature.songresults.data.ChartResultPair

/**
 * Data class representing the local user's current progress towards
 * a particular goal
 */
class LadderGoalProgress(
    val progress: Double,
    val max: Double,
    val showMax: Boolean = true,
    val showProgressBar: Boolean = true,
    val diffTiersOnly: Boolean = true,
    val results: List<ChartResultPair>? = null,
    val resultsBottom: List<ChartResultPair>? = null,
    val altResults: List<ChartResultPair>? = null,
) {

    constructor(
        progress: Int,
        max: Int,
        showMax: Boolean = true,
        showProgressBar: Boolean = true,
        diffTiersOnly: Boolean = true,
        results: List<ChartResultPair>? = null,
        resultsBottom: List<ChartResultPair>? = null,
        altResults: List<ChartResultPair>? = null,
    ) : this(progress.toDouble(), max.toDouble(), showMax, showProgressBar, diffTiersOnly, results, resultsBottom, altResults)

    val isComplete = progress >= max && max > 0

    val percent = progress / max

    val hasResults = results?.isEmpty() == false || resultsBottom?.isEmpty() == false

    override fun toString() = "$progress / $max (show=$showMax)${results?.count()?.let { ", $it" } ?: ""}"
}
