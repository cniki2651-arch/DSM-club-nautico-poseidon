package com.dsm.clubnauticoposeidon.ui.screens.secretaria

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 1. Modelo de Datos
data class Postulante(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val embarcacion_registrada: Boolean = false
)

// 2. ViewModel
class BandejaSecretariaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _postulantes = MutableStateFlow<List<Postulante>>(emptyList())
    val postulantes: StateFlow<List<Postulante>> = _postulantes

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    init {
        escucharSolicitudesPendientes()
    }

    private fun escucharSolicitudesPendientes() {
        db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BandejaSecretariaVM", "Error al escuchar solicitudes pendientes", error)
                    _cargando.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.documents.map { doc ->
                        Postulante(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            apellidos = doc.getString("apellidos") ?: "",
                            dni = doc.getString("dni") ?: "",
                            correo = doc.getString("correo") ?: "",
                            embarcacion_registrada = doc.getBoolean("embarcacion_registrada") ?: false
                        )
                    }
                    _postulantes.value = lista
                    _cargando.value = false
                }
            }
    }

    fun procesarVerificacion(
        postulanteId: String,
        club: String,
        contacto: String,
        historial: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Reglas de Negocio
        val clasificacionCalculada = when (historial) {
            "Puntual" -> "socio pagador"
            "Atrasos ocasionales" -> "socio pagador esporádico"
            "Deudor / Problemático" -> "socio renuente a pago"
            else -> "socio pagador"
        }

        viewModelScope.launch {
            db.collection("usuarios").document(postulanteId)
                .update(
                    mapOf(
                        "estado" to "revisado_secretaria",
                        "clasificacion" to clasificacionCalculada,
                        "verificacion_externa" to mapOf(
                            "club" to club,
                            "contacto" to contacto,
                            "historial" to historial
                        )
                    )
                )
                .addOnSuccessListener {
                    onSuccess(clasificacionCalculada)
                }
                .addOnFailureListener { e ->
                    Log.e("BandejaSecretariaVM", "Error al actualizar verificación", e)
                    onError(e.message ?: "Error al guardar la verificación")
                }
        }
    }
}
