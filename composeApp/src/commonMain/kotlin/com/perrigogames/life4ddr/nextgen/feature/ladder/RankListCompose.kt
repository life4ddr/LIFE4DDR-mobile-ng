package com.perrigogames.life4ddr.nextgen.feature.ladder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UIFooterData
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UINoRank
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UIRankList
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModel
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelEvent
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelInput
import com.perrigogames.life4ddr.nextgen.view.AutoResizedText
import com.perrigogames.life4ddr.nextgen.view.RankImageWithTitle
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RankListScreen(
    isFirstRun: Boolean = false,
    onAction: (RankListViewModelEvent) -> Unit = {},
) {
    val viewModel = koinViewModel<RankListViewModel> { parametersOf(isFirstRun) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.actions.collect(onAction)
    }
    RankListContent(
        state = state,
        onInput = { viewModel.onInputAction(it) },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankListContent(
    state: UIRankList,
    onInput: (RankListViewModelInput) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.titleText.localized(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    if (state.showBackButton) {
                        IconButton(
                            onClick = { onInput(RankListViewModelInput.RankRejected) }
                        ) {
                            Image(
                                painter = painterResource(MR.images.arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            SizedSpacer(32.dp)
            RankSelection(
                data = state,
                onInput = onInput,
            )

            val ladderData = state.ladderData
            if (ladderData != null) {
                LadderGoalsContent(
                    goals = ladderData.goals,
                    rankClass = ladderData.targetRankClass,
                    modifier = Modifier.weight(1f),
                    onInput = { onInput(RankListViewModelInput.GoalList(it)) },
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            state.footer?.let { firstRun ->
                FirstRunWidget(
                    data = firstRun,
                    onInput = onInput,
                )
            }
        }
    }
}

@Composable
fun FirstRunWidget(
    data: UIFooterData,
    onInput: (RankListViewModelInput) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        data.footerText?.let { footer ->
            AutoResizedText(
                text = footer.localized(),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(horizontal = Paddings.HUGE)
                    .padding(top = Paddings.LARGE)
            )
        }
        Button(
            onClick = { onInput(data.buttonInput) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Paddings.HUGE,
                    vertical = Paddings.LARGE
                )
        ) {
            Text(text = data.buttonText.localized())
        }
    }
}

@Composable
fun RankSelection(
    data: UIRankList,
    modifier: Modifier = Modifier,
    onInput: (RankListViewModelInput) -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        RankCategorySelector(
            data = data,
            modifier = Modifier.padding(vertical = Paddings.MEDIUM),
            onInput = onInput
        )
        HorizontalDivider()

        AnimatedVisibility(visible = data.showRankSelector) {
            AnimatedContent(
                targetState = data.ranks to data.isRankSelectorCompressed,
                label = "anim_between_categories",
                transitionSpec = {
                    if (targetState.first == initialState.first) {
                        if (targetState.second) {
                            slideInVertically() + fadeIn() togetherWith
                                    slideOutVertically() + fadeOut()
                        } else {
                            slideInVertically() + fadeIn() togetherWith
                                    slideOutVertically() + fadeOut()
                        }
                    } else {
                        if (targetState.second) {
                            slideInVertically() + fadeIn() togetherWith
                                    slideOutVertically() + fadeOut()
                        } else {
                            slideInVertically() + fadeIn() togetherWith
                                    slideOutVertically() + fadeOut()
                        }
                    }
                }
            ) { (ranks, compress) ->
                RankDetailSelector(
                    availableRanks = ranks,
                    compress = compress,
                    modifier = Modifier.weight(1f),
                    noRank = data.noRankInfo,
                    onInput = onInput,
                )
            }
        }
    }
}

@Composable
fun RankDetailSelector(
    availableRanks: List<UILadderRank>,
    compress: Boolean,
    modifier: Modifier = Modifier,
    noRank: UINoRank = UINoRank.DEFAULT,
    onInput: (RankListViewModelInput) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SizedSpacer(size = Paddings.HUGE)
        if (availableRanks.size < 5) {
            NoRankDetails(
                noRank = noRank,
                onInput = onInput,
            )
        } else {
            RankItemSelector(
                availableRanks = availableRanks,
                compressed = compress,
                onInput = onInput,
            )
        }
    }
}

@Composable
fun NoRankDetails(
    modifier: Modifier = Modifier,
    noRank: UINoRank = UINoRank.DEFAULT,
    onInput: (RankListViewModelInput) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = noRank.bodyText.localized(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(horizontal = Paddings.HUGE)
        )
        Button(
            onClick = { onInput(noRank.buttonInput) },
            modifier = Modifier
                .padding(horizontal = Paddings.HUGE)
                .padding(vertical = Paddings.HUGE)
        ) {
            Text(
                text = noRank.buttonText.localized()
            )
        }
    }
}

@Composable
fun RankCategorySelector(
    data: UIRankList,
    modifier: Modifier = Modifier,
    onInput: (RankListViewModelInput) -> Unit = {},
) {
    LazyRow(
        modifier = modifier
    ) {
        items(data.rankClasses) { category ->
            RankImageWithTitle(
                rank = category.rankClass?.toLadderRank(),
                modifier = Modifier
                    .padding(horizontal = Paddings.MEDIUM)
                    .padding(top = Paddings.MEDIUM),
                iconSize = 64.dp,
                selected = category.selected,
                text = category.text.localized(),
                style = MaterialTheme.typography.titleSmall,
                onClick = { onInput(category.tapInput) }
            )
        }
    }
}

@Composable
fun RankItemSelector(
    availableRanks: List<UILadderRank>,
    compressed: Boolean,
    modifier: Modifier = Modifier,
    onInput: (RankListViewModelInput) -> Unit = {},
) {
    Column(modifier = modifier) {
        if (compressed) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableRanks.forEach { rank ->
                    RankImageWithTitle(
                        rank = rank.rank,
                        modifier = Modifier
                            .padding(horizontal = Paddings.MEDIUM)
                            .padding(top = Paddings.MEDIUM),
                        selected = rank.selected,
                        iconSize = 48.dp,
                        text = rank.text.localized()
                    ) { onInput(rank.tapInput) }
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableRanks.subList(0, 3).forEach { rank ->
                    RankImageWithTitle(rank.rank) { onInput(rank.tapInput) }
                }
            }
            SizedSpacer(size = Paddings.LARGE)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                availableRanks.subList(3, 5).forEach { rank ->
                    RankImageWithTitle(rank.rank) { onInput(rank.tapInput) }
                }
            }
        }
    }
}

@Composable
fun RankSelectionMini(
    modifier: Modifier = Modifier,
    ranks: List<LadderRank> = LadderRank.entries,
    selectedRank: LadderRank?,
    showNone: Boolean = true,
    onRankSelected: (LadderRank?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        item {
            SizedSpacer(size = 10.dp)
        }
        if (showNone) {
            item {
                RankImageWithTitle(
                    rank = null,
                    selected = selectedRank == null
                )
            }
        }
        items(ranks) { rank ->
            RankImageWithTitle(
                rank = rank,
                selected = rank == selectedRank,
                onClick = { onRankSelected(rank) }
            )
        }
        item {
            SizedSpacer(size = 10.dp)
        }
    }
}
