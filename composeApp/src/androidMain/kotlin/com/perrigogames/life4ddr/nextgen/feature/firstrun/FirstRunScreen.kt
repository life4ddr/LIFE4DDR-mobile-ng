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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(0.75f)
                .fillMaxHeight()
        ) {
            FirstRunHeader(
                showWelcome = step == Landing,
                modifier = Modifier.fillMaxWidth()
            )

            val contentModifier = Modifier.fillMaxWidth()

            AnimatedContent(
                targetState = step,
                label = "First run content transitions",
            ) { step ->
                when (step) {
                    Landing -> {
                        FirstRunNewUser(modifier = contentModifier) {
                            viewModel.newUserSelected(it)
                        }
                    }
                    is Username -> {
                        FirstRunUsername(
                            viewModel = viewModel,
                            step = step,
                            modifier = contentModifier,
                        )
                    }
                    is UsernamePassword -> {
                        FirstRunUsernamePassword(
                            viewModel = viewModel,
                            modifier = contentModifier,
                        )
                    }
                    is RivalCode -> {
                        FirstRunRivalCode(
                            viewModel = viewModel,
                            modifier = contentModifier,
                        )
                    }
                    is SocialHandles -> {
                        FirstRunSocials(
                            viewModel = viewModel,
                            modifier = contentModifier,
                        )
                    }
                    is InitialRankSelection -> {
                        FirstRunRankMethod(
                            step = step,
                            modifier = contentModifier,
                            onRankMethodSelected = viewModel::rankMethodSelected
                        )
                    }
                    is Completed -> {
                        if (!completeHandled) {
                            onComplete(step.rankSelection)
                            completeHandled = true
                        }
                    }
                    else -> error("Unsupported step $step")
                }
            }
        }

        if (step.showNextButton) {
            Button(
                onClick = { viewModel.navigateNext() },
                content = { Text("Next") },
                colors = primaryButtonColors(),
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun FirstRunHeader(
    showWelcome: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = showWelcome,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(bottom = 8.dp),
        ) {
            Text(
                text = stringResource(MR.strings.first_run_landing_header),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Image(
            painter = painterResource(R.drawable.life4_logo_invert),
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }
}

@Composable
fun FirstRunNewUser(
    modifier: Modifier = Modifier,
    onNewUserSelected: (Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(MR.strings.first_run_landing_description),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
        ) {
            Button(
                onClick = { onNewUserSelected(true) },
                colors = primaryButtonColors(),
                content = { Text(
                    text = stringResource(MR.strings.yes),
                ) },
                modifier = Modifier.weight(1f, false)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { onNewUserSelected(false) },
                colors = primaryButtonColors(),
                content = { Text(
                    text = stringResource(MR.strings.no),
                ) },
                modifier = Modifier.weight(1f, false)
            )
        }
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunHeaderPreview() {
    LIFE4Theme {
        FirstRunHeader(true)
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunNewUserPreview() {
    LIFE4Theme {
        FirstRunNewUser {}
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunUsernameNewPreview() {
    LIFE4Theme {
        FirstRunUsername(
            step = Username(FirstRunPath.NEW_USER_LOCAL),
            viewModel = FirstRunInfoViewModel()
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunUsernameExistingPreview() {
    LIFE4Theme {
        FirstRunUsername(
            step = Username(FirstRunPath.EXISTING_USER_LOCAL),
            viewModel = FirstRunInfoViewModel()
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunRivalCodePreview() {
    LIFE4Theme {
        FirstRunRivalCode(
            viewModel = FirstRunInfoViewModel()
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunRivalCodeEntryPreview() {
    LIFE4Theme {
        RivalCodeEntry("12345678")
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunSocialsPreview() {
    LIFE4Theme {
        FirstRunSocials(
            viewModel = FirstRunInfoViewModel()
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunRankMethodPreview() {
    LIFE4Theme {
        FirstRunRankMethod(
            step = InitialRankSelection(FirstRunPath.NEW_USER_LOCAL)
        )
    }
}
