package com.dsm.clubnauticoposeidon.model

// Plato del menú tal como está en Firestore (colección "catalogo_servicios")
data class Plato(
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val modulo: String = "",
    val disponible: Boolean = true,
    // Link de la foto del plato. Si está vacío se muestra un ícono
    val imagen: String = ""
)