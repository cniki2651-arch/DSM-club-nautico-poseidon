package com.dsm.clubnauticoposeidon

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize

/**
 * Variante de Application para builds de debug / emulador.
 * Usa DebugAppCheckProviderFactory para generar tokens de prueba.
 *
 * Para usarlo en emulador:
 * 1. Ejecuta la app en modo debug y busca en Logcat el token:
 *    "DebugAppCheckProvider: Enter this debug secret into the..."
 * 2. Registra ese token en la consola de Firebase → App Check → Manage debug tokens.
 */
class PoseidonDebugApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        // App Check con proveedor de debug (emulador / CI)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        Log.d("PoseidonDebugApp", "Firebase App Check inicializado con DebugProvider")
    }
}
