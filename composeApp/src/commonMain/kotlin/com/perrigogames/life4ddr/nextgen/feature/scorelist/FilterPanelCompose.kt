package com.perrigogames.life4ddr.nextgen.feature.scorelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.PlayStyle
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIFilterView
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.FilterPanelInput
import dev.icerock.moko.resources.compose.localized
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    data: UIFilterView,
    modifier: Modifier = Modifier,
    onAction: (FilterPanelInput) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        data.playStyleSelector?.let { selector ->
            SingleChoiceSegmentedButtonRow {
                selector.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = item.selected,
                        onClick = { onAction(item.action) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PlayStyle.entries.size),
                    ) {
                        Text(text = item.text.localized())
                    }
                }
            }
        }
        data.difficultyClassSelector?.let {
            Row {
                it.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Switch(
                            checked = item.selected,
                            onCheckedChange = { onAction(item.action) }
                        )
                        Text(text = item.text.localized())
                    }
                }
            }
            SizedSpacer(8.dp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = data.difficultyNumberTitle.localized(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Checkbox(
                checked = data.difficultyNumberUsesRange,
                onCheckedChange = { onAction(data.difficultyNumberUsesRangeInput) },
            )
            Text(
                text = data.difficultyNumberUsesRangeText.localized(),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (data.difficultyNumberUsesRange) {
            RangeSlider(
                value = data.difficultyNumberRange.innerFloatRange,
                valueRange = data.difficultyNumberRange.outerFloatRange,
                steps = data.difficultyNumberRange.outerRange.count() - 2,
                onValueChange = { range ->
                    onAction(
                        FilterPanelInput.SetDifficultyNumberRange(
                            min = range.start.roundToInt(),
                            max = range.endInclusive.roundToInt()
                        )
                    )
                }
            )
        } else {
            Slider(
                value = data.difficultyNumberRange.innerFloatRange.start,
                valueRange = data.difficultyNumberRange.outerFloatRange,
                steps = data.difficultyNumberRange.outerRange.count() - 2,
                onValueChange = { value -> onAction(FilterPanelInput.SetDifficultyNumber(value.roundToInt())) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.difficultyNumberRange.innerRange.first().toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.1f)
            )
            Spacer(Modifier.weight(0.8f))
            Text(
                text = data.difficultyNumberRange.innerRange.last().toString(),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.1f)
            )
        }

        SizedSpacer(8.dp)
        Text(
            text = data.clearTypeTitle.localized(),
            modifier = Modifier.align(Alignment.Start),
            style = MaterialTheme.typography.labelLarge,
        )
        SizedSpacer(8.dp)
        RangeSlider(
            value = data.clearTypeRange.innerFloatRange,
            valueRange = data.clearTypeRange.outerFloatRange,
            steps = data.clearTypeRange.outerRange.count() - 2,
            onValueChange = { range ->
                onAction(
                    FilterPanelInput.SetClearTypeRange(range.start.roundToInt(), range.endInclusive.roundToInt())
                )
            }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = ClearType.entries[data.clearTypeRange.innerRange.first()].uiName.localized(),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.3f)
            )
            Spacer(Modifier.weight(0.2f))
            Text(
                text = ClearType.entries[data.clearTypeRange.innerRange.last()].uiName.localized(),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(0.3f)
            )
        }

        SizedSpacer(8.dp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var rangeBottom by remember { mutableStateOf(data.scoreRangeBottomString) }
            var rangeTop by remember { mutableStateOf(data.scoreRangeTopString) }
            LaunchedEffect(data.scoreRangeBottomString) {
                if (data.scoreRangeBottomString != rangeBottom) {
                    rangeBottom = data.scoreRangeBottomString
                }
            }
            LaunchedEffect(data.scoreRangeTopString) {
                if (data.scoreRangeTopString != rangeTop) {
                    rangeTop = data.scoreRangeTopString
                }
            }

            TextField(
                value = rangeBottom.orEmpty(),
                onValueChange = {
                    rangeBottom = it
                    val bottomInt = it.trim().toIntOrNull()?.coerceIn(data.scoreRangeAllowed) ?: 0
                    onAction(FilterPanelInput.SetScoreRangeMin(bottomInt))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                placeholder = { Text(text = data.scoreRangeBottomHint.localized()) },
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = rangeTop.orEmpty(),
                onValueChange = {
                    rangeTop = it
                    val topInt = it.trim().toIntOrNull()?.coerceIn(data.scoreRangeAllowed) ?: GameConstants.MAX_SCORE
                    onAction(FilterPanelInput.SetScoreRangeMax(topInt))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
                placeholder = { Text(text = data.scoreRangeTopHint.localized()) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
