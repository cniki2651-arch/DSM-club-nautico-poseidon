package com.dsm.clubnauticoposeidon.ui.screens.profile

// Modelo de datos del perfil del usuario
data class UserProfile(
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val tipoDocumento: String = "DNI",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = "socio",
    val estado: String = ""
) {
    val nombreCompleto: String
        get() = "$nombre $apellidos".trim()
}
