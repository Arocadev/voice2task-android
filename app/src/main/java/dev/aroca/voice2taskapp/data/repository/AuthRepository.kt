package dev.aroca.voice2taskapp.data.repository

import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.model.LoginRequest
import dev.aroca.voice2taskapp.data.model.RegistroRequest
import dev.aroca.voice2taskapp.data.model.TokenResponse
import dev.aroca.voice2taskapp.data.model.Usuario

class AuthRepository {

    suspend fun login(email: String, password: String): TokenResponse {
        return ApiClient.authApi.login(LoginRequest(email, password))
    }

    suspend fun registro(username: String, email: String, password: String): Usuario {
        return ApiClient.authApi.registro(RegistroRequest(username, email, password))
    }
}