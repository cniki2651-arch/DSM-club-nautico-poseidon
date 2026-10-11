package com.dsm.clubnauticoposeidon.ui.screens.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// 1. El Estado (LoginState)
sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    object Success : LoginState
    data class Error(val message: String) : LoginState
}

// 2. El ViewModel (LoginViewModel)
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginState>(LoginState.Idle)
    val uiState: StateFlow<LoginState> = _uiState

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun resetState() {
        _uiState.value = LoginState.Idle
    }

    fun iniciarSesion(correo: String, password: String) {
        _uiState.value = LoginState.Loading

        auth.signInWithEmailAndPassword(correo, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = LoginState.Success
                } else {
                    _uiState.value = LoginState.Error(task.exception?.message ?: "Error al iniciar sesión")
                }
            }
    }

    fun handleBiometricLogin(onNoSession: () -> Unit) {
        if (auth.currentUser != null) {
            _uiState.value = LoginState.Success
        } else {
            onNoSession()
        }
    }
}
