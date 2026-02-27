package com.perrigogames.life4ddr.nextgen.feature.songresults.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.data.GameConstants.HIGHEST_DIFFICULTY
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.manager.FilterFlags
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.FilterPanelInput
import com.perrigogames.life4ddr.nextgen.util.CompoundIntRange
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

data class UIFilterView(
    val playStyleSelector: List<UIPlayStyleSelection>? = null,
    val difficultyClassSelector: List<UIDifficultyClassSelection>? = null,
    val difficultyNumberTitle: StringDesc = StringDesc.Resource(MR.strings.label_difficulty_number),
    val difficultyNumberUsesRange: Boolean,
    val difficultyNumberUsesRangeText: StringDesc = StringDesc.Resource(MR.strings.action_show_difficulty_number_range_short),
    val difficultyNumberUsesRangeInput: FilterPanelInput,
    val difficultyNumberRange: CompoundIntRange = CompoundIntRange(1..HIGHEST_DIFFICULTY),
    val clearTypeTitle: StringDesc = StringDesc.Resource(MR.strings.label_clear_type),
    val clearTypeRange: CompoundIntRange = CompoundIntRange(0 until ClearType.entries.size),
    val scoreRangeBottomValue: Int? = null,
    val scoreRangeBottomHint: StringDesc = StringDesc.Raw("0"),
    val scoreRangeTopValue: Int? = null,
    val scoreRangeTopHint: StringDesc = StringDesc.Raw("1000000"),
    val scoreRangeAllowed: IntRange = 0..GameConstants.MAX_SCORE,
) {
    val scoreRangeBottomString: String? get() = scoreRangeBottomValue?.toString()
    val scoreRangeTopString: String? get() = scoreRangeTopValue?.toString()

    constructor(
        settingsFlags: FilterFlags = FilterFlags(),
        selectedPlayStyle: PlayStyle = PlayStyle.SINGLE,
        selectedDifficultyClasses: List<DifficultyClass> = DifficultyClass.entries,
        difficultyNumberSelection: IntRange? = null,
        clearTypeSelection: IntRange? = null,
        scoreRangeBottomValue: Int? = null,
        scoreRangeTopValue: Int? = null
    ) : this(
        playStyleSelector = if (settingsFlags.showPlayStyleSelector) {
            PlayStyle.entries.map {
                UIPlayStyleSelection(
                    text = it.uiName,
                    selected = it == selectedPlayStyle,
                    action = FilterPanelInput.SelectPlayStyle(it)
                )
            }
        } else {
            null
        },
        difficultyClassSelector = if (settingsFlags.showDiffClasses) {
            DifficultyClass.entries.mapNotNull { diff ->
                if (selectedPlayStyle == PlayStyle.DOUBLE && diff == DifficultyClass.BEGINNER) {
                    return@mapNotNull null
                }
                UIDifficultyClassSelection(
                    text = StringDesc.Raw(selectedPlayStyle.aggregateString(diff)),
                    selected = selectedDifficultyClasses.contains(diff),
                    action = FilterPanelInput.ToggleDifficultyClass(diff, !selectedDifficultyClasses.contains(diff))
                )
            }
        } else {
            null
        },
        difficultyNumberUsesRange = settingsFlags.useDifficultyRange,
        difficultyNumberUsesRangeInput = FilterPanelInput.ToggleDifficultyNumberRange(!settingsFlags.useDifficultyRange),
        difficultyNumberRange = CompoundIntRange(
            outerRange = 1..HIGHEST_DIFFICULTY,
            innerRange = difficultyNumberSelection
        ),
        clearTypeRange = CompoundIntRange(
            outerRange = 0 until ClearType.entries.size,
            innerRange = clearTypeSelection
        ),
        scoreRangeBottomValue = scoreRangeBottomValue,
        scoreRangeTopValue = scoreRangeTopValue,
    )
}

data class UIPlayStyleSelection(
    val text: StringDesc,
    val selected: Boolean,
    val action: FilterPanelInput
)

data class UIDifficultyClassSelection(
    val text: StringDesc,
    val selected: Boolean,
    val action: FilterPanelInput
)

fun FilterState.toUIFilterView(
    settingsFlags: FilterFlags
) = UIFilterView(
    settingsFlags = settingsFlags,
    selectedPlayStyle = chartFilter.selectedPlayStyle,
    selectedDifficultyClasses = chartFilter.difficultyClassSelection,
    difficultyNumberSelection = chartFilter.difficultyNumberRange,
    clearTypeSelection = resultFilter.clearTypeRange,
    scoreRangeBottomValue = resultFilter.scoreRange.first.takeIf { it != 0 },
    scoreRangeTopValue = resultFilter.scoreRange.last.takeIf { it != GameConstants.MAX_SCORE },
)
