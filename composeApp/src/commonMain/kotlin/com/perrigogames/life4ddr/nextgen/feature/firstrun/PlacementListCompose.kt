package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LadderRankClassTheme
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacement
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementListScreen
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementSkipConfirmation
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementListInput
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementListContent(
    data: UIPlacementListScreen,
    modifier: Modifier = Modifier,
    onInput: (PlacementListInput) -> Unit = {}
) {
    var selectedPlacement by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = data.titleText.localized(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = Paddings.LARGE, start = Paddings.LARGE)
        )
        SizedSpacer(16.dp)
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = data.headerText.localized(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            items(data.placements) { placement ->
                SizedSpacer(16.dp)
                LadderRankClassTheme(ladderRankClass = placement.rankIcon.group) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.large
                    ) {
                        PlacementListItem(
                            data = placement,
                            expanded = selectedPlacement == placement.id,
                            onExpand = {
                                selectedPlacement = when {
                                    selectedPlacement == placement.id -> null
                                    else -> placement.id
                                }
                            },
                            onInput = onInput
                        )
                    }
                }
            }
        }

        SizedSpacer(Paddings.LARGE)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onInput(data.ranksButtonInput) },
        ) {
            Text(
                text = data.ranksButtonText.localized(),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        TextButton(
            onClick = { onInput(data.skipButtonInput) },
        ) {
            Text(
                text = data.skipButtonText.localized(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        SizedSpacer(Paddings.LARGE)
    }
    data.skipConfirmation?.let {
        PlacementSkipConfirmDialog(it)
    }
}

@Composable
fun PlacementSkipConfirmDialog(
    data: UIPlacementSkipConfirmation,
    onInput: (PlacementListInput) -> Unit = {}
) {
    AlertDialog(
        title = {
            Text(
                text = data.titleText.localized(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = data.bodyText.localized(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        onDismissRequest = { onInput(data.cancelButtonInput) },
        confirmButton = {
            TextButton(
                onClick = { onInput(data.confirmButtonInput) }
            ) { Text(data.confirmButtonText.localized()) }
        },
        dismissButton = {
            TextButton(
                onClick = { onInput(data.cancelButtonInput) }
            ) { Text(data.cancelButtonText.localized()) }
        }
    )
}

@Composable
fun PlacementListItem(
    data: UIPlacement,
    expanded: Boolean = false,
    onExpand: () -> Unit = {},
    onInput: (PlacementListInput) -> Unit,
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
                    onClick = { onInput(data.selectedInput) },
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
