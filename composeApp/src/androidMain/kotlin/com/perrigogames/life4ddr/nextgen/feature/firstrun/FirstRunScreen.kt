@file:OptIn(ExperimentalAnimationApi::class)

package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.R
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.compose.primaryButtonColors
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunInfoViewModel
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunPath
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.Landing
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.*
import dev.icerock.moko.mvvm.createViewModelFactory
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun FirstRunScreen(
    modifier: Modifier = Modifier,
    viewModel: FirstRunInfoViewModel = viewModel(
        factory = createViewModelFactory { FirstRunInfoViewModel() }
    ),
    onComplete: (InitState) -> Unit,
    onClose: () -> Unit,
) {
    val step: FirstRunStep by viewModel.state.collectAsState(Landing)
    var completeHandled by remember { mutableStateOf(false) }

    BackHandler {
        if (!viewModel.navigateBack()) {
            onClose()
        }
    }

    FirstRunContent(
        step = step,
        modifier = modifier,
        onInput = { viewModel.handleInput(it) },
    )
}
