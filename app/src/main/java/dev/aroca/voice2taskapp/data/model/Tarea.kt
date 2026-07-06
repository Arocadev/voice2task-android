package dev.aroca.voice2taskapp.data.model

data class Tarea(
    val id: Int,
    val lista_id: Int?,
    val titulo: String,
    val descripcion: String?,
    val fecha_limite: String?,
    val prioridad: String,
    val completada: Boolean,
    val importante: Boolean = false,
    val fecha_completada: String?,
    val origen: String,
    val audio_transcripcion: String?,
    val created_at: String,
    val updated_at: String
)