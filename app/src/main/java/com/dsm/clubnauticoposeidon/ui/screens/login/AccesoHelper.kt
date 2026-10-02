package com.dsm.clubnauticoposeidon.ui.screens.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Abre el selector de cuentas de Google, inicia sesión en Firebase
 * y luego valida que el usuario esté registrado y aprobado.
 */
suspend fun signInWithGoogle(
    context: Context,
    auth: FirebaseAuth,
    webClientId: String,
    onSocio: () -> Unit,
    onAdmin: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val option = GetSignInWithGoogleOption.Builder(webClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val result = CredentialManager.create(context).getCredential(context, request)
        val googleCred = GoogleIdTokenCredential.createFrom(result.credential.data)
        val firebaseCred = GoogleAuthProvider.getCredential(googleCred.idToken, null)

        auth.signInWithCredential(firebaseCred).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                validarAcceso(auth, onSocio, onAdmin, onError)
            } else {
                onError(task.exception?.message ?: "Error al iniciar sesión con Google")
            }
        }
    } catch (e: Exception) {
        onError(e.message ?: "Inicio de sesión cancelado")
    }
}

/**
 * Revisa en Firestore que el usuario tenga registro y esté aprobado.
 * Busca por correo (el que llenó en el formulario de registro).
 */
fun validarAcceso(
    auth: FirebaseAuth,
    onSocio: () -> Unit,
    onAdmin: () -> Unit,
    onError: (String) -> Unit
) {
    val user = auth.currentUser
    val correo = user?.email?.trim()?.lowercase()
    if (user == null || correo == null) {
        onError("No se pudo obtener tu correo")
        return
    }

    FirebaseFirestore.getInstance()
        .collection("usuarios")
        .whereEqualTo("correo", correo)
        .limit(1)
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                auth.signOut()
                onError("No tienes registro. Regístrate primero y espera la aprobación.")
                return@addOnSuccessListener
            }
            val doc = result.documents[0]
            when {
                doc.getString("rol") == "naviero" -> onAdmin()
                doc.getString("rol") == "socio" -> onSocio()
                else -> {
                    auth.signOut()
                    onError("Tu solicitud aún está en revisión. Te avisaremos cuando esté activa.")
                }
            }
        }
        .addOnFailureListener {
            auth.signOut()
            onError("No se pudo verificar tu cuenta")
        }
}
