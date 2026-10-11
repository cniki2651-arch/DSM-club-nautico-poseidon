package com.dsm.clubnauticoposeidon.ui.screens.radas


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.ClimaRepository
import com.dsm.clubnauticoposeidon.data.repository.RadaRepository
import com.dsm.clubnauticoposeidon.model.Asignacion
import com.dsm.clubnauticoposeidon.model.ClimaActual
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RadasUiState(
    val radas: List<Rada> = emptyList(),
    val embarcacion: Embarcacion? = null,
    val sugerida: Rada? = null,
    val clima: ClimaActual? = null,
    val vientoSeguro: Boolean? = null,   // null = clima no disponible
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val guardado: Boolean = false
)

class RadaViewModel(
    private val radaRepo: RadaRepository = RadaRepository(),
    private val climaRepo: ClimaRepository = ClimaRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(RadasUiState())
    val ui: StateFlow<RadasUiState> = _ui

    // Coordenadas de la marina (Callao). Cámbialas por las del club.
    private val latitud = -12.05
    private val longitud = -77.15

    init {
        observarRadas()
        cargarClima()
    }

    private fun observarRadas() {
        viewModelScope.launch {
            radaRepo.observarRadas()
                .catch { e -> _ui.update { it.copy(mensaje = "Error al cargar radas: ${e.message}") } }
                .collect { lista -> _ui.update { it.copy(radas = lista) } }
        }
    }

    private fun cargarClima() {
        viewModelScope.launch {
            climaRepo.obtenerClima(latitud, longitud)
                .onSuccess { clima ->
                    _ui.update {
                        it.copy(clima = clima, vientoSeguro = climaRepo.esSeguroAmarrar(clima))
                    }
                }
                .onFailure { _ui.update { it.copy(clima = null, vientoSeguro = null) } }
        }
    }

    // Busca la nave, valida que sea del socio y sugiere la mejor rada
    fun buscarNave(dni: String, matricula: String) {
        viewModelScope.launch {
            _ui.update { it.copy(cargando = true, embarcacion = null, sugerida = null) }
            try {
                val nave = radaRepo.obtenerEmbarcacion(matricula)
                when {
                    nave == null -> _ui.update {
                        it.copy(cargando = false, mensaje = "La embarcación no está registrada")
                    }
                    nave.dniSocio != dni -> _ui.update {
                        it.copy(cargando = false, mensaje = "La embarcación no pertenece a ese DNI")
                    }
                    else -> {
                        val mejor = _ui.value.radas
                            .filter { it.estado == EstadoRada.DISPONIBLE && it.esloraMax >= nave.eslora }
                            .minByOrNull { it.esloraMax - nave.eslora }
                        _ui.update {
                            it.copy(
                                cargando = false,
                                embarcacion = nave,
                                sugerida = mejor,
                                mensaje = if (mejor == null) "No hay radas disponibles para esa eslora" else null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(cargando = false, mensaje = "Error: ${e.message}") }
            }
        }
    }

    fun confirmar(dni: String, radaId: String, fechaIngreso: String, fechaSalida: String) {
        val estado = _ui.value
        val nave = estado.embarcacion
        val rada = estado.radas.firstOrNull { it.id == radaId }

        val error = when {
            nave == null -> "Primero busca la embarcación"
            rada == null -> "La rada seleccionada no existe"
            rada.estado != EstadoRada.DISPONIBLE -> "La rada no está disponible"
            rada.esloraMax < nave.eslora -> "La embarcación no cabe en esa rada"
            estado.vientoSeguro == false -> "Viento fuerte: no es seguro amarrar ahora"
            else -> null
        }
        if (error != null) {
            _ui.update { it.copy(mensaje = error) }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(cargando = true) }
            try {
                radaRepo.guardarAsignacion(
                    Asignacion(
                        matricula = nave!!.matricula,
                        dniSocio = dni,
                        radaAsignada = radaId,
                        fechaIngreso = fechaIngreso,
                        fechaSalida = fechaSalida
                    )
                )
                _ui.update { it.copy(cargando = false, guardado = true, mensaje = "Asignación confirmada") }
            } catch (e: Exception) {
                _ui.update { it.copy(cargando = false, mensaje = e.message ?: "No se pudo guardar") }
            }
        }
    }

    fun mensajeMostrado() {
        _ui.update { it.copy(mensaje = null) }
    }
}