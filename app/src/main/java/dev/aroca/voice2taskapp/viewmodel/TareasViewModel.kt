package dev.aroca.voice2taskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.data.repository.TareasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

// ── Estados ────────────────────────────────────────────────────────────────────

sealed class TareasState {
    object Loading : TareasState()
    data class Success(val tareas: List<Tarea>) : TareasState()
    data class Error(val message: String) : TareasState()
}

sealed class AudioState {
    object Idle : AudioState()
    object Recording : AudioState()
    object Processing : AudioState()
    data class Propuesta(val propuesta: AudioProcesamientoResponse) : AudioState()
    data class Error(val message: String) : AudioState()
}

sealed class EditarState {
    object Idle : EditarState()
    object Loading : EditarState()
    object Success : EditarState()
    data class Error(val message: String) : EditarState()
}

enum class FiltroTareas { TODAS, PENDIENTES, COMPLETADAS, IMPORTANTES }

// ── ViewModel ──────────────────────────────────────────────────────────────────

class TareasViewModel : ViewModel() {

    private val repository = TareasRepository()

    // Tareas principales (lista detail)
    private val _tareasState = MutableStateFlow<TareasState>(TareasState.Loading)
    val tareasState: StateFlow<TareasState> = _tareasState

    // Importantes (tab)
    private val _importantesState = MutableStateFlow<TareasState>(TareasState.Loading)
    val importantesState: StateFlow<TareasState> = _importantesState

    // Búsqueda
    private val _busquedaState = MutableStateFlow<TareasState>(TareasState.Loading)
    val busquedaState: StateFlow<TareasState> = _busquedaState

    // Audio
    private val _audioState = MutableStateFlow<AudioState>(AudioState.Idle)
    val audioState: StateFlow<AudioState> = _audioState

    // Editar
    private val _editarState = MutableStateFlow<EditarState>(EditarState.Idle)
    val editarState: StateFlow<EditarState> = _editarState

    // Filtro activo
    private val _filtroActivo = MutableStateFlow(FiltroTareas.TODAS)
    val filtroActivo: StateFlow<FiltroTareas> = _filtroActivo

    // listaId en contexto para recargar después de acciones
    private var listaIdActual: Int? = null

    // ── Cargar ───────────────────────────────────────────────────────────────

    fun cargarTareas(listaId: Int? = null) {
        listaIdActual = listaId
        viewModelScope.launch {
            _tareasState.value = TareasState.Loading
            try {
                val tareas = repository.getTareas(listaId)
                _tareasState.value = TareasState.Success(tareas)
            } catch (e: Exception) {
                _tareasState.value = TareasState.Error("Error al cargar las tareas")
            }
        }
    }

    fun cargarTareasConFiltro(listaId: Int? = null, filtro: FiltroTareas) {
        listaIdActual = listaId
        _filtroActivo.value = filtro
        viewModelScope.launch {
            _tareasState.value = TareasState.Loading
            try {
                val tareas = when (filtro) {
                    FiltroTareas.TODAS -> repository.getTareas(listaId)
                    FiltroTareas.PENDIENTES -> repository.getTareas(listaId, completada = false)
                    FiltroTareas.COMPLETADAS -> repository.getTareas(listaId, completada = true)
                    FiltroTareas.IMPORTANTES -> repository.getTareas(listaId, importante = true)
                }
                _tareasState.value = TareasState.Success(tareas)
            } catch (e: Exception) {
                _tareasState.value = TareasState.Error("Error al cargar las tareas")
            }
        }
    }

    fun cargarImportantes() {
        viewModelScope.launch {
            _importantesState.value = TareasState.Loading
            try {
                val tareas = repository.getImportantes()
                _importantesState.value = TareasState.Success(tareas)
            } catch (e: Exception) {
                _importantesState.value = TareasState.Error("Error al cargar importantes")
            }
        }
    }

    fun buscarTareas(q: String) {
        if (q.isBlank()) {
            _busquedaState.value = TareasState.Success(emptyList())
            return
        }
        viewModelScope.launch {
            _busquedaState.value = TareasState.Loading
            try {
                val tareas = repository.buscarTareas(q)
                _busquedaState.value = TareasState.Success(tareas)
            } catch (e: Exception) {
                _busquedaState.value = TareasState.Error("Error al buscar")
            }
        }
    }

    // ── Acciones ─────────────────────────────────────────────────────────────

    fun completarTarea(id: Int, listaId: Int?) {
        viewModelScope.launch {
            try { repository.completarTarea(id) } catch (_: Exception) {}
            cargarTareas(listaId)
        }
    }

    fun eliminarTarea(id: Int, listaId: Int?) {
        viewModelScope.launch {
            try { repository.eliminarTarea(id) } catch (_: Exception) {}
            cargarTareas(listaId)
        }
    }

    fun marcarImportante(id: Int, importante: Boolean, listaId: Int?) {
        viewModelScope.launch {
            try {
                repository.marcarImportante(id, importante)
            } catch (_: Exception) {}
            cargarTareas(listaId)
        }
    }

    fun editarTarea(
        id: Int,
        titulo: String,
        descripcion: String?,
        fechaLimite: String?,
        prioridad: String,
        listaId: Int?
    ) {
        viewModelScope.launch {
            _editarState.value = EditarState.Loading
            try {
                repository.editarTarea(
                    id = id,
                    titulo = titulo,
                    descripcion = descripcion,
                    fechaLimite = fechaLimite,
                    prioridad = prioridad
                )
                _editarState.value = EditarState.Success
                cargarTareas(listaId)
            } catch (e: Exception) {
                _editarState.value = EditarState.Error("Error al editar la tarea")
            }
        }
    }

    fun resetEditarState() {
        _editarState.value = EditarState.Idle
    }

    // ── Audio ─────────────────────────────────────────────────────────────────

    fun procesarAudio(archivo: File) {
        viewModelScope.launch {
            _audioState.value = AudioState.Processing
            try {
                val propuesta = repository.procesarAudio(archivo)
                _audioState.value = AudioState.Propuesta(propuesta)
            } catch (e: Exception) {
                _audioState.value = AudioState.Error("Error al procesar el audio")
            }
        }
    }

    fun confirmarTarea(
        titulo: String,
        descripcion: String,
        fechaLimite: String?,
        prioridad: String,
        audioTranscripcion: String,
        listaId: Int?
    ) {
        viewModelScope.launch {
            try {
                repository.confirmarTarea(titulo, descripcion, fechaLimite, prioridad, audioTranscripcion, listaId)
                _audioState.value = AudioState.Idle
                cargarTareas(listaId)
            } catch (e: Exception) {
                _audioState.value = AudioState.Error("Error al guardar la tarea")
            }
        }
    }

    fun resetAudioState() {
        _audioState.value = AudioState.Idle
    }
}