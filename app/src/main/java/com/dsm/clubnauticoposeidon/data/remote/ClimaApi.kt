package com.dsm.clubnauticoposeidon.data.remote



import com.dsm.clubnauticoposeidon.model.ClimaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApi {
    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "temperature_2m,wind_speed_10m"
    ): ClimaResponse
}