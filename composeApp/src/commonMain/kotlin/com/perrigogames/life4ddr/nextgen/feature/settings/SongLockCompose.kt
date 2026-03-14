package com.perrigogames.life4ddr.nextgen.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.feature.ladder.viewmodel.RankListViewModelInput
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISongLockPage
import com.perrigogames.life4ddr.nextgen.feature.settings.view.UISongLockSection
import com.perrigogames.life4ddr.nextgen.feature.settings.viewmodel.SongLockPageProvider
import com.perrigogames.life4ddr.nextgen.view.SizedSpacer
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongLockScreen(
    onBack: () -> Unit,
) {
    val provider = koinInject<SongLockPageProvider>()
    val state by provider.data.collectAsState()

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.title.localized(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() }
                    ) {
                        Image(
                            painter = painterResource(MR.images.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        SongLockScreenContent(
            data = state,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun SongLockScreenContent(
    data: UISongLockPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        SizedSpacer(8.dp)
        LazyColumn {
            items(data.sections) { section ->
                UISongLockSection(section)
                SizedSpacer(8.dp)
            }
        }
    }
}

@Composable
private fun UISongLockSection(
    data: UISongLockSection
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = data.title.localized(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .clickable { expanded = !expanded }
                .fillMaxWidth()
                .padding(16.dp)
        )
        if (expanded) {
            Column(
                modifier = Modifier.clickable { expanded = !expanded }
                    .padding(start = 16.dp)
            ) {
                data.charts.forEach { chart ->
                    Text(
                        text = chart.localized()
                    )
                }
            }
        }
    }
}