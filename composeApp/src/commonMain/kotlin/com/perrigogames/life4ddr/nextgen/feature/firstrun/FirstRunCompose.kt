package com.perrigogames.life4ddr.nextgen.feature.firstrun

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.perrigogames.life4ddr.nextgen.MR
import com.perrigogames.life4ddr.nextgen.compose.primaryButtonColors
import com.perrigogames.life4ddr.nextgen.feature.firstrun.manager.InitState
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunError.PasswordError
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunError.RivalCodeError
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunError.UsernameError
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunInfoViewModel
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.InitialRankSelection
import com.perrigogames.life4ddr.nextgen.feature.firstrun.viewmodel.FirstRunStep.PathStep.Username
import com.perrigogames.life4ddr.nextgen.feature.profile.data.SocialNetwork
import com.perrigogames.life4ddr.nextgen.view.ErrorText
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun FirstRunUsername(
    step: Username,
    viewModel: FirstRunInfoViewModel,
    modifier: Modifier = Modifier,
) {
    val username: String by viewModel.username.collectAsState()
    val error: UsernameError? by viewModel.errorOfType<UsernameError>().collectAsState(null)
    val focusManager = LocalFocusManager.current

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
            value = username,
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
                AnimatedVisibility(visible = error != null) {
                    ErrorText { error?.errorText?.localized() }
                }
            },
            onValueChange = { text: String -> viewModel.username.value = text },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FirstRunUsernamePassword(
    viewModel: FirstRunInfoViewModel,
    modifier: Modifier = Modifier,
) {
    val username: String by viewModel.username.collectAsState()
    val password: String by viewModel.password.collectAsState()
    val usernameError: UsernameError? by viewModel.errorOfType<UsernameError>().collectAsState(null)
    val passwordError: PasswordError? by viewModel.errorOfType<PasswordError>().collectAsState(null)
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = username,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            label = { Text(
                text = stringResource(MR.strings.username),
            ) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                onDone = { focusManager.clearFocus() },
            ),
            supportingText = {
                AnimatedVisibility(visible = usernameError != null) {
                    ErrorText { usernameError?.errorText?.localized() }
                }
            },
            onValueChange = { text: String -> viewModel.username.value = text },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            label = { Text(
                text = stringResource(MR.strings.password),
            ) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            supportingText = {
                AnimatedVisibility(visible = passwordError != null) {
                    ErrorText { passwordError?.errorText?.localized() }
                }
            },
            onValueChange = { text: String -> viewModel.username.value = text },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FirstRunRivalCode(
    viewModel: FirstRunInfoViewModel,
    modifier: Modifier = Modifier,
) {
    val rivalCode: String by viewModel.rivalCode.collectAsState()
    val error: RivalCodeError? by viewModel.errorOfType<RivalCodeError>().collectAsState(null)

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
            rivalCode = rivalCode,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 32.dp, bottom = 8.dp),
        ) { viewModel.rivalCode.value = it }
        AnimatedVisibility(visible = error != null) {
            ErrorText { error?.errorText?.localized() }
        }
    }
}

@Composable
fun RivalCodeEntry(
    rivalCode: String,
    modifier: Modifier = Modifier,
    onTextChanged: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = rivalCode,
        onValueChange = {
            if (it.length <= 8) {
                onTextChanged(it)
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
                        idx >= rivalCode.length -> ""
                        else -> rivalCode[idx].toString()
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
                        idx + 4 >= rivalCode.length -> ""
                        else -> rivalCode[idx + 4].toString()
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
    viewModel: FirstRunInfoViewModel,
) {
    val socials: Map<SocialNetwork, String> by viewModel.socialNetworks.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = stringResource(MR.strings.first_run_social_header),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(MR.strings.first_run_social_description),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        LazyColumn(
            modifier = Modifier
                .padding(vertical = 16.dp)
        ) {
            item {
                Button(
                    onClick = {},
                    content = {
                        Text(
                            text = stringResource(MR.strings.first_run_social_add_new),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
            items(socials.toList()) { (network, name) ->
                Row {
                    Text(
                        text = "$network: ",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun FirstRunRankMethod(
    step: InitialRankSelection,
    modifier: Modifier = Modifier,
    onRankMethodSelected: (InitState) -> Unit = {},
) {
    Column(modifier = modifier) {
        @Composable
        fun OptionButton(
            method: InitState,
        ) {
            Button(
                onClick = { onRankMethodSelected(method) },
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
