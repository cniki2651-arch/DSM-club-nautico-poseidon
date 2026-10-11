package com.dsm.clubnauticoposeidon.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 1. El Estado (LoginState)
sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val rol: String) : LoginState   // ahora lleva el rol
    data class Error(val message: String) : LoginState
}

// 2. El ViewModel (LoginViewModel)
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }

    fun iniciarSesion(correo: String, password: String) {
        _uiState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(correo, password).await()
                validarRolYContinuar()
            } catch (e: Exception) {
                _uiState.value = LoginState.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun handleBiometricLogin(onNoSession: () -> Unit) {
        if (auth.currentUser == null) {
            onNoSession()
            return
        }
        _uiState.value = LoginState.Loading
        viewModelScope.launch { validarRolYContinuar() }
    }

    // Lee usuarios/{uid}, bloquea postulantes y expone el rol
    private suspend fun validarRolYContinuar() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = LoginState.Error("No hay sesión activa")
            return
        }

        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val rol = doc.getString("rol")

            when {
                !doc.exists() || rol.isNullOrBlank() -> {
                    auth.signOut()
                    _uiState.value = LoginState.Error(
                        "Tu cuenta no tiene un perfil asignado. Contacta al club."
                    )
                }
                rol == ROL_POSTULANTE -> {
                    auth.signOut()
                    _uiState.value = LoginState.Error(
                        "Tu solicitud aún está en revisión. Podrás ingresar cuando el club la apruebe."
                    )
                }
                else -> _uiState.value = LoginState.Success(rol)
            }
        } catch (e: Exception) {
            auth.signOut()
            _uiState.value = LoginState.Error(
                "No se pudo verificar tu perfil: ${e.message}"
            )
        }
    }

    companion object {
        const val ROL_POSTULANTE = "postulante"
    }
}