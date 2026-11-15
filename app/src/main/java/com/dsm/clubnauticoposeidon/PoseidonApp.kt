package com.dsm.clubnauticoposeidon

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class PoseidonApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase
        Firebase.initialize(this)

        // Inicializar App Check con Play Integrity (producción)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
}
