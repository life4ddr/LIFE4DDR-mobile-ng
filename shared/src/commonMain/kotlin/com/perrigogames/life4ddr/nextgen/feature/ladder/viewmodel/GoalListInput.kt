package com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel

sealed class GoalListInput {
    sealed class OnGoal : GoalListInput() {
        abstract val id: Long

        data class ToggleComplete(override val id: Long) : OnGoal()
        data class ToggleHidden(override val id: Long) : OnGoal()
        data class ToggleExpanded(override val id: Long) : OnGoal()
    }

    data object ShowSubstitutions : GoalListInput()
}