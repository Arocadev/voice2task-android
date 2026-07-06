package dev.aroca.voice2taskapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.aroca.voice2taskapp.data.model.Lista
import dev.aroca.voice2taskapp.data.repository.ListasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ListasState {
    object Loading : ListasState()
    data class Success(val listas: List<Lista>) : ListasState()
    data class Error(val message: String) : ListasState()
}

class ListasViewModel : ViewModel() {

    private val repository = ListasRepository()

    private val _state = MutableStateFlow<ListasState>(ListasState.Loading)
    val state: StateFlow<ListasState> = _state

    init {
        cargarListas()
    }

    fun cargarListas() {
        viewModelScope.launch {
            _state.value = ListasState.Loading
            try {
                val listas = repository.getListas()
                _state.value = ListasState.Success(listas)
            } catch (e: Exception) {
                _state.value = ListasState.Error("Error al cargar las listas")
            }
        }
    }

    fun crearLista(nombre: String, color: String?) {
        viewModelScope.launch {
            try {
                repository.crearLista(nombre, color)
                cargarListas()
            } catch (e: Exception) {
                _state.value = ListasState.Error("Error al crear la lista")
            }
        }
    }

    fun eliminarLista(id: Int) {
        viewModelScope.launch {
            try {
                repository.eliminarLista(id)
            } catch (e: Exception) {
                // El backend devuelve 204 sin cuerpo — Retrofit puede lanzar excepción
                // al parsear la respuesta vacía aunque el borrado fue exitoso.
                // Recargamos igualmente para reflejar el estado real.
            }
            cargarListas()
        }
    }
}