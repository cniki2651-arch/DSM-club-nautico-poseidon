package com.dsm.clubnauticoposeidon.model

// Plato del menú
data class Plato(
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val modulo: String = "",
    val disponible: Boolean = true,

    val imagen: String = ""
)