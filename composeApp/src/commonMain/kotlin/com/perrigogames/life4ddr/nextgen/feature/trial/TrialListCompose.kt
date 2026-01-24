package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.trials.data.Trial
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UIPlacementBanner
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialJacket
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialList
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialScrapeProgress
import com.perrigogames.life4ddr.nextgen.feature.trials.viewmodel.TrialListViewModel
import com.perrigogames.life4ddr.nextgen.view.JacketCorner
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.desc.image.ImageDescResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TrialListScreen(
    modifier: Modifier = Modifier,
    onTrialSelected: (Trial) -> Unit = {},
    onPlacementsSelected: () -> Unit = {},
) {
    val viewModel = koinViewModel<TrialListViewModel>()
    val state by viewModel.state.collectAsState()
    val scrapeState by viewModel.scrapeState.collectAsState()
    var lastGoodScrapeState by remember { mutableStateOf<UITrialScrapeProgress?>(null) }
    var quickAddDialogTrial by remember { mutableStateOf<Trial?>(null) }
    var deleteRecordsConfirmationTrial by remember { mutableStateOf<Trial?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(),
        modifier = modifier,
        floatingActionButton = {
            AnimatedVisibility(
                visible = scrapeState == null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.scrapeTrialData() },
                ) {
                    Icon(
                        painterResource(MR.images.sync),
                        contentDescription = "Refresh trials from LIFE4 website"
                    )
                }
            }
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            state.placementBanner?.let { banner ->
                PlacementBanner(
                    banner = banner,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = Paddings.MEDIUM)
                        .padding(top = Paddings.LARGE),
                    onPlacementsSelected = onPlacementsSelected,
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                TrialJacketList(
                    displayList = state.trials, // FIXME
                    onTrialSelected = onTrialSelected,
                    onTrialQuickAddSelected = { quickAddDialogTrial = it },
                    onTrialClearRecordsSelected = { deleteRecordsConfirmationTrial = it },
                    modifier = Modifier.fillMaxSize(),
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    AnimatedVisibility(
                        visible = scrapeState != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        if (scrapeState != null) {
                            lastGoodScrapeState = scrapeState
                        }
                        TrialScrapeProgress(
                            data = lastGoodScrapeState!!,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    quickAddDialogTrial?.let { trial ->
        TrialQuickAddDialog(
            availableRanks = trial.goals?.map { it.rank } ?: listOf(TrialRank.COPPER),
            onSubmit = { rank, exScore ->
                viewModel.addTrialPlay(trial, rank, exScore)
                quickAddDialogTrial = null
            },
            onDismiss = { quickAddDialogTrial = null }
        )
    }

    deleteRecordsConfirmationTrial?.let { trial ->
        AlertDialog(
            onDismissRequest = { deleteRecordsConfirmationTrial = null },
            title = { Text(MR.strings.trial_clear_records_title.desc().localized()) },
            text = { Text(
                StringDesc.ResourceFormatted(
                    MR.strings.trial_clear_records_body,
                    trial.name
                ).localized()
            ) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearTrialData(trial.id)
                        deleteRecordsConfirmationTrial = null
                    },
                    content = { Text(MR.strings.yes.desc().localized()) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteRecordsConfirmationTrial = null },
                    content = { Text(MR.strings.no.desc().localized()) }
                )
            },
        )
    }
}

@Composable
fun PlacementBanner(
    banner: UIPlacementBanner,
    modifier: Modifier = Modifier,
    onPlacementsSelected: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .clickable { onPlacementsSelected() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = banner.text.localized(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        SizedSpacer(Paddings.MEDIUM)
        banner.ranks.forEach { rank ->
            RankImage(
                rank = rank,
                size = 24.dp,
                onClick = null
            )
        }
    }
}

@Composable
fun TrialJacketList(
    displayList: List<UITrialList.Item>,
    onTrialSelected: (Trial) -> Unit,
    onTrialQuickAddSelected: (Trial) -> Unit,
    onTrialClearRecordsSelected: (Trial) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        contentPadding = PaddingValues(Paddings.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Paddings.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Paddings.MEDIUM),
        modifier = modifier,
    ) {
        displayList.forEach { displayItem ->
            when (displayItem) {
                is UITrialList.Item.Header -> item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    Text(
                        text = displayItem.text.localized(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                is UITrialList.Item.Trial -> item {
                    TrialJacket(
                        viewData = displayItem.data,
                        onClick = { onTrialSelected(displayItem.data.trial) },
                        onQuickAddSelected = { onTrialQuickAddSelected(displayItem.data.trial) },
                        onClearRecordsSelected = { onTrialClearRecordsSelected(displayItem.data.trial) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrialJacket(
    viewData: UITrialJacket,
    onClick: () -> Unit,
    onQuickAddSelected: () -> Unit,
    onClearRecordsSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var contextMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = { contextMenuExpanded = true },
            )
            .then(modifier),
    ) {
        Image(
            painter = (viewData.trial.coverResource as? ImageDescResource)?.let {
                painterResource(it.resource)
            } ?: painterResource(MR.images.trial_default),
            contentDescription = null,
            modifier = Modifier.aspectRatio(1f)
                .alpha(viewData.viewAlpha)
        )
        viewData.trial.difficulty?.let { diffNum ->
            TrialDifficulty(
                difficulty = diffNum,
                modifier = Modifier.align(Alignment.TopStart)
                    .padding(Paddings.SMALL)
            )
        }
        AnimatedContent(
            targetState = viewData.cornerType,
            transitionSpec = { fadeIn() togetherWith  fadeOut() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) { type ->
            JacketCorner(type)
        }

        val rank = viewData.rank
        val exScore = viewData.exScore
        if (rank != null && exScore != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Paddings.SMALL),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Paddings.SMALL)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.8f
                        ),
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(Paddings.SMALL),
            ) {
                RankImage(
                    rank = rank.parent,
                    size = 32.dp,
                )
                Text(
                    text = exScore.localized(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        DropdownMenu(
            expanded = contextMenuExpanded,
            onDismissRequest = { contextMenuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Quick Add") },
                onClick = {
                    onQuickAddSelected()
                    contextMenuExpanded = false
                },
            )
            DropdownMenuItem(
                text = { Text("Clear Records") },
                onClick = {
                    onClearRecordsSelected()
                    contextMenuExpanded = false
                }
            )
        }
    }
}

@Composable
fun TrialDifficulty(
    difficulty: Int,
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                shape = CircleShape
            ),
    ) {
        Text(
            text = difficulty.toString(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TrialScrapeProgress(
    data: UITrialScrapeProgress,
    modifier: Modifier = Modifier,
) {
    Card (
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = data.topText.localized(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            SizedSpacer(8.dp)
            Text(
                text = data.bottomText.localized(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            data.progress?.let { progress ->
                SizedSpacer(16.dp)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
