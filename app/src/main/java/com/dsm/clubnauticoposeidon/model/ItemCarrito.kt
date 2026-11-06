package com.dsm.clubnauticoposeidon.model

// Un producto dentro del pedido que el socio está armando
data class ItemCarrito(
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int = 1,
    val servicioId: String? = null // ID del catálogo (opcional)
)
