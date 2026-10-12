package com.dsm.clubnauticoposeidon.data.remote

import com.dsm.clubnauticoposeidon.model.MarineResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MarineApi {
    /**
     * Consulta parámetros oceánicos y de oleaje (altura de olas, dirección y periodo)
     * para las coordenadas marítimas del Callao.
     */
    @GET("v1/marine")
    suspend fun obtenerDatosMarinos(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "wave_height,wave_direction,wave_period,wind_wave_height,swell_wave_height,swell_wave_period",
        @Query("timezone") timezone: String = "America/Lima"
    ): MarineResponse
}
