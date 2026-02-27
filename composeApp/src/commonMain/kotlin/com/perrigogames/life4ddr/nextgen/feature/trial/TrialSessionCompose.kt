package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.trialsession.data.InProgressTrialSession
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.*
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionEvent
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionInput
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionViewModel
import com.perrigogames.life4ddr.nextgen.util.MokoImage
import com.perrigogames.life4ddr.nextgen.util.ViewState
import com.perrigogames.life4ddr.nextgen.view.AutoResizedText
import com.perrigogames.life4ddr.nextgen.view.LargeCTAButton
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import com.perrigogames.life4ddr.nextgen.view.SystemBackButton
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TrialSessionScreen(
    trialId: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onSubmit: (InProgressTrialSession) -> Unit ,
) {
    val viewModel = koinViewModel<TrialSessionViewModel> { parametersOf(trialId) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val viewState by viewModel.state.collectAsState()
    val exScoreBar by viewModel.uiExScoreFlow.collectAsState()
    val bottomSheetState by viewModel.bottomSheetState.collectAsState()
    var dialogData by remember { mutableStateOf<TrialSessionEvent.ShowWarningDialog?>(null) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
        )
    )
    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            focusManager.clearFocus()
            scaffoldState.bottomSheetState.hide()
        }
    }
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
            viewModel.handleAction(TrialSessionInput.HideBottomSheet)
        }
    }

    BackHandler {
        viewModel.handleAction(TrialSessionInput.AttemptToClose())
    }

    when (val viewData = viewState) {
        ViewState.Loading -> {
            Text("Loading...")
        }
        is ViewState.Success<UITrialSession> -> {
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                modifier = modifier,
                sheetPeekHeight = 0.dp,
                sheetContent = {
                    fun sendBottomSheetAction() {
                        coroutineScope.launch {
                            bottomSheetState?.onDismissAction?.let {
                                viewModel.handleAction(it)
                            }
                        }
                    }
                    when (val state = bottomSheetState) {
                        is UITrialBottomSheet.ImageCapture -> {
                            BackHandler { sendBottomSheetAction() }
                            ImageCaptureView(
                                onImageCaptured = { path ->
                                    viewModel.handleAction(state.createResultAction(path))
                                },
                                onClose = {
                                    coroutineScope.launch {
                                        scaffoldState.bottomSheetState.hide()
                                    }
                                }
                            )
                        }
                        is UITrialBottomSheet.Details -> {
                            BackHandler { sendBottomSheetAction() }
                            SongEntryBottomSheetContentAndroid(
                                viewData = state,
                                onAction = {
                                    viewModel.handleAction(it)
                                },
                            )
                        }
                        is UITrialBottomSheet.DetailsPlaceholder,
                        null -> {}
                    }
                },
            ) { padding ->
                TrialSessionContent(
                    viewData = viewData.data,
                    exScoreBar = exScoreBar,
                    modifier = Modifier.padding(padding),
                    onAction = { viewModel.handleAction(it) }
                )
            }
        }
        is ViewState.Error<Unit> -> {
            Text("Error loading Trial with ID $trialId")
        }
    }

    dialogData?.let { data ->
        AlertDialog(
            onDismissRequest = { dialogData = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogData = null
                        viewModel.handleAction(data.ctaConfirmInput)
                    }
                ) {
                    Text(data.ctaConfirmText.localized())
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { dialogData = null }
                ) {
                    Text(data.ctaCancelText.localized())
                }
            },
            title = { Text(text = data.title.localized()) },
            text = { Text(text = data.body.localized()) }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                TrialSessionEvent.Close -> onClose()
                is TrialSessionEvent.SubmitAndClose -> {
                    onClose()
                    onSubmit(event.session)
                }
                TrialSessionEvent.HideBottomSheet -> {
                    focusManager.clearFocus()
                    scaffoldState.bottomSheetState.hide()
                }
                is TrialSessionEvent.ShowWarningDialog -> {
                    dialogData = event
                }
            }
        }
    }
}

