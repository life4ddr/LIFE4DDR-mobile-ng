package com.perrigogames.life4ddr.nextgen.feature.placements.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListInput
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import com.perrigogames.life4ddr.nextgen.feature.trials.view.bags
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

object UIPlacementMocks {
    fun createUIPlacementScreen(
        titleText: StringDesc = MR.strings.placements.desc(),
        headerText: StringDesc = MR.strings.placement_list_description.desc(),
        placements: List<UIPlacement> = listOf(
            createUIPlacementData(),
            createUIPlacementData(
                rankIcon = LadderRank.SILVER5,
                difficultyRangeString = "L11-L13",
            ),
            createUIPlacementData(
                rankIcon = LadderRank.GOLD5,
                difficultyRangeString = "L14-L16",
            ),
        ),
    ) = UIPlacementListScreen(
        titleText = titleText,
        headerText = headerText,
        placements = placements,
        ranksButtonText = MR.strings.select_rank_instead.desc(),
        ranksButtonInput = PlacementListInput.GoToRanksScreen,
        skipButtonText = MR.strings.start_no_rank.desc(),
        skipButtonInput = PlacementListInput.SkipPlacement,
        skipConfirmation = null,
    )

    fun createUIPlacementData(
        id: String = "placement_id",
        rankIcon: LadderRank = LadderRank.BRONZE5,
        difficultyRangeString: String = "L7-L10",
        songs: List<UITrialSong> = bags,
    ) = UIPlacement(
        id = id,
        rankIcon = rankIcon,
        difficultyRangeString = difficultyRangeString,
        songs = songs,
        selectedInput = PlacementListInput.PlacementSelected(id),
    )
}