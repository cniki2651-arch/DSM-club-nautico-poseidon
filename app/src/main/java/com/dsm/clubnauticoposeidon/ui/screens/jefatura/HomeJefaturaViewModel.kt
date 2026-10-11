package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeJefaturaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _nombreJefe = MutableStateFlow("Cargando...")
    val nombreJefe: StateFlow<String> = _nombreJefe

    private val _aprobacionesPendientes = MutableStateFlow(0)
    val aprobacionesPendientes: StateFlow<Int> = _aprobacionesPendientes

    init {
        cargarDatosJefe()
        escucharAprobacionesPendientes()
    }

    private fun cargarDatosJefe() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre")
                        _nombreJefe.value = if (!nombre.isNullOrBlank()) nombre else "Jefatura"
                    } else {
                        _nombreJefe.value = "Jefatura"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeJefaturaVM", "Error al obtener nombre del jefe", e)
                    _nombreJefe.value = "Jefatura"
                }
        } else {
            _nombreJefe.value = "Jefatura"
        }
    }

    private fun escucharAprobacionesPendientes() {
        db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "revisado_secretaria")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeJefaturaVM", "Error al obtener solicitudes pendientes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _aprobacionesPendientes.value = snapshot.size()
                }
            }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
