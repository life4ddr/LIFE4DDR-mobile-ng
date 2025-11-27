package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacement
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.image.asImageDesc
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PlacementListItem(
    data: UIPlacement,
    expanded: Boolean = false,
    onExpand: () -> Unit = {},
    onPlacementSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val arrowRotationDegrees by remember {
        derivedStateOf {
            if (expanded) 180f else 0f
        }
    }
    Column(modifier = modifier.clickable { onExpand() }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            RankImage(
                rank = data.rankIcon,
                modifier = Modifier.size(64.dp)
            )
            SizedSpacer(16.dp)
            Text(
                text = stringResource(data.placementName),
                style = MaterialTheme.typography.headlineMedium,
                color = colorResource(data.color),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = data.difficultyRangeString,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                painter = painterResource(MR.images.arrow_drop_down),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(arrowRotationDegrees),
                contentDescription = if (expanded) "expanded" else "collapsed",
            )
        }
        AnimatedVisibility(expanded) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            ) {
                data.songs.forEach { song ->
                    PlacementSongItem(
                        data = song,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                SizedSpacer(16.dp)
                TextButton(
                    onClick = onPlacementSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(MR.strings.placement_start),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}