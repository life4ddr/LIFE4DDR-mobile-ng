package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.DifficultyClass
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
