package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LadderRankClassTheme
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelInput
import com.perrigogames.life4ddr.nextgen.feature.placements.view.UIPlacementDetails
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementDetailsEvent
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementDetailsInput
import com.perrigogames.life4ddr.nextgen.feature.placements.viewmodel.PlacementDetailsViewModel
import com.perrigogames.life4ddr.nextgen.feature.trial.CameraBottomSheetContent
import com.perrigogames.life4ddr.nextgen.feature.trials.view.UITrialSong
import com.perrigogames.life4ddr.nextgen.view.AutoResizedText
import com.perrigogames.life4ddr.nextgen.view.LargeCTAButton
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PlacementDetailsScreen(
    modifier: Modifier = Modifier,
    placementId: String,
    onBackPressed: () -> Unit = {},
    onNavigateToMainScreen: (String?) -> Unit = {},
) {
    val viewModel = koinViewModel<PlacementDetailsViewModel> { parametersOf(placementId) }
    val scope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState()
    var dialogData by remember { mutableStateOf<PlacementDetailsEvent.ShowTooltip?>(null) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
        )
    )

    fun hideBottomSheet() = scope.launch {
        scaffoldState.bottomSheetState.hide()
    }

    BackHandler {
        onBackPressed()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PlacementDetailsEvent.Back -> {
                    onBackPressed()
                }
                is PlacementDetailsEvent.NavigateToMainScreen -> {
                    onNavigateToMainScreen(event.submissionUrl)
                }
                PlacementDetailsEvent.ShowCamera -> {
                    scaffoldState.bottomSheetState.expand()
                }
                is PlacementDetailsEvent.ShowTooltip -> {
                    dialogData = event
                }
            }
        }
    }

    dialogData?.let { data ->
        val onAction = { viewModel.handleAction(data.ctaAction) }
        AlertDialog(
            onDismissRequest = onAction,
            confirmButton = {
                TextButton(onClick = onAction) {
                    Text(data.ctaText.localized())
                }
            },
            title = { Text(text = data.title.localized()) },
            text = { Text(text = data.message.localized()) }
        )
    }

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                BackHandler {
                    hideBottomSheet()
                }
                CameraBottomSheetContent { uri ->
                    hideBottomSheet()
                    viewModel.handleAction(PlacementDetailsInput.PictureTaken(uri))
                }
            }
        }
    ) {
        PlacementDetailsContent(
            viewData = state.value,
            modifier = Modifier.fillMaxSize(),
            onInput = viewModel::handleAction,
        )
    }
}

@Composable
fun PlacementDetailsContent(
    viewData: UIPlacementDetails,
    modifier: Modifier = Modifier,
    onInput: (PlacementDetailsInput) -> Unit,
) {
    LadderRankClassTheme(ladderRankClass = viewData.rankIcon.group) {
        Box(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = { onInput(PlacementDetailsInput.Back) },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Image(
                    painter = painterResource(MR.images.arrow_back),
                    contentDescription = "Back"
                )
            }

            Column(
                modifier = Modifier.matchParentSize()
                    .padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RankImage(
                        rank = viewData.rankIcon,
                        size = 64.dp,
                    )
                    Text(
                        text = stringResource(viewData.rankIcon.group.nameRes),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colorResource(viewData.rankIcon.colorRes),
                    )
                }
                SizedSpacer(32.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    viewData.descriptionPoints.forEachIndexed { index, text ->
                        if (index > 0) {
                            SizedSpacer(8.dp)
                        }
                        Text(
                            text = text.localized(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    SizedSpacer(32.dp)

                    viewData.songs.forEach { song ->
                        PlacementDetailsSongItem(
                            data = song,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        SizedSpacer(16.dp)
                    }
                }

                LargeCTAButton(
                    text = viewData.ctaText.localized(),
                    onClick = { onInput(viewData.ctaAction) }
                )
            }
        }
    }
}

@Composable
fun PlacementDetailsSongItem(
    data: UITrialSong,
    modifier: Modifier = Modifier,
) {
    PlacementSongItem(
        data = data,
        modifier = modifier,
        jacketSize = 96.dp,
        detailSpacing = 0.dp,
        titleTextLines = 3,
        titleTextStyle = MaterialTheme.typography.titleLarge,
        mixTextStyle = MaterialTheme.typography.titleMedium,
    )
}

@Composable
fun PlacementSongItem(
    data: UITrialSong,
    modifier: Modifier = Modifier,
    jacketSize: Dp = 64.dp,
    detailSpacing: Dp = 16.dp,
    titleTextLines: Int = 1,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    mixTextStyle: TextStyle = MaterialTheme.typography.titleSmall,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(data.jacketUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.size(jacketSize)
        )
        SizedSpacer(16.dp)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (titleTextLines == 1) {
                AutoResizedText(
                    text = data.songNameText.localized(),
                    style = titleTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = data.songNameText.localized(),
                    style = titleTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = titleTextLines,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = data.subtitleText.localized(),
                style = mixTextStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
        SizedSpacer(detailSpacing)
        PlacementDifficultySurface(
            data = data,
            modifier = Modifier.width(50.dp)
        )
    }
}
