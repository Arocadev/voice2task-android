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
        val TOKEN_KEY                  = stringPreferencesKey("jwt_token")
        val GROQ_KEY                   = stringPreferencesKey("groq_api_key")
        val TRELLO_API_KEY             = stringPreferencesKey("trello_api_key")
        val TRELLO_TOKEN               = stringPreferencesKey("trello_token")
        val TRELLO_LIST_ID             = stringPreferencesKey("trello_list_id")
        val TRELLO_LIST_NAME           = stringPreferencesKey("trello_list_name")
        val NOTION_TOKEN               = stringPreferencesKey("notion_token")
        val NOTION_DATABASE_ID         = stringPreferencesKey("notion_database_id")
        val NOTION_DATABASE_NAME       = stringPreferencesKey("notion_database_name")
    }

    val token: Flow<String?>              = context.dataStore.data.map { it[TOKEN_KEY] }
    val groqKey: Flow<String?>            = context.dataStore.data.map { it[GROQ_KEY] }
    val trelloApiKey: Flow<String?>       = context.dataStore.data.map { it[TRELLO_API_KEY] }
    val trelloToken: Flow<String?>        = context.dataStore.data.map { it[TRELLO_TOKEN] }
    val trelloListId: Flow<String?>       = context.dataStore.data.map { it[TRELLO_LIST_ID] }
    val trelloListName: Flow<String?>     = context.dataStore.data.map { it[TRELLO_LIST_NAME] }
    val notionToken: Flow<String?>        = context.dataStore.data.map { it[NOTION_TOKEN] }
    val notionDatabaseId: Flow<String?>   = context.dataStore.data.map { it[NOTION_DATABASE_ID] }
    val notionDatabaseName: Flow<String?> = context.dataStore.data.map { it[NOTION_DATABASE_NAME] }

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

    suspend fun saveTrello(apiKey: String, token: String) {
        context.dataStore.edit {
            it[TRELLO_API_KEY] = apiKey
            it[TRELLO_TOKEN] = token
        }
    }

    suspend fun saveTrelloList(id: String, name: String) {
        context.dataStore.edit {
            it[TRELLO_LIST_ID] = id
            it[TRELLO_LIST_NAME] = name
        }
    }

    suspend fun saveNotion(token: String) {
        context.dataStore.edit { it[NOTION_TOKEN] = token }
    }

    suspend fun saveNotionDatabase(id: String, name: String) {
        context.dataStore.edit {
            it[NOTION_DATABASE_ID] = id
            it[NOTION_DATABASE_NAME] = name
        }
    }
}