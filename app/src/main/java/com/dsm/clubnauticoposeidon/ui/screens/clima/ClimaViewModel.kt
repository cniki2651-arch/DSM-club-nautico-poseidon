package com.dsm.clubnauticoposeidon.ui.screens.clima

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.ClimaRepository
import com.dsm.clubnauticoposeidon.model.ClimaDetalle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClimaViewModel : ViewModel() {

    private val repo = ClimaRepository()

    private val _clima = MutableStateFlow<ClimaDetalle?>(null)
    val clima: StateFlow<ClimaDetalle?> = _clima

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarClima()
    }

    /**
     * Consulta de forma asíncrona los datos climáticos y marítimos a través del repositorio.
     * Actualiza el flujo reactivo de estado y maneja posibles errores de conexión.
     */
    fun cargarClima() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null

            repo.obtenerClimaDetallado().fold(
                onSuccess = { detalle ->
                    _clima.value = detalle
                    _cargando.value = false
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Error desconocido"
                    _cargando.value = false
                }
            )
        }
    }
}
