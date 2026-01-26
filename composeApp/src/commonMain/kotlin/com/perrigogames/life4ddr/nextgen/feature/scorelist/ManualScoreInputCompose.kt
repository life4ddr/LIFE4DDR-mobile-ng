package com.perrigogames.life4ddr.nextgen.feature.scorelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.data.GameConstants
import com.perrigogames.life4ddr.nextgen.enums.ClearType
import com.perrigogames.life4ddr.nextgen.enums.flareImageResource
import com.perrigogames.life4ddr.nextgen.feature.songlist.data.Chart
import com.perrigogames.life4ddr.nextgen.feature.songresults.view.UIManualScoreInput
import com.perrigogames.life4ddr.nextgen.feature.songresults.viewmodel.ManualScoreInputViewModel
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ManualScoreInputDialog(
    chart: Chart,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<ManualScoreInputViewModel>(key = chart.key) { parametersOf(chart) }
    val state by viewModel.state.collectAsState()
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            ManualScoreInputContent(
                state = state,
                modifier = Modifier.padding(16.dp),
                onSubmit = { score, clearType, flare ->
                    viewModel.submitResult(score, clearType, flare)
                    onDismiss()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualScoreInputContent(
    state: UIManualScoreInput,
    modifier: Modifier = Modifier,
    onSubmit: (Long, ClearType, Int) -> Unit,
) {
    var scoreInput by remember { mutableStateOf(0L) }
    var flareSelection by remember { mutableStateOf(0) }
    var flareExpanded by remember { mutableStateOf(false) }
    var clearTypeSelection by remember { mutableStateOf(ClearType.CLEAR) }
    var clearTypeExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = state.songTitle.localized(),
            style = MaterialTheme.typography.titleLarge,
        )
        SizedSpacer(4.dp)
        Row {
            Text(
                text = stringResource(state.songDifficultyClass.nameRes),
                style = MaterialTheme.typography.labelLarge,
                color = colorResource(state.songDifficultyClass.colorRes)
            )
            SizedSpacer(4.dp)
            Text(
                text = state.songDifficultyNumber.localized(),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        SizedSpacer(16.dp)

        TextField(
            value = scoreInput.toString(),
            onValueChange = {
                scoreInput = it.toLongOrNull()?.coerceIn(0L..GameConstants.MAX_SCORE) ?: 0
            },
            enabled = clearTypeSelection != ClearType.MARVELOUS_FULL_COMBO,
            label = { Text(state.scoreLabel.localized()) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        SizedSpacer(16.dp)
        ExposedDropdownMenuBox(
            expanded = clearTypeExpanded,
            onExpandedChange = { clearTypeExpanded = it },
        ) {
            TextField(
                value = stringResource(clearTypeSelection.clearRes),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                label = { Text(state.clearTypeLabel.localized()) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(MR.images.arrow_drop_down),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            ExposedDropdownMenu(
                expanded = clearTypeExpanded,
                onDismissRequest = { clearTypeExpanded = false },
            ) {
                state.clearTypeOptions.forEach { value ->
                    DropdownMenuItem(
                        text = { Text(stringResource(value.clearRes)) },
                        onClick = {
                            clearTypeSelection = value
                            clearTypeExpanded = false
                            if (clearTypeSelection == ClearType.MARVELOUS_FULL_COMBO) {
                                scoreInput = GameConstants.MAX_SCORE.toLong()
                            }
                        }
                    )
                }
            }
        }

        SizedSpacer(16.dp)
        ExposedDropdownMenuBox(
            expanded = flareExpanded,
            onExpandedChange = { flareExpanded = it },
        ) {
            TextField(
                value = state.flareOptions[flareSelection].first.localized(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                label = { Text(state.flareLabel.localized()) },
                leadingIcon = {
                    flareImageResource(flareSelection)?.let {
                        Image(
                            painter = painterResource(it),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(MR.images.arrow_drop_down),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            ExposedDropdownMenu(
                expanded = flareExpanded,
                onDismissRequest = { flareExpanded = false },
            ) {
                state.flareOptions.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text(label.localized()) },
                        onClick = {
                            flareSelection = value
                            flareExpanded = false
                        }
                    )
                }
            }
        }

        SizedSpacer(16.dp)

        TextButton(
            onClick = { onSubmit(scoreInput, clearTypeSelection, flareSelection) },
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(state.submitLabel.localized())
        }
    }
}
