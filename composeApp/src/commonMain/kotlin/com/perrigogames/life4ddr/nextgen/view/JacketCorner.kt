package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialJacketCorner
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun JacketCorner(
    corner: TrialJacketCorner,
    modifier: Modifier = Modifier,
) {
    if (corner == TrialJacketCorner.NONE) return
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 100.dp, height = 25.dp)
            .aspectRatio(1f)
    ) {
        Image(
            painter = painterResource(MR.images.trial_corner),
            colorFilter = ColorFilter.tint(when (corner) {
                TrialJacketCorner.NEW -> colorResource(MR.colors.corner_new)
                TrialJacketCorner.EVENT -> colorResource(MR.colors.corner_event)
                TrialJacketCorner.NONE -> error("Cannot make a JacketCorner for type NONE")
            }),
            contentDescription = "${corner.name} tag"
        )
        Text(
            text = stringResource(when (corner) {
                TrialJacketCorner.NEW -> MR.strings.new_tag
                TrialJacketCorner.EVENT -> MR.strings.event_tag
                TrialJacketCorner.NONE -> error("Cannot make a JacketCorner for type NONE")
            }),
            style = MaterialTheme.typography.labelLarge,
            color = when (corner) {
                TrialJacketCorner.NEW -> Color.White
                TrialJacketCorner.EVENT -> Color.Black
                TrialJacketCorner.NONE -> error("Cannot make a JacketCorner for type NONE")
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp)
        )
    }
}

@Composable
@Preview(heightDp = 24)
fun JacketCornerPreview() {
    LIFE4Theme {
        Row {
            JacketCorner(TrialJacketCorner.NEW)
            JacketCorner(TrialJacketCorner.EVENT)
        }
    }
}