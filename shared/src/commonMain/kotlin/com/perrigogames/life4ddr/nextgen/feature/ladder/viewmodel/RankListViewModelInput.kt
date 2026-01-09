package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass

sealed class RankListViewModelInput {
    data class RankClassTapped(val rankClass: LadderRankClass?) : RankListViewModelInput()
    data class RankTapped(val index: Int, val rank: LadderRank) : RankListViewModelInput()
    data class RankSelected(val rank: LadderRank) : RankListViewModelInput()
    data object MoveToPlacements : RankListViewModelInput()
    data object RankRejected : RankListViewModelInput()
    data object Back : RankListViewModelInput()
    data class GoalList(val input: GoalListInput) : RankListViewModelInput()
}