package com.dsm.clubnauticoposeidon.ui.screens.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel para gestionar la lógica de negocio de la pantalla de inicio de sesión (Login).
 * Sigue los principios de Clean Code y MVVM.
 */
class LoginViewModel : ViewModel() {

    /**
     * Valida si existe una sesión activa guardada en Firebase (auth.currentUser != null).
     * - Si existe, ejecuta [onNavigateHome] para omitir contraseña y entrar al dashboard.
     * - Si no existe, ejecuta [onNoSession] para notificar al usuario que debe iniciar sesión con correo por primera vez.
     */
    fun handleBiometricSuccess(
        auth: FirebaseAuth,
        onNavigateHome: () -> Unit,
        onNoSession: () -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            onNavigateHome()
        } else {
            onNoSession()
        }
    }
}
