package com.perrigogames.life4ddr.nextgen.feature.scorelist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.banners.BannerContainer
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIScore
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIScoreList
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListEvent
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListInput
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ScoreListViewModel
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ScoreListScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    showSanbaiLogin: (String) -> Unit = {},
) {
    val viewModel = koinViewModel<ScoreListViewModel>()
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false,
        )
    )

    BackHandler {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.hide()
            }
        } else {
            onBackPressed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            when(it) {
                is ScoreListEvent.ShowSanbaiLogin -> showSanbaiLogin(it.url)
            }
        }
    }

    ScoreListContent(
        scope = scope,
        state = state,
        scaffoldState = scaffoldState,
        modifier = modifier,
        onInput = { viewModel.handleInput(it) },
    )
}

@Composable
fun ScoreListEmptyContent(
    state: UIScoreList.Empty,
    modifier: Modifier = Modifier,
    onInput: (ScoreListInput) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = state.title.localized(),
            style = MaterialTheme.typography.titleLarge,
        )
        SizedSpacer(32.dp)
        Text(
            text = state.subtitle.localized(),
            style = MaterialTheme.typography.bodyMedium,
        )
        SizedSpacer(64.dp)
        Button(
            onClick = { onInput(state.ctaInput) },
        ) {
            Text(text = state.ctaText.localized())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ScoreListContent(
    scope: CoroutineScope,
    state: UIScoreList,
    scaffoldState: BottomSheetScaffoldState,
    modifier: Modifier = Modifier,
    onInput: (ScoreListInput) -> Unit = {},
    showSanbaiLogin: (String) -> Unit = {},
) {
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            FilterPanel(
                data = state.filter,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp),
                onAction = { onInput(ScoreListInput.FilterInput(it)) }
            )
        },
        modifier = modifier,
    ) { outerPadding ->
        Scaffold(
            modifier = Modifier.padding(outerPadding),
            floatingActionButton = {
                SongListFloatingActionButtons(
                    onFilterPressed = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    onSyncScoresPressed = { onInput(ScoreListInput.RefreshSanbaiScores) }
                )
            },
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    BannerContainer(state.banner)
                }
            }
        ) { paddingValues ->
            val innerModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            when (state) {
                is UIScoreList.Empty -> {
                    ScoreListEmptyContent(
                        state = state,
                        modifier = innerModifier,
                        onInput = onInput
                    )
                }
                is UIScoreList.Loaded -> {
                    LazyColumn(modifier = innerModifier) {
                        items(state.scores) {
                            ScoreEntry(it, state.useMonospaceFontForScore)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongListFloatingActionButtons(
    onFilterPressed: () -> Unit,
    onSyncScoresPressed: () -> Unit,
) {
    var isFabExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(visible = isFabExpanded) {
            Column {
                SmallFloatingActionButton(
                    onClick = {
                        onFilterPressed()
                        isFabExpanded = false
                    },
                ) {
                    Icon(
                        painterResource(MR.images.filter_list),
                        contentDescription = "Change Filters"
                    )
                }
                SizedSpacer(8.dp)
                SmallFloatingActionButton(
                    onClick = {
                        onSyncScoresPressed()
                        isFabExpanded = false
                    },
                ) {
                    Icon(
                        painterResource(MR.images.sync),
                        contentDescription = "Sync Sanbai Scores"
                    )
                }
                SizedSpacer(8.dp)
            }
        }

        FloatingActionButton(
            onClick = { isFabExpanded = !isFabExpanded },
        ) {
            Icon(
                painterResource(if (isFabExpanded) MR.images.close else MR.images.more_vert),
                contentDescription = if (isFabExpanded) "Close" else "Expand"
            )
        }
    }
}

@Composable
fun ScoreEntry(
    data: UIScore,
    useMonospaceFontForScore: Boolean
) {
    Row(
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = data.titleText,
                maxLines = 1,
            )
            Row {
                Text(
                    text = data.difficultyText.localized(),
                    color = colorResource(data.difficultyColor),
                    modifier = Modifier.weight(1f),
                )
                SizedSpacer(4.dp)
                Text(
                    text = data.scoreText.localized(),
                    color = colorResource(data.scoreColor),
                    fontFamily = if (useMonospaceFontForScore) { FontFamily.Monospace } else { null },
                )
            }
        }
        val flareResource = data.flareLevel?.let { flareImageResource(it) }
        if (flareResource != null) {
            Image(
                painter = painterResource(flareResource),
                contentDescription = "Flare level ${data.flareLevel}",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically)
            )
        } else {
            SizedSpacer(32.dp)
        }
    }
}

fun flareImageResource(level: Int) = when(level) {
    1 -> MR.images.flare_1
    2 -> MR.images.flare_2
    3 -> MR.images.flare_3
    4 -> MR.images.flare_4
    5 -> MR.images.flare_5
    6 -> MR.images.flare_6
    7 -> MR.images.flare_7
    8 -> MR.images.flare_8
    9 -> MR.images.flare_9
    10 -> MR.images.flare_ex
    else -> null
}
