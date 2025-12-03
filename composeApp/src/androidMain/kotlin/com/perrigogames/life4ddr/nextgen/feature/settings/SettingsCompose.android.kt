package com.perrigogames.life4ddr.nextgen.feature.settings

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.icerock.moko.mvvm.createViewModelFactory
import androidx.core.net.toUri
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISettingsData
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsAction
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsEvent
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SettingsViewModel
import com.perrigogames.life4ddr.nextgen.util.Destination

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onNavigate: (Destination) -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = createViewModelFactory { SettingsViewModel(onClose, onNavigate) }
    ),
) {
    val context = LocalContext.current

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
                is SettingsEvent.WebLink -> {
                    Log.v("Settings", "Opening web link ${event.url}")
                    val intent = Intent(Intent.ACTION_VIEW, event.url.toUri())
                    context.startActivity(intent)
                }
                is SettingsEvent.Email -> {
                    Log.v("Settings", "Opening email client to ${event.email}")
                    val emailUri = "mailto:${event.email}".toUri()
                    val intent = Intent(Intent.ACTION_SENDTO, emailUri)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    data: UISettingsData,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit = {}
) {
    val context = LocalContext.current

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
                title = { Text(text = data.screenTitle.toString(context)) },
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
