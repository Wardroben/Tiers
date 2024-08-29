package ru.smalljinn.tiers.data.preferences.model

data class UserSettings(
    val googleApiKey: String = "",
    val cx: String = "",
    val vibrationEnabled: Boolean = true
)
