package com.dsm.clubnauticoposeidon.ui.screens.zarpe

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ZarpeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<ZarpeState>(ZarpeState.Idle)
    val uiState: StateFlow<ZarpeState> = _uiState

    init {
        verificarZarpeExistente()
    }

    fun resetState() {
        _uiState.value = ZarpeState.Idle
    }

    private fun verificarZarpeExistente() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val query = db.collection("zarpes")
                    .whereEqualTo("socioId", uid)
                    .whereIn("estado", listOf("pendiente_aprobacion", "aprobado", "activo"))
                    .limit(1)
                    .get()
                    .await()

                val doc = query.documents.firstOrNull()
                if (doc != null) {
                    val zarpe = doc.toObject(SolicitudZarpe::class.java)
                    if (zarpe != null) {
                        when (zarpe.estado) {
                            "activo" -> _uiState.value = ZarpeState.ZarpeActivo(zarpe.id, zarpe.destino)
                            "aprobado" -> _uiState.value = ZarpeState.AprobadoListoParaZarpar(zarpe)
                            "pendiente_aprobacion" -> _uiState.value = ZarpeState.PendienteAprobacion(zarpe)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al verificar zarpes existentes", e)
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
        observacionesTripulacion: String,
        tripulacionEspecializada: List<String>
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = ZarpeState.Error("No hay sesión activa")
            return
        }

        _uiState.value = ZarpeState.Cargando

        viewModelScope.launch {
            try {
                // 1. Verificar estado de deuda y clasificación del socio
                val userDoc = db.collection("usuarios").document(uid).get().await()
                if (userDoc.exists()) {
                    val estadoDeuda = userDoc.getString("estado_deuda")
                    val clasificacion = userDoc.getString("clasificacion")

                    if (estadoDeuda == "moroso" || clasificacion == "socio renuente a pago") {
                        _uiState.value = ZarpeState.MorosoBloqueado
                        return@launch
                    }
                }

                val dni = userDoc.getString("dni") ?: ""
                val zarpeRef = db.collection("zarpes").document()

                val fechaHoraSalida = "$fechaPartida - $horaPartida"
                val fechaHoraRegreso = "$fechaRetorno - $horaRetorno"

                val nuevoZarpe = SolicitudZarpe(
                    id = zarpeRef.id,
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
                    observacionesTripulacion = observacionesTripulacion,
                    tripulacionEspecializada = tripulacionEspecializada,
                    fechaSolicitud = System.currentTimeMillis(),
                    estado = "pendiente_aprobacion"
                )

                zarpeRef.set(nuevoZarpe).await()
                _uiState.value = ZarpeState.PendienteAprobacion(nuevoZarpe)

            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al enviar declaración náutica", e)
                _uiState.value = ZarpeState.Error(e.message ?: "Error al enviar la solicitud")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciarTransmisionGps(
        zarpeId: String,
        context: Context
    ) {
        _uiState.value = ZarpeState.Cargando
        viewModelScope.launch {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val location = try {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: Exception) {
                    null
                }

                val lat = location?.latitude ?: -12.046374
                val lng = location?.longitude ?: -77.142786

                db.collection("zarpes").document(zarpeId)
                    .update(
                        mapOf(
                            "estado" to "activo",
                            "latitud" to lat,
                            "longitud" to lng,
                            "ultimaActualizacion" to System.currentTimeMillis()
                        )
                    ).await()

                val doc = db.collection("zarpes").document(zarpeId).get().await()
                val destino = doc.getString("destino") ?: "Mar Abierto"
                _uiState.value = ZarpeState.ZarpeActivo(zarpeId, destino)

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
                db.collection("zarpes").document(zarpeId)
                    .update(
                        mapOf(
                            "estado" to "finalizado",
                            "fechaRetorno" to System.currentTimeMillis()
                        )
                    ).await()

                _uiState.value = ZarpeState.Idle
            } catch (e: Exception) {
                Log.e("ZarpeVM", "Error al finalizar zarpe", e)
                _uiState.value = ZarpeState.Error(e.message ?: "Error al finalizar el zarpe")
            }
        }
    }
}
