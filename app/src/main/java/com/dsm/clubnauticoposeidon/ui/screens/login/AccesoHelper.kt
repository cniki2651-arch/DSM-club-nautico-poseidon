package com.dsm.clubnauticoposeidon.ui.screens.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Pantallas a las que puede llegar una persona al iniciar sesión.
 */
enum class Destino {
    SOCIO,
    ADMIN,          // naviero
    SECRETARIA,
    JEFE,
    FINANZAS,
    MANTENIMIENTO,
    SEGUIMIENTO     // postulante: ve cómo va su solicitud
}

/**
 * Única fuente de verdad del ruteo por rol y estado.
 * La usan tanto el login con correo como el login con Google.
 * Los roles de staff van primero para que siempre ganen sobre "estado".
 */
fun resolverDestino(rol: String?, estado: String?): Destino? = when {
    rol == "jefe" -> Destino.JEFE
    rol == "naviero" -> Destino.ADMIN
    rol == "secretaria" -> Destino.SECRETARIA
    rol == "finanzas" -> Destino.FINANZAS
    rol == "mantenimiento" -> Destino.MANTENIMIENTO
    estado == "aprobado" || rol == "socio" -> Destino.SOCIO
    estado == "pendiente" || estado == "revisado_secretaria" || estado == "rechazado" -> Destino.SEGUIMIENTO
    else -> null
}

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
    onSecretaria: () -> Unit,
    onJefe: () -> Unit,
    onFinanzas: () -> Unit,
    onMantenimiento: () -> Unit,
    onSeguimiento: () -> Unit,
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

        auth.signInWithCredential(firebaseCred).await()

        validarAcceso(
            auth = auth,
            onSocio = onSocio,
            onAdmin = onAdmin,
            onSecretaria = onSecretaria,
            onJefe = onJefe,
            onFinanzas = onFinanzas,
            onMantenimiento = onMantenimiento,
            onSeguimiento = onSeguimiento,
            onError = onError
        )
    } catch (e: Exception) {
        onError(e.message ?: "Inicio de sesión cancelado")
    }
}

/**
 * Revisa en Firestore que el usuario tenga registro y envía a la pantalla correspondiente.
 */
suspend fun validarAcceso(
    auth: FirebaseAuth,
    onSocio: () -> Unit,
    onAdmin: () -> Unit,
    onSecretaria: () -> Unit,
    onJefe: () -> Unit,
    onFinanzas: () -> Unit,
    onMantenimiento: () -> Unit,
    onSeguimiento: () -> Unit,
    onError: (String) -> Unit
) {
    val user = auth.currentUser
    val correo = user?.email?.trim()?.lowercase()
    if (user == null || correo == null) {
        onError("No se pudo obtener tu correo")
        return
    }

    try {
        val result = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("correo", correo)
            .limit(1)
            .get()
            .await()

        if (result.isEmpty) {
            auth.signOut()
            onError("No tienes registro. Regístrate primero y espera la aprobación.")
            return
        }

        val doc = result.documents[0]
        val destino = resolverDestino(doc.getString("rol"), doc.getString("estado"))

        when (destino) {
            Destino.JEFE -> onJefe()
            Destino.ADMIN -> onAdmin()
            Destino.SECRETARIA -> onSecretaria()
            Destino.FINANZAS -> onFinanzas()
            Destino.MANTENIMIENTO -> onMantenimiento()
            Destino.SOCIO -> onSocio()
            Destino.SEGUIMIENTO -> onSeguimiento()
            null -> {
                auth.signOut()
                onError("Tu solicitud aún está en revisión. Te avisaremos cuando esté activa.")
            }
        }
    } catch (e: Exception) {
        auth.signOut()
        onError("No se pudo verificar tu cuenta")
    }
}