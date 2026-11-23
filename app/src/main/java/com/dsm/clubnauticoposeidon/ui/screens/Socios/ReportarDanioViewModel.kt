package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.EmbarcacionRepository
import com.dsm.clubnauticoposeidon.data.repository.ReporteDanioRepository
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.ReporteDanio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportarDanioUiState(
    val navesSocio: List<Embarcacion> = emptyList(),
    val naveSeleccionada: Embarcacion? = null,
    val descripcion: String = "",
    val cuandoOcurrio: String = "",
    val dniSocio: String = "",
    val enviando: Boolean = false,
    val cargandoNaves: Boolean = true,
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val reportesHistorial: List<ReporteDanio> = emptyList()
)

class ReportarDanioViewModel(
    private val embarcacionRepo: EmbarcacionRepository = EmbarcacionRepository(),
    private val reporteRepo: ReporteDanioRepository = ReporteDanioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportarDanioUiState())
    val uiState: StateFlow<ReportarDanioUiState> = _uiState.asStateFlow()

    init {
        cargarDatosSocio()
    }

    fun recargarDatos() {
        cargarDatosSocio()
    }

    private fun cargarDatosSocio() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoNaves = true) }
            try {
                val dni = try {
                    embarcacionRepo.obtenerDniActual().orEmpty()
                } catch (e: Exception) {
                    Log.e("REPORTAR_DANIO", "Error al obtener DNI actual", e)
                    ""
                }

                _uiState.update { it.copy(dniSocio = dni) }

                if (dni.isNotBlank()) {
                    // Observar naves registradas del socio de forma protegida
                    launch {
                        embarcacionRepo.observarNaves(dni)
                            .catch { e ->
                                Log.e("REPORTAR_DANIO", "Error al observar naves", e)
                                emit(emptyList())
                            }
                            .collect { naves ->
                                _uiState.update { current ->
                                    current.copy(
                                        navesSocio = naves,
                                        naveSeleccionada = current.naveSeleccionada ?: naves.firstOrNull(),
                                        cargandoNaves = false
                                    )
                                }
                            }
                    }

                    // Observar historial de reportes de este socio de forma protegida
                    launch {
                        reporteRepo.observarReportesSocio(dni)
                            .catch { e ->
                                Log.e("REPORTAR_DANIO", "Error al observar reportes", e)
                                emit(emptyList())
                            }
                            .collect { reportes ->
                                _uiState.update { it.copy(reportesHistorial = reportes) }
                            }
                    }
                } else {
                    _uiState.update { it.copy(cargandoNaves = false) }
                }
            } catch (e: Exception) {
                Log.e("REPORTAR_DANIO", "Error general en cargarDatosSocio", e)
                _uiState.update { it.copy(cargandoNaves = false) }
            }
        }
    }

    fun seleccionarNave(nave: Embarcacion) {
        _uiState.update { it.copy(naveSeleccionada = nave) }
    }

    fun onDescripcionChanged(desc: String) {
        _uiState.update { it.copy(descripcion = desc) }
    }

    fun onCuandoOcurrioChanged(momento: String) {
        _uiState.update { it.copy(cuandoOcurrio = momento) }
    }

    fun enviarReporte(onCompletado: () -> Unit = {}) {
        val state = _uiState.value
        val nave = state.naveSeleccionada

        if (nave == null) {
            _uiState.update { it.copy(mensajeError = "Debes seleccionar una de tus embarcaciones registradas.") }
            return
        }

        if (state.descripcion.trim().isBlank()) {
            _uiState.update { it.copy(mensajeError = "Por favor detalla qué ocurrió y el daño presentado.") }
            return
        }

        val detalleCompleto = buildString {
            if (state.cuandoOcurrio.isNotBlank()) {
                append("Cuándo ocurrió: ${state.cuandoOcurrio.trim()}\n\n")
            }
            append("Detalle del daño: ${state.descripcion.trim()}")
        }

        val nuevoReporte = ReporteDanio(
            embarcacionId = nave.id,
            nombreNave = nave.nombreNave,
            matricula = nave.matricula,
            descripcion = detalleCompleto,
            socioDni = state.dniSocio
        )

        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true, mensajeError = null) }
            try {
                reporteRepo.crearReporte(nuevoReporte)
                _uiState.update {
                    it.copy(
                        enviando = false,
                        descripcion = "",
                        cuandoOcurrio = "",
                        mensajeExito = "Reporte enviado al equipo de mantenimiento exitosamente."
                    )
                }
                onCompletado()
            } catch (e: Exception) {
                Log.e("REPORTAR_DANIO", "Error al enviar reporte", e)
                _uiState.update {
                    it.copy(
                        enviando = false,
                        mensajeError = "Error al enviar el reporte: ${e.localizedMessage ?: "Revisa tu conexión"}"
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }
}
