package dev.aroca.voice2taskapp.data.api

import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.model.BusquedaResponse
import dev.aroca.voice2taskapp.data.model.Tarea
import okhttp3.MultipartBody
import retrofit2.http.*

// ── Request bodies ─────────────────────────────────────────────────────────────

data class TareaCreateRequest(
    val titulo: String,
    val descripcion: String?,
    val fecha_limite: String?,
    val prioridad: String,
    val lista_id: Int?
)

data class TareaUpdateRequest(
    val titulo: String? = null,
    val descripcion: String? = null,
    val fecha_limite: String? = null,
    val prioridad: String? = null,
    val lista_id: Int? = null,
    val importante: Boolean? = null
)

data class ConfirmarTareaRequest(
    val titulo: String,
    val descripcion: String,
    val fecha_limite: String?,
    val prioridad: String,
    val audio_transcripcion: String,
    val lista_id: Int?
)

// ── API ────────────────────────────────────────────────────────────────────────

interface TareasApi {

    // Listar con filtros opcionales
    @GET("api/tareas/")
    suspend fun getTareas(
        @Query("lista_id") listaId: Int? = null,
        @Query("completada") completada: Boolean? = null,
        @Query("importante") importante: Boolean? = null,
        @Query("prioridad") prioridad: String? = null
    ): List<Tarea>

    // Importantes
    @GET("api/tareas/importantes")
    suspend fun getImportantes(): List<Tarea>

    // Búsqueda
    @GET("api/tareas/buscar")
    suspend fun buscarTareas(@Query("q") q: String): BusquedaResponse

    // Detalle
    @GET("api/tareas/{id}")
    suspend fun getTarea(@Path("id") id: Int): Tarea

    // Crear
    @POST("api/tareas/")
    suspend fun crearTarea(@Body request: TareaCreateRequest): Tarea

    // Editar (PATCH — solo campos enviados)
    @PATCH("api/tareas/{id}")
    suspend fun editarTarea(@Path("id") id: Int, @Body request: TareaUpdateRequest): Tarea

    // Marcar importante
    @PUT("api/tareas/{id}/importante")
    suspend fun marcarImportante(@Path("id") id: Int, @Query("importante") importante: Boolean): Tarea

    // Completar
    @PUT("api/tareas/{id}/completar")
    suspend fun completarTarea(@Path("id") id: Int): Tarea

    // Eliminar
    @DELETE("api/tareas/{id}")
    suspend fun eliminarTarea(@Path("id") id: Int)

    // Audio
    @Multipart
    @POST("api/tareas/audio")
    suspend fun procesarAudio(@Part audio: MultipartBody.Part): AudioProcesamientoResponse

    // Confirmar audio
    @POST("api/tareas/confirmar")
    suspend fun confirmarTarea(@Body request: ConfirmarTareaRequest): Tarea
}