@Composable
fun TrialSessionContent(
    viewData: UITrialSession,
    exScoreBar: UIEXScoreBar,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit = {},
) {
    Box(
        modifier = modifier
    ) {
        MokoImage(
            desc = viewData.backgroundImage,
            modifier = Modifier.matchParentSize()
                .blur(radius = 12.dp, BlurredEdgeTreatment.Unbounded),
            alignment = Alignment.Center,
            contentScale = ContentScale.FillHeight,
            alpha = 0.3f,
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .systemBarsPadding()
                .navigationBarsPadding()
        ) {
            TrialSessionHeader(
                viewData = viewData,
                exScoreBar = exScoreBar,
                onAction = onAction,
            )
            SizedSpacer(16.dp)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = viewData.content, label = "content",
                    contentKey = { it::class },
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }) togetherWith
                                slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                    },
                    modifier = Modifier.matchParentSize()
                ) { content ->
                    Column {
                        when (content) {
                            is UITrialSessionContent.Summary -> {
                                SummaryContent(
                                    viewData = content,
                                    onAction = onAction,
                                )
                            }
                            is UITrialSessionContent.SongFocused -> {
                                SongFocusedContent(
                                    viewData = content,
                                    onAction = onAction,
                                    modifier = Modifier.padding(bottom = 48.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = viewData.footer,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    FooterWidget(
                        footer = viewData.footer,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

@Composable
fun FooterWidget(
    footer: UITrialSession.Footer,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit,
) {
    when (footer) {
        is UITrialSession.Footer.Button -> {
            LargeCTAButton(
                text = footer.buttonText.localized(),
                onClick = { onAction(footer.buttonAction) },
                modifier = modifier,
            )
        }
        is UITrialSession.Footer.Message -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = footer.message.localized(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrialSessionHeader(
    viewData: UITrialSession,
    exScoreBar: UIEXScoreBar,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit = {},
) {
    var showManualScoreEntry by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SystemBackButton { onAction(TrialSessionInput.AttemptToClose()) }
            Text(
                text = viewData.trialTitle.localized(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = viewData.trialLevel.localized(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { showManualScoreEntry = true }
                )
            )
        }

        SizedSpacer(16.dp)
        AnimatedContent(targetState = viewData.targetRank.state == UITargetRank.State.COMPACT) { achieved ->
            if (achieved) {
                RankDisplay(
                    viewData = viewData.targetRank,
                    showSelectorIcon = false,
                    rankImageSize = 64.dp,
                    textStyle = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                RankSelector(
                    viewData = viewData.targetRank,
                    rankSelected = { onAction(TrialSessionInput.ChangeTargetRank(it)) }
                )
            }
        }
        SizedSpacer(16.dp)
        EXScoreBar(
            viewData = exScoreBar,
            onInput = onAction,
        )

        val availableRanks = viewData.targetRank.availableRanks
        if (showManualScoreEntry && availableRanks != null) {
            TrialQuickAddDialog(
                availableRanks = availableRanks,
                onSubmit = { rank, ex -> onAction(TrialSessionInput.ManualScoreEntry(rank, ex)) },
                onDismiss = { }
            )
        }
    }
}

@Composable
fun SummaryContent(
    viewData: UITrialSessionContent.Summary,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .widthIn(max = 800.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(viewData.items) { item ->
            SummaryJacketItem(viewData = item, onAction = onAction)
        }
        item {
            SizedSpacer(24.dp)
        }
    }
}

@Composable
fun SongFocusedContent(
    viewData: UITrialSessionContent.SongFocused,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            viewData.items.forEachIndexed { index, item ->
                InProgressJacketItem(
                    viewData = item,
                    onClick = item.tapAction?.let { { onAction(it) } }
                )
            }
        }

        SizedSpacer(32.dp)

        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(viewData.focusedJacketUrl)
//                .fallback(MR.images.trial_default)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentScale = ContentScale.Crop,
        )

        SizedSpacer(16.dp)

        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AutoResizedText(
                    text = viewData.songTitleText.localized(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )

                SizedSpacer(8.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = viewData.difficultyClassText.localized(),
                        color = colorResource(viewData.difficultyClassColor),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = viewData.difficultyNumberText.localized(),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = viewData.exScoreText.localized(),
                    )
                }
                viewData.reminder?.let { reminder ->
                    SizedSpacer(8.dp)
                    Text(
                        text = reminder.localized(),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        SizedSpacer(32.dp)
    }
}

@Composable
fun EXScoreBar(
    viewData: UIEXScoreBar,
    modifier: Modifier = Modifier,
    onInput: (TrialSessionInput) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = viewData.labelText.localized(),
            style = MaterialTheme.typography.titleLarge,
        )
        SizedSpacer(8.dp)
        if (viewData.hintCurrentEx != null) {
            Box(
                modifier = Modifier.weight(1f)
                    .height(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { viewData.currentEx / viewData.maxEx.toFloat() }
                )
                LinearProgressIndicator(
                    progress = { viewData.hintCurrentEx!! / viewData.maxEx.toFloat() },
                    color = ProgressIndicatorDefaults.linearColor.copy(alpha = 0.3f),
                )
            }
        } else {
            LinearProgressIndicator(
                progress = { viewData.currentEx / viewData.maxEx.toFloat() },
                modifier = Modifier.weight(1f)
                    .height(8.dp)
            )
        }
        SizedSpacer(8.dp)
        Button(
            onClick = { onInput(viewData.exTextClickAction) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewData.currentExText.localized(),
                    style = MaterialTheme.typography.titleLarge,
                )
                SizedSpacer(4.dp)
                Text(
                    text = viewData.maxExText.localized(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun SummaryJacketItem(
    viewData: UITrialSessionContent.Summary.Item,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit,
) {
    Column(modifier = modifier) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(viewData.jacketUrl)
//                .fallback(MR.images.trial_default)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable(enabled = viewData.tapAction != null) { viewData.tapAction?.let { onAction(it) } },
                contentScale = ContentScale.Crop,
            )
            ErrorOverlayLarge(viewData.hasError)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = viewData.difficultyClassText.localized(),
                    color = colorResource(viewData.difficultyClassColor),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = viewData.difficultyNumberText.localized()
                )
            }
            viewData.summaryContent?.let { content ->
                SummaryContent(content)
            }
        }
    }
}

@Composable
fun SummaryContent(
    viewData: UITrialSessionContent.Summary.SummaryContent,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        viewData.topText?.let { topText ->
            Text(
                text = topText.localized(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = viewData.bottomMainText.localized(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = viewData.bottomSubText.localized(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun RowScope.InProgressJacketItem(
    viewData: UITrialSessionContent.SongFocused.Item,
    modifier: Modifier = Modifier.weight(1f),
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(viewData.jacketUrl)
//                    .fallback(MR.images.trial_default)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )

            ErrorOverlaySmall(viewData.hasError)
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            viewData.topText?.let { topText ->
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = " ") // dummy text to retain bounding box
                    AutoResizedText(
                        text = topText.localized(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            viewData.bottomBoldText?.let { bottomText ->
                Text(
                    text = bottomText.localized(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun BoxScope.ErrorOverlaySmall(hasError: Boolean) = ErrorOverlay(
    hasError = hasError,
    iconSize = 48.dp,
    iconPadding = 4.dp
)

@Composable
fun BoxScope.ErrorOverlayLarge(hasError: Boolean) = ErrorOverlay(
    hasError = hasError,
    iconSize = 96.dp,
    iconPadding = 16.dp,
    shape = MaterialTheme.shapes.large
)

@Composable
fun BoxScope.ErrorOverlay(
    hasError: Boolean,
    iconSize: Dp,
    iconPadding: Dp,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Column(
        modifier = Modifier.matchParentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = hasError) {
            Image(
                painter = painterResource(MR.images.warning),
                contentDescription = "needs update",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .size(iconSize)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        shape = shape,
                    )
                    .padding(iconPadding)
            )
        }
    }
}

// This can still be useful, but currently we don't use this here
//private fun Modifier.backgroundGradientMask(): Modifier = this
//    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
//    .drawWithContent {
//        drawContent()
//        drawRect(
//            brush = Brush.verticalGradient(
//                colors = listOf(Color.White, Color.Transparent, Color.White),
//                startY = 0f,
//                endY = size.height / 2,
//                tileMode = TileMode.Mirror
//            ),
//            blendMode = BlendMode.DstIn // Multiplies alpha from the gradient with the underlying image
//        )
//    }
