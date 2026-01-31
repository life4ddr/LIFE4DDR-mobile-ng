package com.perrigogames.life4ddr.nextgen.feature.trialsession.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Course
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionInput
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.color.ColorDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.ImageDesc

/**
 * Describes the content of the Trial Session screen.
 */
data class UITrialSession(
    val trialTitle: StringDesc,
    val trialLevel: StringDesc,
    val backgroundImage: ImageDesc,
    val targetRank: UITargetRank,
    val content: UITrialSessionContent,
    val footer: Footer
) {

    sealed class Footer {
        data class Button(
            val buttonText: StringDesc,
            val buttonAction: TrialSessionInput,
        ) : Footer()

        data class Message(
            val message: StringDesc,
        ) : Footer()
    }
}

/**
 * Describes the content of the EX score bar.
 */
data class UIEXScoreBar(
    val labelText: StringDesc = MR.strings.ex.desc(),
    val currentEx: Int = 0,
    val hintCurrentEx: Int? = null,
    val maxEx: Int = 0,
    val currentExText: StringDesc = "0".desc(),
    val maxExText: StringDesc,
    val exTextClickAction: TrialSessionInput = TrialSessionInput.ToggleExLost(false),
) {

    constructor(
        trial: Course,
        labelText: StringDesc = MR.strings.ex.desc(),
        currentEx: Int = 0,
        hintCurrentEx: Int? = null,
        currentExText: StringDesc = "0".desc(),
        exTextClickAction: TrialSessionInput = TrialSessionInput.ToggleExLost(false),
    ) : this(
        labelText = labelText,
        currentEx = currentEx,
        hintCurrentEx = hintCurrentEx,
        maxEx = trial.totalEx,
        currentExText = currentExText,
        maxExText = StringDesc.Raw("/" + trial.totalEx),
        exTextClickAction = exTextClickAction,
    )
}

/**
 * Describes the content of the Target Rank section for a
 * Trial session.
 */
data class UITargetRank(
    val state: State,
    val rank: TrialRank,
    val title: StringDesc,
    val titleColor: ColorResource,
    val rankGoalItems: List<StringDesc>,
    val availableRanks: List<TrialRank>?,
) {
    enum class State {
        /**
         * Specifies an open selector that can be changed by the user.
         */
        SELECTION,

        /**
         * Specifies an in-progress Trial with goals that should still be visible.
         */
        IN_PROGRESS,

        /**
         * Specifies a completed Trial that doesn't need to show the goals.
         */
        ACHIEVED;


    }
}

/**
 * Describes the content of the bottom half of the screen.
 */
sealed class UITrialSessionContent {

    /**
     * Specifies a summary view with each song getting equal
     * screen space.  Optionally, score/EX information can also
     */
    data class Summary(
        val items: List<Item>,
    ) : UITrialSessionContent() {

        data class Item(
            val jacketUrl: String?,
            val difficultyClassText: StringDesc,
            val difficultyClassColor: ColorResource,
            val difficultyNumberText: StringDesc,
            val summaryContent: SummaryContent? = null,
        )

        data class SummaryContent(
            val topText: StringDesc?,
            val bottomMainText: StringDesc,
            val bottomSubText: StringDesc,
        )
    }

    /**
     * Specifies a focused song view where the main set of songs
     * is shown with reduced size and information alongside a
     * single focused song with all the information needed to
     * effectively find it.
     */
    data class SongFocused(
        val items: List<Item>,
        val focusedJacketUrl: String?,
        val songTitleText: StringDesc,
        val difficultyClassText: StringDesc,
        val difficultyClassColor: ColorResource,
        val difficultyNumberText: StringDesc,
        val exScoreText: StringDesc,
        val reminder: StringDesc? = null,
    ) : UITrialSessionContent() {

        data class Item(
            val jacketUrl: String?,
            val topText: StringDesc?,
            val bottomBoldText: StringDesc?,
            val bottomTagColor: ColorDesc,
            val tapAction: TrialSessionInput?,
        )
    }
}

/**
 * Describes the content of the song detail bottom sheet, shown
 * when a single song needs editing.
 */
sealed class UITrialBottomSheet {

    open val onDismissAction: TrialSessionInput = TrialSessionInput.HideBottomSheet

    /**
     * Describes the state where the bottom sheet should be used
     * for image capture.
     */
    data class ImageCapture(val index: Int?) : UITrialBottomSheet() {

        fun createResultAction(uri: String) =
            index?.let { index -> TrialSessionInput.PhotoTaken(uri, index) }
                ?: TrialSessionInput.ResultsPhotoTaken(uri)
    }

    /**
     * Placeholder for details panel used only in KM.
     */
    data class DetailsPlaceholder(
        override val onDismissAction: TrialSessionInput = TrialSessionInput.HideBottomSheet,
    ) : UITrialBottomSheet()

    /**
     * Describes the state where the bottom sheet should be used
     * for entering score details.
     */
    data class Details(
        val imagePath: String,
        val fields: List<List<Field>>,
        val isEdit: Boolean,
        val shortcuts: List<Shortcut>,
        override val onDismissAction: TrialSessionInput = TrialSessionInput.HideBottomSheet,
    ) : UITrialBottomSheet() {
    }

    /**
     * Defines a single field on the sheet.
     * @param id the ID of the field, used when communicating input
     *  back to KM when clicking Submit.
     * @param weight the amount of space this field should take up,
     *  relative to the other fields.
     * @param text the initial text to show in the field. Any
     *  changes to text should be tracked in the native code and
     *  submitted using [generateSubmitAction].
     * @param label the text to show by the field to identify it.
     */
    data class Field(
        val id: String,
        val text: String,
        val label: StringDesc,
        val enabled: Boolean = true,
        val weight: Float = 1f,
        val hasError: Boolean = false,
    )

    /**
     * Defines a Shortcut action that can be taken to set some data
     * automatically, with the intent to cut down on redundant asks.
     */
    data class Shortcut(
        val itemText: StringDesc,
        val action: TrialSessionInput,
    )
}