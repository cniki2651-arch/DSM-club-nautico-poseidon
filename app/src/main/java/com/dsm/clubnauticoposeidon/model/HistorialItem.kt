package com.dsm.clubnauticoposeidon.model


data class HistorialItem(
    val id: String,
    val socio: String,
    val dni: String,
    val nave: String,
    val tipo: String,
    val eslora: Double?,
    val matricula: String,
    val rada: String,
    val fechaIngreso: String,
    val fechaSalida: String,
    val estado: String,
    val salidaPendiente: Boolean
)