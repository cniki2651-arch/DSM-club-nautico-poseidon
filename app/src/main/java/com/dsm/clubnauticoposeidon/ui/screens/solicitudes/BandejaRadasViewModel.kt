package com.dsm.clubnauticoposeidon.ui.screens.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.RadaRepository
import com.dsm.clubnauticoposeidon.data.repository.SolicitudRadaRepository
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BandejaRadasUiState(
    val solicitudes: List<SolicitudRada> = emptyList(),
    val radas: List<Rada> = emptyList(),
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val completado: Boolean = false,
    val mensaje: String? = null
)

class BandejaRadasViewModel(
    private val solicitudRepo: SolicitudRadaRepository = SolicitudRadaRepository(),
    private val radaRepo: RadaRepository = RadaRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(BandejaRadasUiState())
    val ui: StateFlow<BandejaRadasUiState> = _ui

    init {
        viewModelScope.launch {
            solicitudRepo.observarPendientes()
                .catch { e -> _ui.update { it.copy(cargando = false, mensaje = "Error: ${e.message}") } }
                .collect { lista ->
                    _ui.update { it.copy(solicitudes = lista.sortedBy { s -> s.fechaSolicitud }, cargando = false) }
                }
        }
        viewModelScope.launch {
            radaRepo.observarRadas()
                .catch { e -> _ui.update { it.copy(mensaje = "Error: ${e.message}") } }
                .collect { lista -> _ui.update { it.copy(radas = lista.sortedBy { r -> r.codigo }) } }
        }
    }

    // Rada sugerida
    fun sugerirRada(eslora: Double): Rada? =
        _ui.value.radas
            .filter { it.estado == EstadoRada.DISPONIBLE && it.esloraMax >= eslora }
            .minByOrNull { it.esloraMax - eslora }

    fun aprobar(solicitud: SolicitudRada, radaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(procesando = true) }
            try {
                solicitudRepo.aprobar(solicitud, radaId)
                _ui.update { it.copy(procesando = false, completado = true, mensaje = "Rada $radaId asignada") }
            } catch (e: Exception) {
                _ui.update { it.copy(procesando = false, mensaje = e.message ?: "No se pudo aprobar") }
            }
        }
    }

    fun rechazar(solicitudId: String, motivo: String) {
        viewModelScope.launch {
            _ui.update { it.copy(procesando = true) }
            try {
                solicitudRepo.rechazar(solicitudId, motivo.trim())
                _ui.update { it.copy(procesando = false, completado = true, mensaje = "Solicitud rechazada") }
            } catch (e: Exception) {
                _ui.update { it.copy(procesando = false, mensaje = e.message ?: "No se pudo rechazar") }
            }
        }
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }
}