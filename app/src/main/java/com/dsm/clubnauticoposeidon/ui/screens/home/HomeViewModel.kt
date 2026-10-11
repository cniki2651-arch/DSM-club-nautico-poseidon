package com.dsm.clubnauticoposeidon.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _nombreSocio = MutableStateFlow("Cargando...")
    val nombreSocio: StateFlow<String> = _nombreSocio

    init {
        cargarDatosSocio()
    }

    private fun cargarDatosSocio() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre")
                        _nombreSocio.value = if (!nombre.isNullOrBlank()) nombre else "Socio"
                    } else {
                        _nombreSocio.value = "Socio"
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("HomeViewModel", "Error al cargar datos del socio", e)
                    _nombreSocio.value = "Socio"
                }
        } else {
            _nombreSocio.value = "Socio"
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
