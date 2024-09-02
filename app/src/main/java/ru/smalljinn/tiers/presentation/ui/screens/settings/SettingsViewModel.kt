package ru.smalljinn.tiers.presentation.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.TierApp
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.util.EventHandler

const val CX_REGEX = "^[A-Z0-9]+$"
const val API_REGEX = "^[A-Z0-9-+/|]+$"

data class SettingsUiState(
    val apiKey: String = "",
    val cx: String = "",
    val apiError: Boolean = apiKey.isBlank()
            || apiKey.length != 39
            || !apiKey.contains(API_REGEX.toRegex(RegexOption.IGNORE_CASE)),
    val cxError: Boolean = cx.isBlank()
            || cx.length != 17
            || !cx.contains(CX_REGEX.toRegex(RegexOption.IGNORE_CASE)),
    val vibrationEnabled: Boolean = true
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel(), EventHandler<SettingsEvent> {
    init {
        viewModelScope.launch {
            val stream = preferencesRepository.getSettingsStream().first()
            apiKey = stream.googleApiKey
            cx = stream.cx
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val appContainer = (this[APPLICATION_KEY] as TierApp).appContainer
                SettingsViewModel(
                    preferencesRepository = appContainer.preferencesRepository
                )
            }
        }
    }
}