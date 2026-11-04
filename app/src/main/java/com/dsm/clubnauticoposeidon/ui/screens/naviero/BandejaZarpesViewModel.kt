package com.dsm.clubnauticoposeidon.ui.screens.naviero

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.ui.screens.zarpe.SolicitudZarpe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class BandejaZarpesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<BandejaZarpesState>(BandejaZarpesState.Cargando)
    val uiState: StateFlow<BandejaZarpesState> = _uiState

    private var listaZarpes: List<SolicitudZarpe> = emptyList()
    private var listaTripulantes: List<Tripulante> = emptyList()

    init {
        escucharSolicitudesYTripulacion()
    }

    private fun escucharSolicitudesYTripulacion() {
        // Escuchar solicitudes de zarpe pendientes
        db.collection("zarpes")
            .whereEqualTo("estado", "pendiente_aprobacion")
            .addSnapshotListener { snapshotZarpes, errorZarpes ->
                if (errorZarpes != null) {
                    Log.e("BandejaZarpesVM", "Error al escuchar zarpes", errorZarpes)
                    listaZarpes = emptyList()
                    emitirEstadoActual()
                    return@addSnapshotListener
                }

                if (snapshotZarpes != null) {
                    listaZarpes = snapshotZarpes.documents.map { doc ->
                        doc.toObject(SolicitudZarpe::class.java)?.copy(id = doc.id) ?: SolicitudZarpe(id = doc.id)
                    }
                    emitirEstadoActual()
                }
            }

        // Escuchar padrón de tripulación especializada
        db.collection("tripulantes").addSnapshotListener { snapshotTrip, errorTrip ->
            if (errorTrip != null) {
                Log.e("BandejaZarpesVM", "Error al escuchar tripulantes", errorTrip)
                listaTripulantes = emptyList()
                emitirEstadoActual()
                return@addSnapshotListener
            }

            if (snapshotTrip != null) {
                listaTripulantes = snapshotTrip.documents.map { doc ->
                    Tripulante(
                        id = doc.id,
                        nombreCompleto = doc.getString("nombreCompleto") ?: "",
                        dni = doc.getString("dni") ?: "",
                        numeroLicencia = doc.getString("numeroLicencia") ?: "",
                        cargo = doc.getString("cargo") ?: "",
                        estado = doc.getString("estado") ?: "autorizado"
                    )
                }
                emitirEstadoActual()
            }
        }
    }

    private fun emitirEstadoActual() {
        _uiState.value = BandejaZarpesState.Exito(
            solicitudesPendientes = listaZarpes.toList(),
            tripulantesAutorizados = listaTripulantes.toList()
        )
    }

    fun aprobarZarpe(zarpeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("zarpes").document(zarpeId)
                    .update("estado", "aprobado").await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al aprobar zarpe")
            }
        }
    }

    fun rechazarZarpe(zarpeId: String, motivo: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("zarpes").document(zarpeId)
                    .update(
                        mapOf(
                            "estado" to "rechazado",
                            "motivoRechazo" to motivo
                        )
                    ).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al rechazar zarpe")
            }
        }
    }

    fun registrarTripulante(
        nombre: String,
        dni: String,
        licencia: String,
        cargo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val nuevoTripulante = hashMapOf(
                    "nombreCompleto" to nombre,
                    "dni" to dni,
                    "numeroLicencia" to licencia,
                    "cargo" to cargo,
                    "estado" to "autorizado"
                )
                db.collection("tripulantes").add(nuevoTripulante).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al registrar tripulante")
            }
        }
    }
}
