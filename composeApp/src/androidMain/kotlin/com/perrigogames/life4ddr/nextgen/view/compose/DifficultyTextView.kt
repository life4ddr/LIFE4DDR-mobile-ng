package com.perrigogames.life4ddr.nextgen.view.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass.*
import com.perrigogames.life4ddr.nextgen.enums.colorRes
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun DifficultyText(
    difficultyClass: DifficultyClass,
    modifier: Modifier = Modifier,
    difficultyNumber: Int? = null,
) {
    val text = difficultyNumber?.let { diffNumber ->
        stringResource(MR.strings.difficulty_string_format, difficultyClass.toString(), diffNumber)
    } ?: difficultyClass.toString()

    Text(
        text = text,
        color = colorResource(difficultyClass.colorRes),
        modifier = modifier,
    )
}

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