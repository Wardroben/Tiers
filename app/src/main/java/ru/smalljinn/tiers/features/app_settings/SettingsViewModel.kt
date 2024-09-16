package ru.smalljinn.tiers.features.app_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.util.EventHandler
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel(), EventHandler<SettingsEvent> {

    val settingsStream =
        preferencesRepository.getSettingsStream().map { settings ->
            SettingsUiState(vibrationEnabled = settings.vibrationEnabled)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            SettingsUiState()
        )

    override fun obtainEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ChangeVibration -> viewModelScope.launch {
                preferencesRepository.changeVibration(event.enabled)
            }
        }
    }
}