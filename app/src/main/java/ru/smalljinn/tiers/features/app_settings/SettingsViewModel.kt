package ru.smalljinn.tiers.features.app_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.util.EventHandler
import javax.inject.Inject

const val CX_REGEX = "^[A-Z0-9]+$"
const val API_REGEX = "^[A-Z0-9-+/|]+$"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel(), EventHandler<SettingsEvent> {
    init {
        viewModelScope.launch {
            val userSettings = preferencesRepository.getSettingsStream().first()
            apiKey = userSettings.googleApiKey
            cx = userSettings.cx
        }
    }

    private var apiKey by mutableStateOf("")
    private var cx by mutableStateOf("")

    val settingsStream = combine(
        snapshotFlow { apiKey },
        snapshotFlow { cx },
        preferencesRepository.getSettingsStream()
    ) { apiKey, cx, settings ->
        SettingsUiState(apiKey = apiKey, cx = cx, vibrationEnabled = settings.vibrationEnabled)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        SettingsUiState(apiError = false, cxError = false)
    )

    override fun obtainEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ChangeApiKey -> {
                apiKey = event.key
                viewModelScope.launch {
                    preferencesRepository.changeApiKey(apiKey)
                }
            }

            is SettingsEvent.ChangeVibration -> viewModelScope.launch {
                preferencesRepository.changeVibration(event.enabled)
            }

            is SettingsEvent.ChangeCX -> {
                cx = event.cx
                viewModelScope.launch {
                    preferencesRepository.changeCX(cx)
                }
            }
        }
    }
}