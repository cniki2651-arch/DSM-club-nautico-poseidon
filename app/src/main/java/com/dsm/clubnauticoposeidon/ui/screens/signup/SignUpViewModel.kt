package com.dsm.clubnauticoposeidon.ui.screens.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

        viewModelScope.launch {
            // Paso 1: Consulta a Firestore para validar DNI único
            db.collection("usuarios").whereEqualTo("dni", dni).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        _uiState.value = SignUpState.Error("El DNI ya se encuentra registrado.")
                    } else {
                        // Paso 2: Crear cuenta en FirebaseAuth
                        auth.createUserWithEmailAndPassword(correo, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = task.result?.user
                                    if (user == null) {
                                        _uiState.value = SignUpState.Error("Error al crear el usuario en Authentication.")
                                        return@addOnCompleteListener
                                    }

                                    // Paso 3: Guardar en Firestore con db.batch()
                                    val batch = db.batch()
                                    val userRef = db.collection("usuarios").document(user.uid)

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
                                        "cantidad_embarcaciones" to cantidadEmbarcaciones
                                    )

                                    if (tieneEmbarcacion) {
                                        userData["tieneEmbarcacion"] = true
                                        userData["matricula"] = matricula
                                        userData["nombreEmbarcacion"] = nombreEmbarcacion
                                    } else {
                                        userData["tieneEmbarcacion"] = false
                                    }

                                    batch.set(userRef, userData)
                                    
                                    // Paso 4: Commit batch, signOut y Success
                                    batch.commit()
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
                                } else {
                                    Log.e("AUTH", "Error al crear usuario", task.exception)
                                    _uiState.value = SignUpState.Error("Error al registrar: ${task.exception?.message}")
                                }
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Error al validar DNI", e)
                    _uiState.value = SignUpState.Error("Error al validar la información: ${e.message}")
                }
        }
    }
}
