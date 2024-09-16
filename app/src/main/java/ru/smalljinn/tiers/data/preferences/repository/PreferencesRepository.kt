package ru.smalljinn.tiers.data.preferences.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.preferences.model.UserSettings

interface PreferencesRepository {
    fun getSettingsStream(): Flow<UserSettings>
    suspend fun changeVibration(enabled: Boolean)
}