package dev.aroca.voice2taskapp.data.model

data class BusquedaResponse(
    val total: Int,
    val tareas: List<Tarea>
)