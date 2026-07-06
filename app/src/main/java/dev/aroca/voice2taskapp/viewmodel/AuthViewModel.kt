package dev.aroca.voice2taskapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.model.Usuario
import dev.aroca.voice2taskapp.data.repository.AuthRepository
import dev.aroca.voice2taskapp.data.repository.TokenRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val tokenRepository = TokenRepository(application)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario

    init {
        viewModelScope.launch {
            tokenRepository.token.collect { token ->
                if (token != null) {
                    ApiClient.setToken(token)
                    _state.value = AuthState.Success
                    cargarUsuario()
                }
            }
        }
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            try {
                _usuario.value = ApiClient.authApi.me()
            } catch (_: Exception) {}
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val response = authRepository.login(email, password)
                tokenRepository.saveToken(response.access_token)
                _state.value = AuthState.Success
                cargarUsuario()
            } catch (e: HttpException) {
                val mensaje = extraerMensajeError(e) ?: "Email o contraseña incorrectos"
                _state.value = AuthState.Error(mensaje)
                autoLimpiarError()
            } catch (e: Exception) {
                _state.value = AuthState.Error("No se pudo conectar con el servidor")
                autoLimpiarError()
            }
        }
    }

    fun registro(username: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                authRepository.registro(username, email, password)
                login(email, password)
            } catch (e: HttpException) {
                val mensaje = extraerMensajeError(e) ?: "Error al registrarse. Inténtalo de nuevo."
                _state.value = AuthState.Error(mensaje)
                autoLimpiarError()
            } catch (e: Exception) {
                _state.value = AuthState.Error("No se pudo conectar con el servidor")
                autoLimpiarError()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenRepository.clearToken()
            ApiClient.setToken(null)
            _state.value = AuthState.Idle
            _usuario.value = null
        }
    }

    fun clearError() {
        if (_state.value is AuthState.Error) {
            _state.value = AuthState.Idle
        }
    }

    private fun autoLimpiarError() {
        viewModelScope.launch {
            delay(7_000)
            clearError()
        }
    }

    private fun extraerMensajeError(e: HttpException): String? {
        return try {
            val body = e.response()?.errorBody()?.string() ?: return null
            JSONObject(body).getString("detail")
        } catch (_: Exception) {
            null
        }
    }
}