package com.dsm.clubnauticoposeidon.ui.screens.naves


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.EmbarcacionRepository
import com.dsm.clubnauticoposeidon.model.Embarcacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisNavesUiState(
    val naves: List<Embarcacion> = emptyList(),
    val dni: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val registrada: Boolean = false
)

class MisNavesViewModel(
    private val repo: EmbarcacionRepository = EmbarcacionRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(MisNavesUiState())
    val ui: StateFlow<MisNavesUiState> = _ui

    init {
        viewModelScope.launch {
            val dni = try { repo.obtenerDniActual() } catch (e: Exception) { null }
            if (dni.isNullOrBlank()) {
                _ui.update { it.copy(cargando = false, mensaje = "No se encontró tu DNI en el perfil") }
                return@launch
            }
            _ui.update { it.copy(dni = dni) }
            repo.observarNaves(dni)
                .catch { e -> _ui.update { it.copy(cargando = false, mensaje = "Error: ${e.message}") } }
                .collect { lista -> _ui.update { it.copy(naves = lista, cargando = false) } }
        }
    }

    fun registrar(matricula: String, nombre: String, tipo: String, esloraTexto: String) {
        val dni = _ui.value.dni ?: return
        val eslora = esloraTexto.replace(',', '.').toDoubleOrNull()
        if (eslora == null || eslora <= 0 || eslora > 200) {
            _ui.update { it.copy(mensaje = "Ingresa una eslora válida") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true) }
            try {
                repo.registrarNave(dni, matricula.trim().uppercase(), nombre.trim(), tipo, eslora)
                _ui.update { it.copy(guardando = false, registrada = true, mensaje = "Embarcación registrada") }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = e.message ?: "No se pudo registrar") }
            }
        }
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }
    fun registroProcesado() = _ui.update { it.copy(registrada = false) }
}