package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun ErrorText(
    modifier: Modifier = Modifier.fillMaxWidth(),
    text: @Composable () -> String?,
) = Text(
    text = text() ?: "",
    modifier = modifier,
    color = MaterialTheme.colorScheme.error,
)

@Composable
fun SizedSpacer(size: Dp) = Spacer(modifier = Modifier.size(size))