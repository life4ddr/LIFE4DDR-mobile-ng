package com.perrigogames.life4ddr.nextgen.feature.banners

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.feature.banners.view.UIBanner
import com.perrigogames.life4ddr.nextgen.feature.banners.view.UIBannerTemplates
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized

@Composable
fun BannerContainer(
    data: UIBanner? = null,
    modifier: Modifier = Modifier,
) {
    var lastBanner: UIBanner by remember { mutableStateOf(UIBannerTemplates.dummy) }
    if (data != null) {
        lastBanner = data
    }

    val text = lastBanner.text.localized()
    lastBanner.backgroundColor
    val backgroundColor = lastBanner.backgroundColor?.let { colorResource(it) }
        ?: MaterialTheme.colorScheme.secondaryContainer
    val textColor = lastBanner.textColor?.let { colorResource(it) }
        ?: MaterialTheme.colorScheme.onSecondaryContainer

    AnimatedVisibility(
        visible = data != null,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Text(
            text = text,
            color = textColor,
            modifier = Modifier
                .background(backgroundColor)
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp,
                    horizontal = 16.dp
                )
                .then(modifier)
        )
    }
}