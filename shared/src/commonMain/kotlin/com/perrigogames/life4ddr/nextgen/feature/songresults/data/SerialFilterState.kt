package com.perrigogames.life4ddr.nextgen.feature.songresults.data

import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import kotlinx.serialization.Serializable

@Serializable
data class SerialFilterState(
    val selectedPlayStyle: PlayStyle,
    val difficultyClassSelection: List<DifficultyClass>,
    val difficultyNumberBottom: Int,
    val difficultyNumberTop: Int,
    val clearTypeBottom: Int,
    val clearTypeTop: Int,
    val scoreBottom: Int,
    val scoreTop: Int,
    val filterIgnored: Boolean,
) {

    fun toFilterState() = FilterState(
        selectedPlayStyle = selectedPlayStyle,
        difficultyClassSelection = difficultyClassSelection,
        difficultyNumberRange = difficultyNumberBottom..difficultyNumberTop,
        clearTypeRange = clearTypeBottom..clearTypeTop,
        scoreRange = scoreBottom..scoreTop,
        filterIgnored = filterIgnored
    )
}

fun FilterState.toSerialFilterState() = SerialFilterState(
    selectedPlayStyle = chartFilter.selectedPlayStyle,
    difficultyClassSelection = chartFilter.difficultyClassSelection,
    difficultyNumberBottom = chartFilter.difficultyNumberRange.first,
    difficultyNumberTop = chartFilter.difficultyNumberRange.last,
    clearTypeBottom = resultFilter.clearTypeRange.first,
    clearTypeTop = resultFilter.clearTypeRange.last,
    scoreBottom = resultFilter.scoreRange.first,
    scoreTop = resultFilter.scoreRange.last,
    filterIgnored = resultFilter.filterIgnored,
)
