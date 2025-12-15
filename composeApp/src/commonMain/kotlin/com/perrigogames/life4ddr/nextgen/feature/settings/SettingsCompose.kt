package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import co.touchlab.kermit.Logger
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsItem
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsEvent
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsViewModel
import com.perrigogames.life4ddr.nextgen.util.Destination
import dev.icerock.moko.resources.compose.localized
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onNavigate: (Destination) -> Unit,
    logger: Logger,
) {
    val viewModel = koinViewModel<SettingsViewModel>()

    val state = viewModel.state.collectAsState()
    state.value?.let { data ->
        SettingsScreen(
            data = data,
            modifier = modifier,
            onAction = { viewModel.handleAction(it) }
        )
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when(event) {
                SettingsEvent.Close -> {
                    onClose()
                }
                is SettingsEvent.Navigate -> {
                    onNavigate(event.destination)
                }
                is SettingsEvent.NavigateToWebLink -> {
                    logger.v { "Opening web link ${event.url}" }
                    TODO()
//                    val intent = Intent(Intent.ACTION_VIEW, event.url.toUri())
//                    context.startActivity(intent)
                }
                is SettingsEvent.NavigateToEmail -> {
                    logger.v { "Opening email client to ${event.email}" }
                    TODO()
//                    val emailUri = "mailto:${event.email}".toUri()
//                    val intent = Intent(Intent.ACTION_SENDTO, emailUri)
//                    context.startActivity(intent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    data: UISettingsData,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit = {}
) {
    BackHandler {
        onAction(SettingsAction.NavigateBack)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(text = data.screenTitle.localized()) },
            )
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            SettingsScreenContent(
                items = data.settingsItems,
                modifier = Modifier.fillMaxSize(),
                onAction = onAction,
            )
        }
    }
}


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
