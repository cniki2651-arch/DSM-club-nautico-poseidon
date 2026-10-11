package com.dsm.clubnauticoposeidon.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Modelo de datos del perfil del usuario
data class UserProfile(
    val nombreCompleto: String = "",
    val dni: String = "",
    val tipoDocumento: String = "DNI",
    val correo: String = "",
    val telefono: String = "",
    val estado: String = ""
)

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _perfil = MutableStateFlow<UserProfile?>(null)
    val perfil: StateFlow<UserProfile?> = _perfil

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        val user = auth.currentUser
        val correo = user?.email?.trim()?.lowercase()
        if (correo == null) {
            _error.value = "No hay una sesión activa"
            _cargando.value = false
            return
        }

        db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                if (doc == null) {
                    _error.value = "No se encontró tu registro de socio"
                } else {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    _perfil.value = UserProfile(
                        nombreCompleto = "$nombre $apellidos".trim(),
                        dni = doc.getString("dni") ?: "",
                        tipoDocumento = doc.getString("tipoDocumento") ?: "DNI",
                        correo = doc.getString("correo") ?: correo,
                        telefono = doc.getString("telefono") ?: "",
                        estado = doc.getString("estado") ?: ""
                    )
                }
                _cargando.value = false
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Error al cargar perfil", e)
                _error.value = "No se pudieron cargar tus datos"
                _cargando.value = false
            }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}
