package ru.smalljinn.tiers.features.app_settings

sealed class SettingsEvent {
    data class ChangeVibration(val enabled: Boolean) : SettingsEvent()
}