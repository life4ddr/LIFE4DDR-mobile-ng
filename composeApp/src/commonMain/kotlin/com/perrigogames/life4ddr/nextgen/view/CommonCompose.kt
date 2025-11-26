package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun Life4Divider() = HorizontalDivider(
    thickness = 1.dp,
    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
)