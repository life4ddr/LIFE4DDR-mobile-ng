package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsItem
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import dev.icerock.moko.resources.compose.localized


@Composable
fun SettingsScreenContent(
    items: List<UISettingsItem>,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(items) {
            when (it) {
                is UISettingsItem.Header -> SettingsHeaderItem(it)
                is UISettingsItem.Link -> SettingsLinkItem(it, onAction)
                is UISettingsItem.Checkbox -> SettingsCheckboxItem(it, onAction)
                UISettingsItem.Divider -> HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Paddings.LARGE)
                )
            }
        }
    }
}

@Composable
private fun SettingsHeaderItem(
    item: UISettingsItem.Header
) {
    Text(
        text = item.title.localized(),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = Paddings.LARGE)
            .padding(top = Paddings.LARGE)
    )
}

@Composable
private fun SettingsLinkItem(
    item: UISettingsItem.Link,
    onAction: (SettingsAction) -> Unit = {}
) {
    SettingsMenuLink(
        title = { Text(text = item.title.localized()) },
        subtitle = { item.subtitle?.let { Text(text = it.localized()) } },
        enabled = item.enabled
    ) { onAction(item.action) }
}

@Composable
private fun SettingsCheckboxItem(
    item: UISettingsItem.Checkbox,
    onAction: (SettingsAction) -> Unit = {}
) {
    SettingsCheckbox(
        title = { Text(text = item.title.localized()) },
        subtitle = { item.subtitle?.let { Text(text = it.localized()) } },
        enabled = item.enabled,
        state = item.toggled
    ) { onAction(item.action) }
}
