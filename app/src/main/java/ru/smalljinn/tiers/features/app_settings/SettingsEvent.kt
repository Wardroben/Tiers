package ru.smalljinn.tiers.features.app_settings

sealed class SettingsEvent {
    data class ChangeVibration(val enabled: Boolean) : SettingsEvent()
    data class ChangeApiKey(val key: String) : SettingsEvent()
    data class ChangeCX(val cx: String) : SettingsEvent()
}