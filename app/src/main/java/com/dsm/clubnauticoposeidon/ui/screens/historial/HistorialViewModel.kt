package com.dsm.clubnauticoposeidon.ui.screens.historial



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.RadaRepository
import com.dsm.clubnauticoposeidon.model.HistorialItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistorialUiState(
    val items: List<HistorialItem> = emptyList(),
    val filtro: String = "todas",   // todas | activa | finalizada
    val cargando: Boolean = true,
    val error: String? = null
) {
    val visibles: List<HistorialItem>
        get() = if (filtro == "todas") items else items.filter { it.estado == filtro }
}

class HistorialViewModel(
    private val repo: RadaRepository = RadaRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(HistorialUiState())
    val ui: StateFlow<HistorialUiState> = _ui

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, error = null) }
            try {
                val lista = repo.obtenerHistorial()
                _ui.update { it.copy(items = lista, cargando = false) }
            } catch (e: Exception) {
                _ui.update { it.copy(cargando = false, error = "No se pudo cargar: ${e.message}") }
            }
        }
    }

    fun cambiarFiltro(filtro: String) {
        _ui.update { it.copy(filtro = filtro) }
    }
}