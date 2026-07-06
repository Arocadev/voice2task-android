package dev.aroca.voice2taskapp.data.api

import dev.aroca.voice2taskapp.data.model.Lista
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class ListaCreateRequest(val nombre: String, val color: String?)

interface ListasApi {
    @GET("api/listas/")
    suspend fun getListas(): List<Lista>

    @POST("api/listas/")
    suspend fun crearLista(@Body request: ListaCreateRequest): Lista

    @PUT("api/listas/{id}")
    suspend fun editarLista(@Path("id") id: Int, @Body request: ListaCreateRequest): Lista

    @DELETE("api/listas/{id}")
    suspend fun eliminarLista(@Path("id") id: Int)
}