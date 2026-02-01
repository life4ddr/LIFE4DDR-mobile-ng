package com.perrigogames.life4ddr.nextgen.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.perrigogames.life4ddr.nextgen.AppInfo
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.PlatformType
import dev.icerock.moko.resources.compose.painterResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

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

@Composable
fun SystemBackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val appInfo = koinInject<AppInfo>()
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(
                if (appInfo.platform == PlatformType.IOS) {
                    MR.images.arrow_back_ios_new
                } else {
                    MR.images.arrow_back
                }
            ),
            contentDescription = "Back"
        )
    }
}
