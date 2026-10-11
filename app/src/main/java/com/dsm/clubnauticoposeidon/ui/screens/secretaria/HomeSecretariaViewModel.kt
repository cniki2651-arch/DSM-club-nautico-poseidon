package com.dsm.clubnauticoposeidon.ui.screens.secretaria

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeSecretariaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _nombreSecretaria = MutableStateFlow("Cargando...")
    val nombreSecretaria: StateFlow<String> = _nombreSecretaria

    private val _solicitudesPendientes = MutableStateFlow(0)
    val solicitudesPendientes: StateFlow<Int> = _solicitudesPendientes

    init {
        cargarDatosSecretaria()
        escucharSolicitudesPendientes()
    }

    private fun cargarDatosSecretaria() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre")
                        _nombreSecretaria.value = if (!nombre.isNullOrBlank()) nombre else "Secretaría"
                    } else {
                        _nombreSecretaria.value = "Secretaría"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeSecretariaVM", "Error al obtener nombre de secretaria", e)
                    _nombreSecretaria.value = "Secretaría"
                }
        } else {
            _nombreSecretaria.value = "Secretaría"
        }
    }

    private fun escucharSolicitudesPendientes() {
        db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeSecretariaVM", "Error al obtener solicitudes pendientes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _solicitudesPendientes.value = snapshot.size()
                }
            }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
