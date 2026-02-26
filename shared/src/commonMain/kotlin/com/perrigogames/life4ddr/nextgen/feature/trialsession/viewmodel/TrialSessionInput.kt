package com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel

import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.enums.ShortcutType
import kotlinx.io.files.Path

sealed class TrialSessionInput {
    data class AttemptToClose(val confirmed: Boolean = false) : TrialSessionInput()

    data class StartTrial(val fromDialog: Boolean) : TrialSessionInput()

    data class ChangeTargetRank(
        val target: TrialRank
    ) : TrialSessionInput()

    data class TakePhoto(
        val index: Int,
    ) : TrialSessionInput()

    data object TakeResultsPhoto : TrialSessionInput()

    data class PhotoTaken(
        val photoPath: Path,
        val index: Int,
    ) : TrialSessionInput()

    data class ResultsPhotoTaken(
        val photoPath: Path,
    ) : TrialSessionInput()

    data object Finished : TrialSessionInput()

    data object HideBottomSheet : TrialSessionInput()

    data object AdvanceStage : TrialSessionInput()

    data class UseShortcut(
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

    data class ToggleExLost(
        val enabled: Boolean
    ) : TrialSessionInput()
}
