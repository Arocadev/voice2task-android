package dev.aroca.voice2taskapp.data.repository

import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.api.ConfirmarTareaRequest
import dev.aroca.voice2taskapp.data.api.TareaCreateRequest
import dev.aroca.voice2taskapp.data.api.TareaUpdateRequest
import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.model.Tarea
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TareasRepository {

    suspend fun getTareas(
        listaId: Int? = null,
        completada: Boolean? = null,
        importante: Boolean? = null,
        prioridad: String? = null
    ): List<Tarea> {
        return ApiClient.tareasApi.getTareas(listaId, completada, importante, prioridad)
    }

    suspend fun getImportantes(): List<Tarea> {
        return ApiClient.tareasApi.getImportantes()
    }

    suspend fun buscarTareas(q: String): List<Tarea> {
        return ApiClient.tareasApi.buscarTareas(q).tareas
    }

    suspend fun crearTarea(
        titulo: String,
        descripcion: String?,
        fechaLimite: String?,
        prioridad: String,
        listaId: Int?
    ): Tarea {
        return ApiClient.tareasApi.crearTarea(
            TareaCreateRequest(titulo, descripcion, fechaLimite, prioridad, listaId)
        )
    }

    suspend fun editarTarea(
        id: Int,
        titulo: String? = null,
        descripcion: String? = null,
        fechaLimite: String? = null,
        prioridad: String? = null,
        importante: Boolean? = null
    ): Tarea {
        return ApiClient.tareasApi.editarTarea(
            id,
            TareaUpdateRequest(titulo, descripcion, fechaLimite, prioridad, importante = importante)
        )
    }

    suspend fun marcarImportante(id: Int, importante: Boolean): Tarea {
        return ApiClient.tareasApi.marcarImportante(id, importante)
    }

    suspend fun procesarAudio(archivo: File): AudioProcesamientoResponse {
        val requestBody = archivo.asRequestBody("audio/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("audio", archivo.name, requestBody)
        return ApiClient.tareasApi.procesarAudio(part)
    }

    suspend fun confirmarTarea(
        titulo: String,
        descripcion: String,
        fechaLimite: String?,
        prioridad: String,
        audioTranscripcion: String,
        listaId: Int?
    ): Tarea {
        return ApiClient.tareasApi.confirmarTarea(
            ConfirmarTareaRequest(titulo, descripcion, fechaLimite, prioridad, audioTranscripcion, listaId)
        )
    }

    suspend fun completarTarea(id: Int): Tarea {
        return ApiClient.tareasApi.completarTarea(id)
    }

    suspend fun eliminarTarea(id: Int) {
        ApiClient.tareasApi.eliminarTarea(id)
    }
}