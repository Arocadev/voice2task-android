package dev.aroca.voice2taskapp.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegistroRequest(
    val username: String,
    val email: String,
    val password: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class AudioProcesamientoResponse(
    val titulo: String,
    val descripcion: String,
    val fecha_limite: String?,
    val prioridad: String,
    val audio_transcripcion: String
)