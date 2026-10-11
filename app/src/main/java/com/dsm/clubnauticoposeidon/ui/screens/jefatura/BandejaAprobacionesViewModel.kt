package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 1. Modelo de Datos
data class PostulanteAprobacion(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val clasificacion: String = "",
    val verificacionExterna: Map<String, String>? = null
)

// 2. ViewModel
class BandejaAprobacionesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _postulantes = MutableStateFlow<List<PostulanteAprobacion>>(emptyList())
    val postulantes: StateFlow<List<PostulanteAprobacion>> = _postulantes

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    init {
        escucharAprobacionesPendientes()
    }

    private fun escucharAprobacionesPendientes() {
        db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "revisado_secretaria")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BandejaAprobacionesVM", "Error al escuchar aprobaciones pendientes", error)
                    _cargando.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.documents.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        PostulanteAprobacion(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            apellidos = doc.getString("apellidos") ?: "",
                            dni = doc.getString("dni") ?: "",
                            correo = doc.getString("correo") ?: "",
                            clasificacion = doc.getString("clasificacion") ?: "",
                            verificacionExterna = doc.get("verificacion_externa") as? Map<String, String>
                        )
                    }
                    _postulantes.value = lista
                    _cargando.value = false
                }
            }
    }

    fun aprobarSocio(
        postulanteId: String,
        clasificacion: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Regla de Negocio: solo admite "socio pagador"
        if (clasificacion != "socio pagador") {
            onError("Error: La política del club solo admite socios clasificados como 'pagador'.")
            return
        }

        viewModelScope.launch {
            db.collection("usuarios").document(postulanteId)
                .update(
                    mapOf(
                        "estado" to "aprobado",
                        "rol" to "socio"
                    )
                )
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("BandejaAprobacionesVM", "Error al aprobar socio", e)
                    onError(e.message ?: "Error al aprobar el socio")
                }
        }
    }

    fun rechazarSocio(
        postulanteId: String,
        motivo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            db.collection("usuarios").document(postulanteId)
                .update(
                    mapOf(
                        "estado" to "rechazado",
                        "motivo_rechazo" to motivo
                    )
                )
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("BandejaAprobacionesVM", "Error al rechazar socio", e)
                    onError(e.message ?: "Error al rechazar solicitud")
                }
        }
    }
}
