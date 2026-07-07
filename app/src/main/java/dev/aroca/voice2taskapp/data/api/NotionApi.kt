package dev.aroca.voice2taskapp.data.api

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class NotionDatabase(val id: String, val title: List<NotionRichText>?) {
    val name: String get() = title?.firstOrNull()?.plainText ?: "Sin nombre"
}
data class NotionRichText(@SerializedName("plain_text") val plainText: String)
data class NotionSearchResponse(val results: List<NotionDatabase>)
data class NotionSearchBody(val filter: Map<String, String> = mapOf("value" to "database", "property" to "object"))

interface NotionApi {
    @POST("v1/search")
    suspend fun searchDatabases(
        @Header("Authorization") token: String,
        @Header("Notion-Version") version: String = "2026-03-11",
        @Body body: NotionSearchBody = NotionSearchBody()
    ): NotionSearchResponse

    @POST("v1/pages")
    suspend fun crearPagina(
        @Header("Authorization") token: String,
        @Header("Notion-Version") version: String = "2026-03-11",
        @Header("Content-Type") contentType: String = "application/json",
        @Body body: JsonObject
    ): JsonObject
}