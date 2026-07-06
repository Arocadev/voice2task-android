package dev.aroca.voice2taskapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.aroca.voice2taskapp.data.api.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voice2task_prefs")

class TokenRepository(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val GROQ_KEY = stringPreferencesKey("groq_api_key")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val groqKey: Flow<String?> = context.dataStore.data.map { it[GROQ_KEY] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
        ApiClient.setToken(token)
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
        ApiClient.setToken(null)
    }

    suspend fun saveGroqKey(key: String) {
        context.dataStore.edit { it[GROQ_KEY] = key }
    }
}