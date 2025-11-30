@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.enums.LadderRank
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RankImage(
    rank: LadderRank?,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    onClick: (() -> Unit)? = null,
) {
    val painter = painterResource(
        rank?.drawableRes ?: MR.images.copper_1
    )
    val colorMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val colorFilter = if (rank != null) null else ColorFilter.colorMatrix(colorMatrix)
    Image(
        painter = painter,
        colorFilter = colorFilter,
        contentDescription = null, //FIXME
        modifier = modifier
            .size(size)
            .alpha(if (rank != null) 1F else 0.3F)
            .let {
                if (onClick != null) {
                    it.clickable { onClick() }
                } else {
                    it
                }
            }
    )
}

@Preview
@Composable
fun RankImagePreviewNone() {
    MaterialTheme {
        RankImage(rank = null)
    }
}

@Preview
@Composable
fun RankImagePreviewCopper() {
    MaterialTheme {
        Column {
            LadderRank.entries
                .groupBy { it.group }
                .map { it.value }
                .forEach { group ->
                    Row {
                        group.forEach { rank ->
                            RankImage(size = 48.dp, rank = rank)
                        }
                    }
                }
        }
    }
}
