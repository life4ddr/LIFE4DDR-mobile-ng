package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType

sealed class TrialSessionInput {
    data class StartTrial(val fromDialog: Boolean) : TrialSessionInput()

    data class ChangeTargetRank(
        val target: TrialRank
    ) : TrialSessionInput()

    data class TakePhoto(
        val index: Int,
    ) : TrialSessionInput()

    data object TakeResultsPhoto : TrialSessionInput()

    data class PhotoTaken(
        val photoUri: String,
        val index: Int,
    ) : TrialSessionInput()

    data class ResultsPhotoTaken(
        val photoUri: String,
    ) : TrialSessionInput()

    data object HideBottomSheet : TrialSessionInput()

    data object AdvanceStage : TrialSessionInput()

    data class UseShortcut(
        val songId: String,
        val shortcut: ShortcutType?,
    ) : TrialSessionInput()

    data class EditItem(
        val index: Int,
    ) : TrialSessionInput()

    data class ChangeText(
        val id: String,
        val text: String,
    ) : TrialSessionInput()

    data class ManualScoreEntry(
        val rank: TrialRank,
        val ex: Int
    ) : TrialSessionInput()
}
