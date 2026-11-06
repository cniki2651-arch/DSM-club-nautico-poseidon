package com.dsm.clubnauticoposeidon.ui.components

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest


// Guarda las fotos en el celular para que se vean también sin internet (modo avión).
object ImagenesMenu {

    @Volatile
    private var cargador: ImageLoader? = null

    fun loader(context: Context): ImageLoader =
        cargador ?: synchronized(this) {
            cargador ?: ImageLoader.Builder(context.applicationContext)
                // Usa la foto guardada aunque el servidor diga "no guardar"
                .respectCacheHeaders(false)
                .crossfade(true)
                .build()
                .also { cargador = it }
        }


    fun precargar(context: Context, urls: List<String>) {
        val loader = loader(context)
        urls.filter { it.isNotBlank() }.distinct().forEach { url ->
            loader.enqueue(
                ImageRequest.Builder(context.applicationContext)
                    .data(url)
                    .build()
            )
        }
    }
}