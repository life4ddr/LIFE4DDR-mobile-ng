package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.trialsession.view.UITrialBottomSheet
import com.perrigogames.life4ddr.nextgen.feature.trialsession.viewmodel.TrialSessionInput
import com.perrigogames.life4ddr.nextgen.view.InteractiveImage
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun SongEntryBottomSheetContentAndroid(
    viewData: UITrialBottomSheet.Details,
    onAction: (TrialSessionInput) -> Unit,
) {
    SongEntryBottomSheetContent(
        viewData = viewData,
        onAction = onAction,
    )
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SongEntryBottomSheetContent(
    viewData: UITrialBottomSheet.Details,
    onAction: (TrialSessionInput) -> Unit,
) {
    BackHandler {
        onAction(TrialSessionInput.HideBottomSheet)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        InteractiveImage(
            painter = rememberAsyncImagePainter(model = viewData.imagePath),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        SongEntryControls(
            fields = viewData.fields,
            shortcuts = viewData.shortcuts,
            isEdit = viewData.isEdit,
            submitAction = viewData.onDismissAction,
            onAction = onAction,
        )
    }
}

@Composable
fun SongEntryControls(
    fields: List<List<UITrialBottomSheet.Field>>,
    shortcuts: List<UITrialBottomSheet.Shortcut>,
    isEdit: Boolean,
    submitAction: TrialSessionInput,
    modifier: Modifier = Modifier,
    onAction: (TrialSessionInput) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val flatFields = remember(fields) { fields.flatten() }
    val focusRequesters = remember(flatFields.size) { List(flatFields.size) { FocusRequester() } }

    Row(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            fields.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { field ->
                        var value by remember { mutableStateOf(TextFieldValue(field.text)) }
                        val isLast = field == row.last() && row == fields.last()

                        LaunchedEffect(field.text) {
                            if (value.text != field.text) {
                                value = TextFieldValue(field.text)
                            }
                        }

                        TextField(
                            value = value,
                            onValueChange = { newText: TextFieldValue ->
                                value = newText
                                onAction(TrialSessionInput.ChangeText(field.id, newText.text))
                            },
                            enabled = field.enabled,
                            label = { Text(field.label.localized()) },
                            maxLines = 1,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = if (isLast) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    val nextIndex = flatFields.indexOf(field) + 1
                                    if (nextIndex < focusRequesters.size) {
                                        focusRequesters[nextIndex].requestFocus()
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                },
                                onDone = {
                                    onAction(submitAction)
                                }
                            ),
                            modifier = Modifier
                                .weight(field.weight)
                                .focusRequester(focusRequesters[flatFields.indexOf(field)])
                        )
                        // FIXME error state
                    }
                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (shortcuts.isNotEmpty()) {
            // FIXME implement shortcuts
        }

        IconButton(
            onClick = { onAction(submitAction) },
        ) {
            Icon(
                painter = painterResource(
                    if (isEdit) {
                        MR.images.check
                    } else {
                        MR.images.arrow_forward
                    }
                ),
                contentDescription = "Next"
            )
        }
    }
}

