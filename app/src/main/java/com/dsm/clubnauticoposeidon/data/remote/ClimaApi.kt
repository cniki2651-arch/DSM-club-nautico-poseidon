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

    @GET("v1/forecast")
    suspend fun obtenerClimaDetallado(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,cloud_cover,wind_speed_10m,wind_direction_10m,wind_gusts_10m,surface_pressure",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max",
        @Query("timezone") timezone: String = "America/Lima",
        @Query("forecast_days") forecastDays: Int = 1
    ): ClimaResponse
}