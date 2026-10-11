package com.dsm.clubnauticoposeidon.ui.screens.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// 1. El Estado (SignUpState)
sealed interface SignUpState {
    object Idle : SignUpState
    object Loading : SignUpState
    object Success : SignUpState
    data class Error(val message: String) : SignUpState
}

// 2. El ViewModel (SignUpViewModel)
class SignUpViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val uiState: StateFlow<SignUpState> = _uiState

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun resetState() {
        _uiState.value = SignUpState.Idle
    }

    fun registrarUsuario(
        nombre: String,
        apellidos: String,
        tipoDocumento: String,
        dni: String,
        fechaNacimiento: String,
        telefono: String,
        correo: String,
        password: String,
        tieneEmbarcacion: Boolean,
        cantidadEmbarcaciones: Int,
        matricula: String,
        nombreEmbarcacion: String
    ) {
        _uiState.value = SignUpState.Loading

        // Paso 1: crear la cuenta en Authentication
        auth.createUserWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("AUTH", "Error al crear usuario", task.exception)
                    _uiState.value = SignUpState.Error("Error al registrar: ${task.exception?.message}")
                    return@addOnCompleteListener
                }

                val user = task.result?.user
                if (user == null) {
                    _uiState.value = SignUpState.Error("Error al crear el usuario en Authentication.")
                    return@addOnCompleteListener
                }

                // Paso 2: validar DNI único (ya con sesión iniciada)
                db.collection("usuarios").whereEqualTo("dni", dni).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            user.delete()
                            _uiState.value = SignUpState.Error("El DNI ya se encuentra registrado.")
                            return@addOnSuccessListener
                        }

                        // Paso 3: guardar en Firestore
                        val userData = hashMapOf<String, Any>(
                            "nombre" to nombre,
                            "apellidos" to apellidos,
                            "tipoDocumento" to tipoDocumento,
                            "dni" to dni,
                            "fechaNacimiento" to fechaNacimiento,
                            "telefono" to telefono,
                            "correo" to correo,
                            "rol" to "postulante",
                            "estado" to "pendiente",
                            "proveedor" to "password",
                            "fechaRegistro" to System.currentTimeMillis(),
                            "embarcacion_registrada" to (cantidadEmbarcaciones > 0),
                            "cantidad_embarcaciones" to cantidadEmbarcaciones,
                            "tieneEmbarcacion" to tieneEmbarcacion
                        )

                        if (tieneEmbarcacion) {
                            userData["matricula"] = matricula.trim().uppercase()
                            userData["nombreEmbarcacion"] = nombreEmbarcacion
                        }

                        db.collection("usuarios").document(user.uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("FIRESTORE", "Postulante guardado exitosamente en BD")
                                auth.signOut()
                                _uiState.value = SignUpState.Success
                            }
                            .addOnFailureListener { e ->
                                Log.e("FIRESTORE", "Error al guardar postulante", e)
                                user.delete()
                                _uiState.value = SignUpState.Error("Error al guardar los datos: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIRESTORE", "Error al validar DNI", e)
                        user.delete()
                        _uiState.value = SignUpState.Error("Error al validar la información: ${e.message}")
                    }
            }
    }
}