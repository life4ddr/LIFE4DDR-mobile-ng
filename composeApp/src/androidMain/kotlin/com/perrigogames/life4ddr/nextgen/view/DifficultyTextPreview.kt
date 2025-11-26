package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.BASIC
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.BEGINNER
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.CHALLENGE
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.DIFFICULT
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.EXPERT
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DifficultyTextPreview() {
    LIFE4Theme {
        Column {
            DifficultyText(BEGINNER)
            DifficultyText(BEGINNER, difficultyNumber = 1)
            DifficultyText(BASIC)
            DifficultyText(BASIC, difficultyNumber = 4)
            DifficultyText(DIFFICULT)
            DifficultyText(DIFFICULT, difficultyNumber = 9)
            DifficultyText(EXPERT)
            DifficultyText(EXPERT, difficultyNumber = 13)
            DifficultyText(CHALLENGE)
            DifficultyText(CHALLENGE, difficultyNumber = 18)
        }
    }
}