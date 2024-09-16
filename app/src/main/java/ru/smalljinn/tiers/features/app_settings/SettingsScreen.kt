package ru.smalljinn.tiers.features.app_settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.smalljinn.tiers.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateBack: () -> Unit
) {
    val uiState = viewModel.settingsStream.collectAsStateWithLifecycle().value
    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopBar(navigateBack = navigateBack) }
    ) { innerPadding ->
        SettingsBody(
            modifier = Modifier
                .padding(innerPadding),
            vibrationEnabled = uiState.vibrationEnabled,
            onVibrationChanged = { viewModel.obtainEvent(SettingsEvent.ChangeVibration(it)) },

        )
    }
}

@Composable
fun SettingsBody(
    modifier: Modifier = Modifier,
    vibrationEnabled: Boolean,
    onVibrationChanged: (Boolean) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.vertical_arrangement))
    ) {
        //Instruction
        Icon(
            painter = painterResource(id = R.drawable.baseline_info_outline_24),
            contentDescription = stringResource(R.string.search_images_instruction_cd)
        )
        Text(stringResource(R.string.keys_settings_notification))
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
        vibrationEnabled = true,
    ) {}
}