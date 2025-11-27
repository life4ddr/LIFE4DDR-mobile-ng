package com.perrigogames.life4ddr.nextgen.feature.ladder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LadderRankClassTheme
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.enums.LadderRankClass
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderDetailItem
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoal
import com.perrigogames.life4ddr.nextgen.feature.ladder.view.UILadderGoals
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.GoalListInput
import com.perrigogames.life4ddr.nextgen.view.Life4Divider
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun LadderGoalsContent(
    goals: UILadderGoals,
    rankClass: LadderRankClass? = null,
    onInput: (GoalListInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (rankClass != null) {
        LadderRankClassTheme(rankClass) {
            LadderGoalsContent(goals, onInput, modifier)
        }
    } else {
        LadderGoalsContent(goals, onInput, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderGoalsContent(
    goals: UILadderGoals,
    onInput: (GoalListInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var debugDialog by remember { mutableStateOf<String?>(null) }

    when (goals) {
        is UILadderGoals.SingleList -> {
            SingleGoalList(
                goals = goals.items,
                onInput = onInput,
                onShowDebug = { debugDialog = it },
                modifier = modifier,
            )
        }
        is UILadderGoals.CategorizedList -> {
            CategorizedList(
                goals = goals,
                onInput = onInput,
                onShowDebug = { debugDialog = it },
                modifier = modifier,
            )
        }
    }

    if (debugDialog != null) {
        BasicAlertDialog(
            onDismissRequest = { debugDialog = null },
            content = {
                Card {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp)
                    ) {
                        Text(text = debugDialog!!)
                        TextButton(
                            onClick = { debugDialog = null }
                        ) { Text("Close") }
                    }
                }
            }
        )
    }
}

@Composable
fun SingleGoalList(
    goals: List<UILadderGoal>,
    onInput: (GoalListInput) -> Unit,
    onShowDebug: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
    ) {
        itemsIndexed(goals) { idx, goal ->
            if (idx > 0) {
                SizedSpacer(size = 4.dp)
            }
            LadderGoalItem(
                goal = goal,
                onInput = onInput,
                onShowDebug = onShowDebug,
                modifier = Modifier.fillParentMaxWidth(),
            )
        }
    }
}

@Composable
fun CategorizedList(
    goals: UILadderGoals.CategorizedList,
    onInput: (GoalListInput) -> Unit,
    onShowDebug: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val aggregateItems = goals.categories
        .flatMap { (info, goals) -> listOf(info) + goals }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(all = 8.dp),
    ) {
        itemsIndexed(aggregateItems) { idx, item ->
            if (idx > 0) {
                SizedSpacer(size = 4.dp)
            }
            when(item) {
                is UILadderGoals.CategorizedList.Category -> {
                    SizedSpacer(8.dp)
                    Row(
                        modifier = Modifier.fillParentMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        item.title?.localized()?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        item.goalText?.localized()?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    SizedSpacer(8.dp)
                }
                is UILadderGoal -> {
                    LadderGoalItem(
                        goal = item,
                        expanded = item.detailItems.isNotEmpty(),
                        onInput = onInput,
                        onShowDebug = onShowDebug,
                        modifier = Modifier.fillParentMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun LadderGoalItem(
    goal: UILadderGoal,
    expanded: Boolean = false,
    onInput: (GoalListInput) -> Unit,
    onShowDebug: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (goal.hidden) 0.5f else 1f),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (goal.hidden) 0.5f else 1f)
        ) {
            LadderGoalHeaderRow(
                goal = goal,
                onInput = onInput,
                onShowDebug = onShowDebug,
            )
            if (goal.detailItems.isNotEmpty()) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top),
                ) {
                    Column {
                        Life4Divider()
                        SizedSpacer(Paddings.LadderGoals.VERTICAL_PADDING)
                        LadderGoalDetailShade(
                            items = goal.detailItems,
                            modifier = Modifier
                                .padding(horizontal = Paddings.LadderGoals.HORIZONTAL_PADDING)
                                .padding(bottom = Paddings.LadderGoals.VERTICAL_PADDING)
                        )
                    }
                }
            }
            if (goal.progress?.showProgressBar == true) {
                LinearProgressIndicator(
                    color = colorResource(MR.colors.colorAccent),
                    trackColor = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
                    progress = { goal.progress!!.progressPercent },
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LadderGoalHeaderRow(
    goal: UILadderGoal,
    onInput: (GoalListInput) -> Unit,
    onShowDebug: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .let { mod ->
                if (goal.expandAction != null || goal.debugText != null) {
                    mod.combinedClickable(
                        onClick = { goal.expandAction?.let { onInput(it) } },
                        onLongClick = { goal.debugText?.let { onShowDebug(it) } }
                    )
                } else {
                    mod
                }
            },
    ) {
        Text(
            text = goal.goalText.localized(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Paddings.LadderGoals.VERTICAL_PADDING)
                .padding(start = Paddings.LadderGoals.HORIZONTAL_PADDING)
        )
        goal.progress?.let { progress ->
            Text(
                text = progress.progressText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (goal.showCheckbox) { // FIXME these should be fixed so they have a state when we show these too
            Checkbox(
                checked = goal.completed,
                enabled = goal.completeAction != null,
                onCheckedChange = { goal.completeAction?.let(onInput) },
            )
        }
        goal.hideAction?.let { action -> // FIXME these should be fixed so they have a state when we show these too
            Icon(
                painter = painterResource(
                    if (goal.hidden) {
                        MR.images.visibility_off
                    } else {
                        MR.images.visibility
                    }
                ),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = if (goal.hidden) "Hidden" else "Visible",
                modifier = Modifier
                    .clickable { onInput(action) }
                    .safeContentPadding()
                    .padding(end = Paddings.LadderGoals.HORIZONTAL_PADDING)
            )
        }

        if (!goal.showCheckbox && goal.hideAction == null) {
            SizedSpacer(16.dp)
        }
    }
}

@Composable
private fun LadderGoalDetailShade(
    items: List<UILadderDetailItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            when (item) {
                is UILadderDetailItem.Entry -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = item.leftText,
                            color = item.leftColor?.let { colorResource(it) } ?: MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(item.leftWeight)
                        )
                        if (item.rightText != null) {
                            SizedSpacer(8.dp)
                            Text(
                                text = item.rightText!!,
                                color = item.rightColor?.let { colorResource(it) } ?: MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(item.rightWeight)
                            )
                        }
                    }
                }
                UILadderDetailItem.Spacer -> HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}