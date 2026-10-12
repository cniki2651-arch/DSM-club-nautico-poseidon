package com.dsm.clubnauticoposeidon.model

import com.google.gson.annotations.SerializedName

// ── Respuesta de clima atmosférico (Open-Meteo Forecast API) ──

data class ClimaResponse(
    val current: ClimaActual,
    val daily: ClimaDiario? = null
)

data class ClimaActual(
    val temperature_2m: Double,
    val wind_speed_10m: Double,
    val relative_humidity_2m: Int? = null,
    val apparent_temperature: Double? = null,
    val precipitation: Double? = null,
    val weather_code: Int? = null,
    val cloud_cover: Int? = null,
    val wind_direction_10m: Int? = null,
    val wind_gusts_10m: Double? = null,
    val surface_pressure: Double? = null
)

data class ClimaDiario(
    val temperature_2m_max: List<Double>? = null,
    val temperature_2m_min: List<Double>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    val uv_index_max: List<Double>? = null
)

// ── Respuesta de datos marinos (Open-Meteo Marine API) ──

data class MarineResponse(
    val current: MarineActual
)

data class MarineActual(
    val wave_height: Double? = null,
    val wave_direction: Int? = null,
    val wave_period: Double? = null,
    val wind_wave_height: Double? = null,
    val swell_wave_height: Double? = null,
    val swell_wave_period: Double? = null
)

// ── Modelo unificado para la pantalla de detalle ──

data class ClimaDetalle(
    // Atmosférico
    val temperatura: Double,
    val sensacionTermica: Double,
    val humedad: Int,
    val precipitacion: Double,
    val codigoClima: Int,
    val nubosidad: Int,
    val velocidadViento: Double,
    val direccionViento: Int,
    val rafagasViento: Double,
    val presionAtmosferica: Double,
    // Diario
    val temperaturaMax: Double?,
    val temperaturaMin: Double?,
    val amanecer: String?,
    val atardecer: String?,
    val indiceUV: Double?,
    // Marino
    val alturaOlas: Double?,
    val direccionOlas: Int?,
    val periodoOlas: Double?,
    val alturaOlasViento: Double?,
    val alturaSwellOlas: Double?,
    val periodoSwellOlas: Double?
)