package com.dsm.clubnauticoposeidon.data.repository


import com.dsm.clubnauticoposeidon.data.remote.ClimaApi
import com.dsm.clubnauticoposeidon.data.remote.MarineApi
import com.dsm.clubnauticoposeidon.data.remote.RetrofitClient
import com.dsm.clubnauticoposeidon.model.ClimaActual
import com.dsm.clubnauticoposeidon.model.ClimaDetalle

class ClimaRepository(
    private val api: ClimaApi = RetrofitClient.climaApi,
    private val marineApi: MarineApi = RetrofitClient.marineApi
) {
    companion object {
        // Viento máximo (km/h) para permitir amarrar con seguridad
        const val VIENTO_MAXIMO_KMH = 30.0

        // Coordenadas del Callao
        const val CALLAO_LAT = -12.0667
        const val CALLAO_LON = -77.15
    }

    // Result evita que la app se cierre si falla el internet
    suspend fun obtenerClima(latitud: Double, longitud: Double): Result<ClimaActual> =
        runCatching { api.obtenerClima(latitud, longitud).current }

    fun esSeguroAmarrar(clima: ClimaActual): Boolean =
        clima.wind_speed_10m < VIENTO_MAXIMO_KMH

    /**
     * Obtiene el clima detallado (atmosférico + marino) para la pantalla
     * de detalle HU05. Combina datos de ambas APIs de Open-Meteo.
     */
    suspend fun obtenerClimaDetallado(
        latitud: Double = CALLAO_LAT,
        longitud: Double = CALLAO_LON
    ): Result<ClimaDetalle> = runCatching {

        val climaResp = api.obtenerClimaDetallado(latitud, longitud)
        val marineResp = try {
            marineApi.obtenerDatosMarinos(latitud, longitud)
        } catch (_: Exception) {
            null
        }

        val c = climaResp.current
        val d = climaResp.daily
        val m = marineResp?.current

        ClimaDetalle(
            temperatura = c.temperature_2m,
            sensacionTermica = c.apparent_temperature ?: c.temperature_2m,
            humedad = c.relative_humidity_2m ?: 0,
            precipitacion = c.precipitation ?: 0.0,
            codigoClima = c.weather_code ?: 0,
            nubosidad = c.cloud_cover ?: 0,
            velocidadViento = c.wind_speed_10m,
            direccionViento = c.wind_direction_10m ?: 0,
            rafagasViento = c.wind_gusts_10m ?: 0.0,
            presionAtmosferica = c.surface_pressure ?: 0.0,
            temperaturaMax = d?.temperature_2m_max?.firstOrNull(),
            temperaturaMin = d?.temperature_2m_min?.firstOrNull(),
            amanecer = d?.sunrise?.firstOrNull(),
            atardecer = d?.sunset?.firstOrNull(),
            indiceUV = d?.uv_index_max?.firstOrNull(),
            alturaOlas = m?.wave_height,
            direccionOlas = m?.wave_direction,
            periodoOlas = m?.wave_period,
            alturaOlasViento = m?.wind_wave_height,
            alturaSwellOlas = m?.swell_wave_height,
            periodoSwellOlas = m?.swell_wave_period
        )
    }
}