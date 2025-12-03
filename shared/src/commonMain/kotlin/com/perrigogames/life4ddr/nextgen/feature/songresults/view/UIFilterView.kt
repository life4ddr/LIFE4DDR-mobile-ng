package com.perrigogames.life4ddr.nextgen.feature.songresults.view

import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants.HIGHEST_DIFFICULTY
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.songresults.data.FilterState
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.FilterPanelInput
import com.perrigogames.life4ddr.nextgen.util.CompoundIntRange
import dev.icerock.moko.resources.desc.Raw
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc

data class UIFilterView(
    val playStyleSelector: List<UIPlayStyleSelection>? = null,
    val difficultyClassSelector: List<UIDifficultyClassSelection>,
    val difficultyNumberTitle: StringDesc = StringDesc.Resource(MR.strings.label_difficulty_number),
    val difficultyNumberRange: CompoundIntRange = CompoundIntRange(1..HIGHEST_DIFFICULTY),
    val clearTypeTitle: StringDesc = StringDesc.Resource(MR.strings.label_clear_type),
    val clearTypeRange: CompoundIntRange = CompoundIntRange(0 until ClearType.entries.size),
    val scoreRangeBottomValue: Int? = null,
    val scoreRangeBottomHint: StringDesc = StringDesc.Raw("0"),
    val scoreRangeTopValue: Int? = null,
    val scoreRangeTopHint: StringDesc = StringDesc.Raw("1,000,000"),
) {

    constructor(
        showPlayStyleSelector: Boolean = true,
        selectedPlayStyle: PlayStyle = PlayStyle.SINGLE,
        selectedDifficultyClasses: List<DifficultyClass> = DifficultyClass.entries,
        difficultyNumberSelection: IntRange? = null,
        clearTypeSelection: IntRange? = null,
        scoreRangeBottomValue: Int? = null,
        scoreRangeTopValue: Int? = null
    ) : this(
        playStyleSelector = if (showPlayStyleSelector) {
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
        difficultyClassSelector = DifficultyClass.entries.mapNotNull { diff ->
            if (selectedPlayStyle == PlayStyle.DOUBLE && diff == DifficultyClass.BEGINNER) {
                return@mapNotNull null
            }
            UIDifficultyClassSelection(
                text = StringDesc.Raw(selectedPlayStyle.aggregateString(diff)),
                selected = selectedDifficultyClasses.contains(diff),
                action = FilterPanelInput.ToggleDifficultyClass(diff, !selectedDifficultyClasses.contains(diff))
            )
        },
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

fun FilterState.toUIFilterView(showPlayStyleSelector: Boolean) = UIFilterView(
    showPlayStyleSelector = showPlayStyleSelector,
    selectedPlayStyle = chartFilter.selectedPlayStyle,
    selectedDifficultyClasses = chartFilter.difficultyClassSelection,
    difficultyNumberSelection = chartFilter.difficultyNumberRange,
    clearTypeSelection = resultFilter.clearTypeRange,
    scoreRangeBottomValue = resultFilter.scoreRange.first,
    scoreRangeTopValue = resultFilter.scoreRange.last,
)
