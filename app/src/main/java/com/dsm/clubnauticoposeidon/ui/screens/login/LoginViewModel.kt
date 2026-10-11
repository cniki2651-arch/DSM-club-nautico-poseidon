package com.dsm.clubnauticoposeidon.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val destino: Destino) : LoginState
    data class Error(val message: String) : LoginState
}

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
                resolverAcceso()
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
        viewModelScope.launch { resolverAcceso() }
    }

    // Lee usuarios/{uid} y decide el destino con la misma lógica que Google
    private suspend fun resolverAcceso() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.value = LoginState.Error("No hay sesión activa")
            return
        }
        try {
            val doc = db.collection("usuarios").document(uid).get().await()
            if (!doc.exists()) {
                auth.signOut()
                _uiState.value = LoginState.Error("No tienes registro. Regístrate primero y espera la aprobación.")
                return
            }
            val destino = resolverDestino(doc.getString("rol"), doc.getString("estado"))
            if (destino == null) {
                auth.signOut()
                _uiState.value = LoginState.Error("Tu solicitud aún está en revisión. Te avisaremos cuando esté activa.")
            } else {
                _uiState.value = LoginState.Success(destino)
            }
        } catch (e: Exception) {
            auth.signOut()
            _uiState.value = LoginState.Error("No se pudo verificar tu cuenta: ${e.message}")
        }
    }
}