package ru.smalljinn.tiers.features.app_settings

data class SettingsUiState(
    val apiKey: String = "",
    val cx: String = "",
    val apiError: Boolean = hasApiKeyError(apiKey),
    val cxError: Boolean = hasCxKeyError(cx),
    val vibrationEnabled: Boolean = true
)

private fun hasApiKeyError(key: String): Boolean = key.isBlank()
        || key.length != 39
        || !key.contains(API_REGEX.toRegex(RegexOption.IGNORE_CASE))

private fun hasCxKeyError(cx: String): Boolean = cx.isBlank()
        || cx.length != 17
        || !cx.contains(CX_REGEX.toRegex(RegexOption.IGNORE_CASE))