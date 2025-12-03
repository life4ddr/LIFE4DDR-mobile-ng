package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import com.perrigogames.life4ddr.nextgen.view.RankImage
import com.perrigogames.life4ddr.nextgen.feature.trials.enums.TrialRank
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITargetRank
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RankSelector(
    viewData: UITargetRank,
    rankSelected: (TrialRank) -> Unit = {},
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankDropdown(viewData, rankSelected)
        }

        SizedSpacer(8.dp)
        AnimatedContent(targetState = viewData.rankGoalItems) { items ->
            Column {
                items.forEach { goal ->
                    Text(
                        text = goal.localized()
                    )
                }
            }
        }
    }
}

@Composable
fun RankDropdown(
    viewData: UITargetRank,
    rankSelected: (TrialRank) -> Unit = {},
) {
    var dropdownExpanded: Boolean by remember { mutableStateOf(false) }

    Box {
        CardRankDisplay(
            viewData = viewData,
            showSelectorIcon = viewData is UITargetRank.Selection,
            modifier = Modifier.clickable(
                enabled = viewData is UITargetRank.Selection,
            ) { dropdownExpanded = true }
        )
        if (viewData is UITargetRank.Selection) {
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
            ) {
                viewData.availableRanks.forEach { rank ->
                    DropdownMenuItem(
                        text = { Text(stringResource(rank.nameRes)) },
                        leadingIcon = {
                            RankImage(
                                rank = rank.parent,
                                size = 32.dp
                            )
                        },
                        onClick = {
                            rankSelected(rank)
                            dropdownExpanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun CardRankDisplay(
    viewData: UITargetRank,
    showSelectorIcon: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(),
        modifier = modifier,
    ) {
        RankDisplay(
            viewData = viewData,
            showSelectorIcon = showSelectorIcon,
            rankImageSize = 32.dp,
            textStyle = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun RankDisplay(
    viewData: UITargetRank,
    showSelectorIcon: Boolean,
    rankImageSize: Dp,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedContent(
            targetState = viewData,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }
        ) { viewData ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RankImage(
                    rank = viewData.rank.parent,
                    size = rankImageSize,
                )
                Text(
                    text = viewData.title.localized(),
                    color = colorResource(viewData.titleColor),
                    style = textStyle,
                )
            }
        }

        AnimatedVisibility(visible = showSelectorIcon) {
            Icon(
                painter = painterResource(MR.images.arrow_drop_down),
                contentDescription = "Select rank"
            )
        }
        SizedSpacer(4.dp)
    }
}
