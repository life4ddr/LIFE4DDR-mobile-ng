package com.perrigogames.life4ddr.nextgen.feature.placements.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListInput
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc

data class UIPlacementListScreen(
    val titleText: StringDesc,
    val headerText: StringDesc,
    val placements: List<UIPlacement>,
    val ranksButtonText: StringDesc,
    val ranksButtonInput: PlacementListInput,
    val skipButtonText: StringDesc,
    val skipButtonInput: PlacementListInput,
    val skipConfirmation: UIPlacementSkipConfirmation? = null
)

data class UIPlacement(
    val id: String,
    val rankIcon: LadderRank,
    val difficultyRangeString: String = "", // FIXME resource
    val songs: List<UITrialSong>,
    val selectedInput: PlacementListInput
) {
    val color: ColorResource = rankIcon.group.colorRes
    val placementName: StringResource = rankIcon.groupNameRes
}

data class UIPlacementSkipConfirmation(
    val titleText: StringDesc = MR.strings.placement_close_confirm_title.desc(),
    val bodyText: StringDesc = MR.strings.placement_close_confirm_body.desc(),
    val confirmButtonText: StringDesc = MR.strings.confirm.desc(),
    val confirmButtonInput: PlacementListInput = PlacementListInput.SkipPlacementConfirm,
    val cancelButtonText: StringDesc = MR.strings.cancel.desc(),
    val cancelButtonInput: PlacementListInput = PlacementListInput.SkipPlacementCancel
)
