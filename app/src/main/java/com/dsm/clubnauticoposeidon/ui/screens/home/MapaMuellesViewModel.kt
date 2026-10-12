package com.dsm.clubnauticoposeidon.ui.screens.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.RadaRepository
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapaUiState(
    val radas: List<Rada> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)

class MapaMuellesViewModel(
    private val repo: RadaRepository = RadaRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(MapaUiState())
    val ui: StateFlow<MapaUiState> = _ui

    init {
        viewModelScope.launch {
            repo.observarRadas()
                .catch { e ->
                    _ui.update { it.copy(cargando = false, mensaje = "Error al cargar radas: ${e.message}") }
                }
                .collect { lista ->
                    _ui.update { it.copy(radas = lista.sortedBy { r -> r.codigo }, cargando = false) }
                }
        }
    }

    fun marcarMantenimiento(radaId: String) = cambiar(radaId, EstadoRada.MANTENIMIENTO, "Rada en mantenimiento")

    fun marcarDisponible(radaId: String) = cambiar(radaId, EstadoRada.DISPONIBLE, "Rada disponible")

    private fun cambiar(radaId: String, estado: String, exito: String) {
        viewModelScope.launch {
            try {
                repo.cambiarEstado(radaId, estado)
                _ui.update { it.copy(mensaje = exito) }
            } catch (e: Exception) {
                _ui.update { it.copy(mensaje = e.message ?: "No se pudo cambiar el estado") }
            }
        }
    }

    fun liberar(radaId: String) {
        viewModelScope.launch {
            try {
                repo.liberarRada(radaId)
                _ui.update { it.copy(mensaje = "Rada liberada") }
            } catch (e: Exception) {
                _ui.update { it.copy(mensaje = e.message ?: "No se pudo liberar la rada") }
            }
        }
    }

    fun mensajeMostrado() {
        _ui.update { it.copy(mensaje = null) }
    }
}