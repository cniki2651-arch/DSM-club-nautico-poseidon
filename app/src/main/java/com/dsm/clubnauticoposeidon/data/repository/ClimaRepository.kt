package com.dsm.clubnauticoposeidon.data.repository


import com.dsm.clubnauticoposeidon.data.remote.ClimaApi
import com.dsm.clubnauticoposeidon.data.remote.RetrofitClient
import com.dsm.clubnauticoposeidon.model.ClimaActual

class ClimaRepository(
    private val api: ClimaApi = RetrofitClient.climaApi
) {

    companion object {
        // Viento máximo (km/h) para permitir amarrar con seguridad
        const val VIENTO_MAXIMO_KMH = 30.0
    }

    // Result evita que la app se cierre si falla el internet
    suspend fun obtenerClima(latitud: Double, longitud: Double): Result<ClimaActual> =
        runCatching { api.obtenerClima(latitud, longitud).current }

    fun esSeguroAmarrar(clima: ClimaActual): Boolean =
        clima.wind_speed_10m < VIENTO_MAXIMO_KMH
}