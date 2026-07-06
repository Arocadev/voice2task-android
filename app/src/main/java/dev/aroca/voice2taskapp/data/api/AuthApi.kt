package dev.aroca.voice2taskapp.data.api

import dev.aroca.voice2taskapp.data.model.LoginRequest
import dev.aroca.voice2taskapp.data.model.RegistroRequest
import dev.aroca.voice2taskapp.data.model.TokenResponse
import dev.aroca.voice2taskapp.data.model.Usuario
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("api/auth/registro")
    suspend fun registro(@Body request: RegistroRequest): Usuario

    @GET("api/auth/me")
    suspend fun me(): Usuario

    @PUT("api/auth/cambiar-password")
    suspend fun cambiarPassword(@Body request: Map<String, String>)
}