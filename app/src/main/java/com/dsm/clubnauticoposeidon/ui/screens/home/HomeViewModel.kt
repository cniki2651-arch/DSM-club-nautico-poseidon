package com.dsm.clubnauticoposeidon.ui.screens.home

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class EstadoSocio {
    CARGANDO,
    INVITADO,       // Aún no envió solicitud de inscripción
    PENDIENTE,      // Ya envió solicitud, esperando revisión/aprobación
    APROBADO        // Ya es socio activo del club
}

class HomeViewModel : ViewModel() {
    private var db: FirebaseFirestore = Firebase.firestore

    private val _estadoSocio = MutableStateFlow(EstadoSocio.CARGANDO)
    val estadoSocio: StateFlow<EstadoSocio> = _estadoSocio

    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> = _nombreUsuario

    init {
        verificarEstadoSocio()
    }

    private fun verificarEstadoSocio() {
        val uid = Firebase.auth.currentUser?.uid

        if (uid == null) {
            _estadoSocio.value = EstadoSocio.INVITADO
            return
        }

        _nombreUsuario.value = Firebase.auth.currentUser?.email ?: ""

        db.collection("socios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    _estadoSocio.value = EstadoSocio.INVITADO
                } else {
                    when (doc.getString("estado_socio")) {
                        "aprobado" -> _estadoSocio.value = EstadoSocio.APROBADO
                        "pendiente", "revisado_secretaria" -> _estadoSocio.value = EstadoSocio.PENDIENTE
                        else -> _estadoSocio.value = EstadoSocio.INVITADO
                    }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HOME", "Error al verificar estado de socio: ${e.message}")
                _estadoSocio.value = EstadoSocio.INVITADO
            }
    }
}