package ru.smalljinn.tiers.data.preferences.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.smalljinn.tiers.data.preferences.model.UserSettings

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepositoryImpl(private val appContext: Context) : PreferencesRepository {
    private object PreferencesKeys {
        val VIBRATION = booleanPreferencesKey("vibration")
        val GOOGLE_API_KEY = stringPreferencesKey("google_api_key")
        val CX = stringPreferencesKey("cx")
    }

    override fun getSettingsStream(): Flow<UserSettings> =
        appContext.dataStore.data.map { preferences ->
            UserSettings(
                googleApiKey = preferences[PreferencesKeys.GOOGLE_API_KEY] ?: String(),
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION] ?: true,
                cx = preferences[PreferencesKeys.CX] ?: String()
            )
        }

    override suspend fun changeVibration(enabled: Boolean) {
        appContext.dataStore.edit { settings ->
            settings[PreferencesKeys.VIBRATION] = enabled
        }
    }

    override suspend fun changeApiKey(key: String) {
        appContext.dataStore.edit { settings ->
            settings[PreferencesKeys.GOOGLE_API_KEY] = key
        }
    }

    override suspend fun changeCX(cx: String) {
        appContext.dataStore.edit { settings ->
            settings[PreferencesKeys.CX] = cx
        }
    }
}