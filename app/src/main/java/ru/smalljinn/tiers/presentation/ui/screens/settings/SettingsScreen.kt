package ru.smalljinn.tiers.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.smalljinn.tiers.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
    navigateBack: () -> Unit
) {
    val uiState = viewModel.settingsStream.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopBar(navigateBack = navigateBack) }
    ) { innerPadding ->
        SettingsBody(
            modifier = Modifier.padding(innerPadding),
            apiKey = uiState.apiKey,
            cxKey = uiState.cx,
            vibrationEnabled = uiState.vibrationEnabled,
            onVibrationChanged = { viewModel.obtainEvent(SettingsEvent.ChangeVibration(it)) },
            onApiKeyChanged = { viewModel.obtainEvent(SettingsEvent.ChangeApiKey(it)) },
            onCxKeyChanged = { viewModel.obtainEvent(SettingsEvent.ChangeCX(it)) }
        )
    }
}

@Composable
fun SettingsBody(
    modifier: Modifier = Modifier,
    apiKey: String,
    cxKey: String,
    onApiKeyChanged: (String) -> Unit,
    onCxKeyChanged: (String) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationChanged: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val instructionBeforeLink = stringResource(id = R.string.google_api_instruction_before_link)
    val instructionAfterLink = stringResource(id = R.string.google_api_instruction_after_link)
    val linkStyle = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.vertical_arrangement))
    ) {
        //Instruction
        val instructionText = remember {
            buildAnnotatedString {
                append(instructionBeforeLink)
                pushStringAnnotation(
                    tag = "instruction",
                    annotation = "https://developers.google.com/custom-search/v1/using_rest?hl=ru#making_a_request"
                )
                withStyle(style = SpanStyle(color = linkStyle)) {
                    append(" instruction")
                }
                pop()
                append("\n\n")
                append(instructionAfterLink)
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.baseline_info_outline_24),
            contentDescription = stringResource(R.string.search_images_instruction_cd)
        )
        ClickableText(text = instructionText) { offset ->
            instructionText.getStringAnnotations(tag = "instruction", start = offset, end = offset)
                .firstOrNull()?.let { link ->
                    uriHandler.openUri(link.item)
                }
        }

        //API KEY
        KeysTextField(
            key = apiKey,
            contentDescription = stringResource(R.string.google_api_key_cd),
            label = stringResource(R.string.google_api_key_cd)
        ) { key -> onApiKeyChanged(key) }
        //CX
        KeysTextField(
            key = cxKey,
            contentDescription = stringResource(id = R.string.cx_key_cd),
            label = stringResource(id = R.string.cx_key_cd)
        ) { cx -> onCxKeyChanged(cx) }
        VibrationSetting(enabled = vibrationEnabled) { enabled -> onVibrationChanged(enabled) }
    }
}

@Composable
fun VibrationSetting(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onVibrationChanged: (Boolean) -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.vibration),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = enabled, onCheckedChange = onVibrationChanged)
    }
}

@Composable
private fun KeysTextField(
    modifier: Modifier = Modifier,
    key: String,
    contentDescription: String,
    label: String,
    onTextChanged: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = key,
        onValueChange = onTextChanged,
        singleLine = true,
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_key_24),
                contentDescription = contentDescription
            )
        },
        label = { Text(text = label) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsTopBar(navigateBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back_cd),
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    SettingsBody(
        apiKey = "abjodfet4382653",
        onApiKeyChanged = {},
        vibrationEnabled = true,
        onCxKeyChanged = {}, cxKey = "dsfghj4r6thd"
    ) {

    }
}