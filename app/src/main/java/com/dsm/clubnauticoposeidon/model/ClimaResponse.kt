package com.dsm.clubnauticoposeidon.model



data class ClimaResponse(
    val current: ClimaActual
)

data class ClimaActual(
    val temperature_2m: Double,
    val wind_speed_10m: Double
)