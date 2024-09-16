package ru.smalljinn.tiers.data.preferences.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.smalljinn.tiers.data.preferences.model.UserSettings
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : PreferencesRepository {
    private object PreferencesKeys {
        val VIBRATION = booleanPreferencesKey("vibration")
    }

    override fun getSettingsStream(): Flow<UserSettings> =
        appContext.dataStore.data.map { preferences ->
            UserSettings(
                vibrationEnabled = preferences[PreferencesKeys.VIBRATION] ?: true,
            )
        }

    override suspend fun changeVibration(enabled: Boolean) {
        appContext.dataStore.edit { settings ->
            settings[PreferencesKeys.VIBRATION] = enabled
        }
    }
}