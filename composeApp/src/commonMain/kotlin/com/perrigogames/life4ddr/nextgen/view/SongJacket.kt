package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.jackets.db.JacketsDatabaseHelper
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SongJacket(
    data: UISongJacket,
    modifier: Modifier = Modifier,
) {
    when(data) {
        is UISongJacket.WithUrl -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(data.url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .aspectRatio(1f)
            )
        }
        is UISongJacket.Placeholder -> {
            val jacketsDatabaseHelper = koinInject<JacketsDatabaseHelper>()
            val scope = rememberCoroutineScope()
            var showDialog by remember { mutableStateOf(false) }

            if (showDialog) {
                UrlEntryDialog(
                    onUrlConfirmed = { url ->
                        scope.launch {
                            jacketsDatabaseHelper.putUrl(id = data.chart.song.skillId, url = url)
                        }
                    },
                    onDismiss = { showDialog = false }
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .aspectRatio(1f)
                    .background(colorResource(data.chart.difficultyClass.colorRes))
                    .clickable { showDialog = true }
            ) {
                @Composable
                fun warningIcon(size: Dp) {
                    Icon(
                        painter = painterResource(MR.images.warning),
                        contentDescription = "No jacket found",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(size)
                    )
                }

                when (data.type) {
                    UISongJacket.PlaceholderType.FULL -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = data.warningText.localized(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            warningIcon(48.dp)
                            AutoResizedText(
                                text = data.chart.song.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(data.chart.difficultyClass.nameRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = data.chart.difficultyNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text(
                                text = data.chart.song.version.printName,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    UISongJacket.PlaceholderType.THUMBNAIL -> {
                        warningIcon(24.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlEntryDialog(
    onUrlConfirmed: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var urlInput by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter URL") },
        text = {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (urlInput.isNotEmpty()) {
                        onUrlConfirmed.invoke(urlInput)
                    }
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}