package com.dsm.clubnauticoposeidon.utils

import android.content.Context

// Guarda en el celular qué embarcación eligió el socio, para no perderla al retroceder
object PreferenciasSocio {

    private const val ARCHIVO = "preferencias_socio"

    fun embarcacionSeleccionada(context: Context, uid: String): String? =
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .getString("embarcacion_$uid", null)

    fun guardarEmbarcacion(context: Context, uid: String, embarcacionId: String) {
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
            .edit()
            .putString("embarcacion_$uid", embarcacionId)
            .apply()
    }
}