package ru.smalljinn.tiers.presentation.ui.screens.settings

sealed class SettingsEvent {
    data class ChangeVibration(val enabled: Boolean) : SettingsEvent()
    data class ChangeApiKey(val key: String) : SettingsEvent()
    data class ChangeCX(val cx: String) : SettingsEvent()
}