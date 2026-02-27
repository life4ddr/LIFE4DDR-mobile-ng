package com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle

sealed class FilterPanelInput {
    data class SelectPlayStyle(val playStyle: PlayStyle): FilterPanelInput()
    data class ToggleDifficultyClass(val difficultyClass: DifficultyClass, val selected: Boolean): FilterPanelInput()
    data class SetDifficultyNumber(val value: Int): FilterPanelInput()
    data class SetDifficultyNumberRange(val range: IntRange): FilterPanelInput() {
        constructor(min: Int, max: Int) : this(min..max)
    }
    data class ToggleDifficultyNumberRange(val enabled: Boolean): FilterPanelInput()
    data class SetClearTypeRange(val range: IntRange): FilterPanelInput() {
        constructor(min: Int, max: Int) : this(min..max)
    }
    data class SetScoreRangeMin(val amount: Int): FilterPanelInput()
    data class SetScoreRangeMax(val amount: Int): FilterPanelInput()
    data object ResetFilter : FilterPanelInput()
}
