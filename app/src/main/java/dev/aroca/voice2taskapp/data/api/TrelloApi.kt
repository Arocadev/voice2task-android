package dev.aroca.voice2taskapp.data.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class TrelloBoard(val id: String, val name: String)
data class TrelloList(val id: String, val name: String)
data class TrelloCard(val id: String, val name: String, val url: String)

interface TrelloApi {
    @GET("1/members/me/boards")
    suspend fun getBoards(
        @Query("key") apiKey: String,
        @Query("token") token: String,
        @Query("fields") fields: String = "id,name"
    ): List<TrelloBoard>

    @GET("1/boards/{boardId}/lists")
    suspend fun getLists(
        @retrofit2.http.Path("boardId") boardId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String,
        @Query("fields") fields: String = "id,name"
    ): List<TrelloList>

    @POST("1/cards")
    suspend fun crearTarjeta(
        @Query("idList") idList: String,
        @Query("name") name: String,
        @Query("desc") desc: String,
        @Query("due") due: String?,
        @Query("key") key: String,
        @Query("token") token: String
    ): TrelloCard
}