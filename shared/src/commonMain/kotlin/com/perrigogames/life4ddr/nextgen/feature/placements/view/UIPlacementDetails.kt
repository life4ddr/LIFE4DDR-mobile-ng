package com.perrigogames.life4ddr.nextgen.feature.placements.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.PlacementRank
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementDetailsAction
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class UIPlacementDetails(
    val rankIcon: LadderRank = PlacementRank.COPPER.toLadderRank(),
    val descriptionPoints: List<StringDesc> = emptyList(),
    val songs: List<UITrialSong> = emptyList(),
    val ctaText: StringDesc = MR.strings.finalize.desc(),
    val ctaAction: PlacementDetailsAction = PlacementDetailsAction.FinalizeClicked,
)
