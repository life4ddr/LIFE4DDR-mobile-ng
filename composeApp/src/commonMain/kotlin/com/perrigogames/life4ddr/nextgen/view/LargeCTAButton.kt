package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LargeCTAButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors()
) = Button(
    elevation = ButtonDefaults.elevatedButtonElevation(),
    colors = colors,
    modifier = modifier.fillMaxWidth(),
    onClick = onClick
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
