package com.dsm.clubnauticoposeidon.ui.screens.zarpe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.ZarpeRepository
import com.dsm.clubnauticoposeidon.data.service.LocationService
import com.dsm.clubnauticoposeidon.ui.screens.naviero.Tripulante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ZarpeViewModel(
    private val zarpeRepository: ZarpeRepository = ZarpeRepository(),
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ZarpeState>(ZarpeState.Cargando)
    val uiState: StateFlow<ZarpeState> = _uiState

    private val _tripulantesAutorizados = MutableStateFlow<List<Tripulante>>(emptyList())
    val tripulantesAutorizados: StateFlow<List<Tripulante>> = _tripulantesAutorizados

    init {
        escucharTripulantesAutorizados()
    }

    fun resetState() {
        _uiState.value = ZarpeState.Idle
    }

    private fun escucharTripulantesAutorizados() {
        viewModelScope.launch {
            zarpeRepository.observarTripulantesAutorizados()
                .catch { e -> Log.e("ZarpeVM", "Error al observar tripulantes autorizados", e) }
                .collect { lista ->
                    _tripulantesAutorizados.value = lista
                }
        }
    }

    fun verificarZarpeParaEmbarcacion(embarcacionId: String) {
        if (embarcacionId.isBlank()) {
            _uiState.value = ZarpeState.Idle
            return
        }
        viewModelScope.launch {
            try {
                val zarpe = zarpeRepository.verificarZarpeActivo(embarcacionId)
                if (zarpe != null) {
                    val lat = zarpe.latitud
                    val lng = zarpe.longitud
                    when (zarpe.estado) {
                        "activo", "en_mar" -> _uiState.value = ZarpeState.ZarpeActivo(zarpe.id, zarpe.destino, lat, lng)
                        "aprobado" -> _uiState.value = ZarpeState.AprobadoListoParaZarpar(zarpe)
                        "pendiente_aprobacion" -> _uiState.value = ZarpeState.PendienteAprobacion(zarpe)
                        "rechazado" -> _uiState.value = ZarpeState.Rechazado(
                            zarpe.motivoRechazo.ifBlank { "Solicitud de zarpe rechazada por Capitanía o Servicios Navieros." }
                        )
                        else -> _uiState.value = ZarpeState.Idle
                    }
                } else {
                    _uiState.value = ZarpeState.Idle
                }
            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al verificar zarpe para embarcacion $embarcacionId", e)
                _uiState.value = ZarpeState.Idle
            }
        }
    }

    fun solicitarZarpe(
        embarcacionId: String,
        embarcacionNombre: String,
        destino: String,
        fechaPartida: String,
        horaPartida: String,
        fechaRetorno: String,
        horaRetorno: String,
        cantidadTripulantes: Int,
        manifiestoPasajeros: String,
        observacionesTripulacion: String,
        tripulacionEspecializada: List<String>
    ) {
        val uid = zarpeRepository.currentUserId
        if (uid == null) {
            _uiState.value = ZarpeState.Error("No hay sesión activa")
            return
        }

        _uiState.value = ZarpeState.Cargando

        viewModelScope.launch {
            try {
                // 1. Verificar estado de deuda y clasificación del socio
                val userDoc = zarpeRepository.obtenerUsuarioActual(uid)
                if (userDoc.exists()) {
                    val estadoDeuda = userDoc.getString("estado_deuda")
                    val clasificacion = userDoc.getString("clasificacion")

                    if (estadoDeuda == "moroso" || clasificacion == "socio renuente a pago") {
                        _uiState.value = ZarpeState.MorosoBloqueado
                        return@launch
                    }
                }

                val dni = userDoc.getString("dni") ?: ""
                val fechaHoraSalida = "$fechaPartida - $horaPartida"
                val fechaHoraRegreso = "$fechaRetorno - $horaRetorno"

                val nuevoZarpe = SolicitudZarpe(
                    id = "",
                    embarcacionId = embarcacionId,
                    embarcacionNombre = embarcacionNombre,
                    socioId = uid,
                    dni = dni,
                    destino = destino,
                    fechaPartida = fechaPartida,
                    horaPartida = horaPartida,
                    fechaRetorno = fechaRetorno,
                    horaRetorno = horaRetorno,
                    fechaHoraSalida = fechaHoraSalida,
                    fechaHoraRegreso = fechaHoraRegreso,
                    cantidadTripulantes = cantidadTripulantes,
                    manifiestoPasajeros = manifiestoPasajeros,
                    observacionesTripulacion = observacionesTripulacion,
                    tripulacionEspecializada = tripulacionEspecializada,
                    fechaSolicitud = System.currentTimeMillis(),
                    estado = "pendiente_aprobacion"
                )

                zarpeRepository.solicitarZarpe(nuevoZarpe)
                _uiState.value = ZarpeState.PendienteAprobacion(nuevoZarpe)

            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al enviar declaración náutica", e)
                _uiState.value = ZarpeState.Error(e.message ?: "Error al enviar la solicitud")
            }
        }
    }

    /**
     * Inicia la navegación marítima y registra las coordenadas iniciales de GPS en Firestore.
     *
     * Nota de Arquitectura / Documentación Técnica:
     * La llamada actual a `locationService.obtenerCoordenadasActuales()` realiza una captura única de la
     * ubicación GPS al momento de arrancar la navegación. La transmisión continua de telemetría en bucle
     * en segundo plano mediante un Foreground Service / WorkManager queda programada como una mejora
     * técnica para la siguiente fase del proyecto.
     */
    fun iniciarTransmisionGps(zarpeId: String, destino: String) {
        _uiState.value = ZarpeState.Cargando
        viewModelScope.launch {
            try {
                val (lat, lng) = locationService.obtenerCoordenadasActuales()
                zarpeRepository.actualizarZarpeEnMar(zarpeId, lat, lng)

                _uiState.value = ZarpeState.ZarpeActivo(
                    zarpeId = zarpeId,
                    destino = destino,
                    latitud = lat,
                    longitud = lng
                )

            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al iniciar transmisión GPS", e)
                _uiState.value = ZarpeState.Error(e.message ?: "Error al activar el GPS")
            }
        }
    }

    fun finalizarZarpe(zarpeId: String) {
        _uiState.value = ZarpeState.Cargando
        viewModelScope.launch {
            try {
                zarpeRepository.finalizarZarpe(zarpeId)
                _uiState.value = ZarpeState.Idle
            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al finalizar zarpe", e)
                _uiState.value = ZarpeState.Error(e.message ?: "Error al finalizar el zarpe")
            }
        }
    }
}
