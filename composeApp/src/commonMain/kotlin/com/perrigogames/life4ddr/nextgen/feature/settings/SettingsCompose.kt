package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.AnnotatedString
import co.touchlab.kermit.Logger
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.Paddings
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsItem
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsEvent
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsViewModel
import com.perrigogames.life4ddr.nextgen.util.Destination
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import me.zhanghai.compose.preference.CheckboxPreference
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.TextFieldPreference
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
        SettingsScreenContent(
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
fun SettingsScreenContent(
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
                navigationIcon = {
                    if (!data.isRoot) {
                        IconButton(
                            onClick = { onAction(SettingsAction.NavigateBack) }
                        ) { Icon(painter = painterResource(MR.images.arrow_back), "back") }
                    }
                }
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
    ProvidePreferenceLocals {
        LazyColumn(modifier = modifier) {
            items.forEach { item ->
                item(item.key) {
                    when (item) {
                        is UISettingsItem.Header -> {
                            PreferenceCategory(
                                title = { Text(item.title.localized()) }
                            )
                        }

                        is UISettingsItem.Link -> {
                            Preference(
                                title = { Text(item.title.localized()) },
                                summary = { item.subtitle?.let { Text(it.localized()) } },
                                enabled = item.enabled,
                                onClick = { onAction(item.action) }
                            )
                        }

                        is UISettingsItem.Checkbox -> {
                            CheckboxPreference(
                                title = { Text(item.title.localized()) },
                                summary = { item.subtitle?.let { Text(it.localized()) } },
                                enabled = item.enabled,
                                value = item.toggled,
                                onValueChange = { onAction(item.createAction(it)) }
                            )
                        }

                        is UISettingsItem.Text -> {
                            TextFieldPreference(
                                title = { Text(item.title.localized()) },
                                summary = { item.subtitle?.let { Text(it.localized()) } },
                                textToValue = item.transform,
                                value = item.initialValue,
                                onValueChange = { onAction(item.createAction(it)) }
                            )
                        }

                        is UISettingsItem.Dropdown -> {
                            ListPreference(
                                value = item.currentItem,
                                values = item.dropdownItems,
                                onValueChange = { onAction(item.createAction(it)) },
                                title = { Text(item.title.localized()) },
                                summary = { item.subtitle?.let { Text(it.localized()) } },
                                type = ListPreferenceType.DROPDOWN_MENU,
                                valueToText = { AnnotatedString(item.createText(it)) }
                            )
                        }

                        UISettingsItem.Divider -> {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = Paddings.LARGE)
                            )
                        }
                    }
                }
            }
        }
    }
}
