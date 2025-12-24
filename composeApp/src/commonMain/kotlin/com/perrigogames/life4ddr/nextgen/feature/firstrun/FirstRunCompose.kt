package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.LIFE4Theme
import com.perrigogames.life4ddr.nextgen.compose.primaryButtonColors
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunInfoViewModel
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunInput
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunPath
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.Landing
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.*
import com.perrigogames.life4ddr.nextgen.view.ErrorText
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FirstRunScreen(
    modifier: Modifier = Modifier,
    onComplete: (InitState) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<FirstRunInfoViewModel>()
    val step: FirstRunStep by viewModel.state.collectAsState(Landing)

    BackHandler {
        if (!viewModel.navigateBack()) {
            onClose()
        }
    }

    FirstRunContent(
        step = step,
        modifier = modifier,
        onInput = { viewModel.handleInput(it) },
        onComplete = onComplete,
    )
}

@Composable
fun FirstRunContent(
    step: FirstRunStep,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
    onComplete: (InitState) -> Unit = {},
) {
    var completeHandled by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(0.75f)
                .fillMaxHeight()
        ) {
            val contentModifier = Modifier.fillMaxWidth()

            FirstRunHeader(
                showWelcome = step == Landing,
                modifier = contentModifier
            )

            AnimatedContent(
                targetState = step,
                label = "First run content transitions",
            ) { step ->
                when (step) {
                    Landing -> {
                        FirstRunNewUser(
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is Username -> {
                        FirstRunUsername(
                            step = step,
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is UsernamePassword -> {
                        FirstRunUsernamePassword(
                            step = step,
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is RivalCode -> {
                        FirstRunRivalCode(
                            step = step,
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is SocialHandles -> {
                        FirstRunSocials(
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is InitialRankSelection -> {
                        FirstRunRankMethod(
                            step = step,
                            onInput = onInput,
                            modifier = contentModifier,
                        )
                    }
                    is Completed -> {
                        if (!completeHandled) {
                            onComplete(step.initStep)
                            completeHandled = true
                        }
                    }
                    else -> error("Unsupported step $step")
                }
            }
        }

        if (step.showNextButton) {
            Button(
                onClick = { onInput(FirstRunInput.NavigateNext) },
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            painter = painterResource(MR.images.life4_logo_invert),
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
    onInput: (FirstRunInput) -> Unit = {},
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
                onClick = { onInput(FirstRunInput.NewUserSelected(isNewUser = true)) },
                colors = primaryButtonColors(),
                content = { Text(
                    text = stringResource(MR.strings.yes),
                ) },
                modifier = Modifier.weight(1f, false)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { onInput(FirstRunInput.NewUserSelected(isNewUser = false)) },
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
fun FirstRunUsername(
    step: Username,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var usernameText by remember { mutableStateOf(step.username) }

    Column(modifier = modifier) {
        Text(
            text = step.headerText.localized(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(
            modifier = Modifier.size(16.dp)
        )
        step.descriptionText?.let { description ->
            Text(
                text = description.localized(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(
                modifier = Modifier.size(16.dp)
            )
        }
        OutlinedTextField(
            value = usernameText,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            label = { Text(
                text = stringResource(MR.strings.username),
            ) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            supportingText = {
                AnimatedVisibility(visible = step.usernameError != null) {
                    ErrorText { step.usernameError?.errorText?.localized() }
                }
            },
            onValueChange = {
                usernameText = it
                onInput(FirstRunInput.UsernameUpdated(it))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FirstRunUsernamePassword(
    step: UsernamePassword,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
//    val username: String by viewModel.username.collectAsState()
//    val password: String by viewModel.password.collectAsState()
//    val usernameError: UsernameError? by viewModel.errorOfType<UsernameError>().collectAsState(null)
//    val passwordError: PasswordError? by viewModel.errorOfType<PasswordError>().collectAsState(null)
//    val focusManager = LocalFocusManager.current
//
//    Column(modifier = modifier) {
//        OutlinedTextField(
//            value = username,
//            colors = OutlinedTextFieldDefaults.colors(
//                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
//                focusedTextColor = MaterialTheme.colorScheme.onSurface,
//            ),
//            label = { Text(
//                text = stringResource(MR.strings.username),
//            ) },
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(
//                imeAction = ImeAction.Next
//            ),
//            keyboardActions = KeyboardActions(
//                onNext = { focusManager.moveFocus(FocusDirection.Down) },
//                onDone = { focusManager.clearFocus() },
//            ),
//            supportingText = {
//                AnimatedVisibility(visible = usernameError != null) {
//                    ErrorText { usernameError?.errorText?.localized() }
//                }
//            },
//            onValueChange = { text: String -> viewModel.username.value = text },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = password,
//            colors = OutlinedTextFieldDefaults.colors(
//                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
//                focusedTextColor = MaterialTheme.colorScheme.onSurface,
//            ),
//            label = { Text(
//                text = stringResource(MR.strings.password),
//            ) },
//            singleLine = true,
//            keyboardOptions = KeyboardOptions(
//                imeAction = ImeAction.Done
//            ),
//            visualTransformation = PasswordVisualTransformation(),
//            keyboardActions = KeyboardActions(
//                onDone = { focusManager.clearFocus() },
//            ),
//            supportingText = {
//                AnimatedVisibility(visible = passwordError != null) {
//                    ErrorText { passwordError?.errorText?.localized() }
//                }
//            },
//            onValueChange = { text: String -> viewModel.username.value = text },
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
}

@Composable
fun FirstRunRivalCode(
    step: RivalCode,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(MR.strings.first_run_rival_code_header),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(MR.strings.first_run_rival_code_description_1),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(MR.strings.first_run_rival_code_description_2),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        RivalCodeEntry(
            rivalCode = step.rivalCode,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp, bottom = 8.dp),
            onInput = onInput,
        )
        AnimatedVisibility(visible = step.rivalCodeError != null) {
            ErrorText { step.rivalCodeError?.errorText?.localized() }
        }
    }
}

@Composable
fun RivalCodeEntry(
    rivalCode: String,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var rivalCodeText by remember { mutableStateOf(rivalCode) }

    BasicTextField(
        value = rivalCodeText,
        onValueChange = {
            if (it.length <= 8) {
                rivalCodeText = it
                onInput(FirstRunInput.RivalCodeUpdated(it))
                if (it.length == 8) {
                    focusManager.clearFocus()
                }
            }
        },
        textStyle = MaterialTheme.typography.labelMedium,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
        decorationBox = {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                @Composable
                fun Cell(text: String) {
                    Text(
                        text = text,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(8.dp)
                            ),
                    )
                }

                repeat(4) { idx ->
                    val char = when {
                        idx >= rivalCodeText.length -> ""
                        else -> rivalCodeText[idx].toString()
                    }
                    Cell(char)
                    Spacer(modifier = Modifier.size(6.dp))
                }
                Text(
                    text = "-",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                repeat(4) { idx ->
                    val char = when {
                        idx + 4 >= rivalCodeText.length -> ""
                        else -> rivalCodeText[idx + 4].toString()
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Cell(char)
                }
            }
        }
    )
}

@Composable
fun FirstRunSocials(
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
//    val socials: Map<SocialNetwork, String> by viewModel.socialNetworks.collectAsState()
//
//    Column(modifier = modifier) {
//        Text(
//            text = stringResource(MR.strings.first_run_social_header),
//            color = MaterialTheme.colorScheme.onSurface,
//            style = MaterialTheme.typography.headlineMedium,
//        )
//        Spacer(modifier = Modifier.size(16.dp))
//        Text(
//            text = stringResource(MR.strings.first_run_social_description),
//            color = MaterialTheme.colorScheme.onSurface,
//            style = MaterialTheme.typography.bodyMedium,
//        )
//        LazyColumn(
//            modifier = Modifier
//                .padding(vertical = 16.dp)
//        ) {
//            item {
//                Button(
//                    onClick = {},
//                    content = {
//                        Text(
//                            text = stringResource(MR.strings.first_run_social_add_new),
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//                )
//            }
//            items(socials.toList()) { (network, name) ->
//                Row {
//                    Text(
//                        text = "$network: ",
//                        color = MaterialTheme.colorScheme.onSurface,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = name,
//                        color = MaterialTheme.colorScheme.onSurface,
//                    )
//                }
//            }
//        }
//    }
}

@Composable
fun FirstRunRankMethod(
    step: InitialRankSelection,
    modifier: Modifier = Modifier,
    onInput: (FirstRunInput) -> Unit = {},
) {
    Column(modifier = modifier) {
        @Composable
        fun OptionButton(
            method: InitState,
        ) {
            Button(
                onClick = { onInput(FirstRunInput.RankMathodSelected(method)) },
                colors = primaryButtonColors(),
                content = { Text(
                    text = method.description.localized(),
                    textAlign = TextAlign.Center,
                ) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Text(
            text = stringResource(MR.strings.first_run_rank_selection_header),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.size(16.dp))

        step.path.allowedRankSelectionTypes().forEach {
            OptionButton(it)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(MR.strings.first_run_rank_selection_footer),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
        )
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
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunUsernameExistingPreview() {
    LIFE4Theme {
        FirstRunUsername(
            step = Username(FirstRunPath.EXISTING_USER_LOCAL),
        )
    }
}

@Composable
@Preview(widthDp = 480)
fun FirstRunRivalCodePreview() {
    LIFE4Theme {
        FirstRunRivalCode(
            step = RivalCode(FirstRunPath.NEW_USER_LOCAL, "12345678")
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
        FirstRunSocials()
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
