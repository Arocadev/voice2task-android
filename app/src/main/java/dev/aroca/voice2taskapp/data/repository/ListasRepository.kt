package dev.aroca.voice2taskapp.data.repository

import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.api.ListaCreateRequest
import dev.aroca.voice2taskapp.data.model.Lista

class ListasRepository {

    suspend fun getListas(): List<Lista> {
        return ApiClient.listasApi.getListas()
    }

    suspend fun crearLista(nombre: String, color: String?): Lista {
        return ApiClient.listasApi.crearLista(ListaCreateRequest(nombre, color))
    }

    suspend fun editarLista(id: Int, nombre: String, color: String?): Lista {
        return ApiClient.listasApi.editarLista(id, ListaCreateRequest(nombre, color))
    }

    suspend fun eliminarLista(id: Int) {
        ApiClient.listasApi.eliminarLista(id)
    }
}