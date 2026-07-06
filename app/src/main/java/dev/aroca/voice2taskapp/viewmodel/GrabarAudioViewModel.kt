package dev.aroca.voice2taskapp.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.api.ConfirmarTareaRequest
import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.model.Tarea
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

enum class PasosProcesando {
    TRANSCRIBIENDO,
    ENTENDIENDO,
    CREANDO
}

sealed class GrabarState {
    object Idle : GrabarState()
    object Grabando : GrabarState()
    data class Procesando(val paso: PasosProcesando = PasosProcesando.TRANSCRIBIENDO) : GrabarState()
    data class Propuesta(val respuesta: AudioProcesamientoResponse) : GrabarState()
    data class Confirmada(val tarea: Tarea) : GrabarState()
    data class Error(val message: String) : GrabarState()
}

class GrabarAudioViewModel : ViewModel() {

    private val _state = MutableStateFlow<GrabarState>(GrabarState.Idle)
    val state: StateFlow<GrabarState> = _state

    private val _segundos = MutableStateFlow(0)
    val segundos: StateFlow<Int> = _segundos

    private var mediaRecorder: MediaRecorder? = null
    private var archivoAudio: File? = null
    private var timerJob: kotlinx.coroutines.Job? = null

    fun iniciarGrabacion(context: Context) {
        val archivo = File(context.cacheDir, "audio_voice2task.ogg")
        archivoAudio = archivo
        _segundos.value = 0

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.OGG)
            setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            setOutputFile(archivo.absolutePath)
            prepare()
            start()
        }

        _state.value = GrabarState.Grabando

        timerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _segundos.value++
            }
        }
    }

    fun detenerYProcesar() {
        timerJob?.cancel()
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) { /* ignorar */ }
        mediaRecorder = null

        _state.value = GrabarState.Procesando(PasosProcesando.TRANSCRIBIENDO)
        enviarAudio()
    }

    fun cancelar() {
        timerJob?.cancel()
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) { /* ignorar */ }
        mediaRecorder = null
        archivoAudio?.delete()
        archivoAudio = null
        _state.value = GrabarState.Idle
    }

    private fun enviarAudio() {
        val archivo = archivoAudio ?: run {
            _state.value = GrabarState.Error("No se encontró el archivo de audio")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = GrabarState.Procesando(PasosProcesando.TRANSCRIBIENDO)

                val requestBody = archivo.asRequestBody("audio/ogg".toMediaType())
                val part = MultipartBody.Part.createFormData("audio", archivo.name, requestBody)

                // Lanzar API en paralelo usando viewModelScope.async
                val apiCall: Deferred<AudioProcesamientoResponse> = viewModelScope.async(kotlinx.coroutines.Dispatchers.IO) {
                    ApiClient.tareasApi.procesarAudio(part)
                }

                // Mostrar cada paso mínimo tiempo para que se vea el spinner
                kotlinx.coroutines.delay(1500)
                _state.value = GrabarState.Procesando(PasosProcesando.ENTENDIENDO)

                kotlinx.coroutines.delay(1000)
                _state.value = GrabarState.Procesando(PasosProcesando.CREANDO)

                // Esperar respuesta del backend (si aún no ha llegado)
                val respuesta = apiCall.await()

                kotlinx.coroutines.delay(500)
                _state.value = GrabarState.Propuesta(respuesta)
            } catch (e: Exception) {
                _state.value = GrabarState.Error(e.message ?: "Error al procesar el audio")
            }
        }
    }

    fun confirmarTarea(propuesta: AudioProcesamientoResponse, listaId: Int?) {
        viewModelScope.launch {
            try {
                val request = ConfirmarTareaRequest(
                    titulo = propuesta.titulo,
                    descripcion = propuesta.descripcion,
                    fecha_limite = propuesta.fecha_limite,
                    prioridad = propuesta.prioridad,
                    audio_transcripcion = propuesta.audio_transcripcion,
                    lista_id = listaId
                )
                val tarea = ApiClient.tareasApi.confirmarTarea(request)
                _state.value = GrabarState.Confirmada(tarea)
            } catch (e: Exception) {
                _state.value = GrabarState.Error(e.message ?: "Error al guardar la tarea")
            }
        }
    }

    fun resetear() {
        _state.value = GrabarState.Idle
        _segundos.value = 0
        archivoAudio?.delete()
        archivoAudio = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelar()
    }
}