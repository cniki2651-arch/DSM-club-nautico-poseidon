package com.dsm.clubnauticoposeidon.ui.screens.postulante

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SeguimientoViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _estadoActual = MutableStateFlow("cargando")
    val estadoActual: StateFlow<String> = _estadoActual

    private val _motivoRechazo = MutableStateFlow("")
    val motivoRechazo: StateFlow<String> = _motivoRechazo

    init {
        escucharEstadoPostulante()
    }

    private fun escucharEstadoPostulante() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _estadoActual.value = "error"
            return
        }

        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SeguimientoVM", "Error al escuchar estado del postulante", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    _estadoActual.value = snapshot.getString("estado") ?: "desconocido"
                    _motivoRechazo.value = snapshot.getString("motivo_rechazo") ?: ""
                }
            }
    }

    fun subsanarSolicitud(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("No se encontró la sesión del usuario")
            return
        }

        viewModelScope.launch {
            db.collection("usuarios").document(uid)
                .update(
                    mapOf(
                        "estado" to "pendiente",
                        "motivo_rechazo" to ""
                    )
                )
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("SeguimientoVM", "Error al subsanar solicitud", e)
                    onError(e.message ?: "Error al reenviar la solicitud")
                }
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
