package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import dev.icerock.moko.resources.compose.colorResource

@Composable
fun PlacementDifficultySurface(
    data: UITrialSong,
    modifier: Modifier = Modifier,
    difficultyClassTextStyle: TextStyle = MaterialTheme.typography.titleSmall,
    difficultyNumberTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = data.chartString,
                style = difficultyClassTextStyle,
                color = colorResource(data.difficultyClass.colorRes)
            )
            Text(
                text = data.difficultyText,
                style = difficultyNumberTextStyle,
                color = colorResource(data.difficultyClass.colorRes)
            )
        }
    }
}