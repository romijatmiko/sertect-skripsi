package com.skripsi.jalu.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class UserPreference(context: Context) {
    private val dataStore: DataStore<Preferences> = context.createDataStore(name = "user_prefs")

    companion object {
        private val USER_TOKEN_KEY = preferencesKey<String>("user_token")
        private val USER_ID_KEY = preferencesKey<String>("user_id") // New key for user ID
    }

    val userToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_TOKEN_KEY]
    }

    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    suspend fun saveUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(id: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

    suspend fun saveLastTokenRefreshTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[preferencesKey<Long>("last_token_refresh_time")] = time
        }
    }

    suspend fun getLastTokenRefreshTime(): Long {
        return dataStore.data.map { preferences ->
            preferences[preferencesKey<Long>("last_token_refresh_time")] ?: 0L
        }.firstOrNull() ?: 0L
    }

    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(USER_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}
