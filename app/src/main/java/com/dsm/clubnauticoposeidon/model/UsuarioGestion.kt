package com.dsm.clubnauticoposeidon.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Modelo de datos para la gestión administrativa de usuarios en Firestore.
 */
data class UsuarioGestion(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val dni: String = "",
    val telefono: String = "",
    val rol: String = "socio",
    val estado: String = "activo",
    @get:PropertyName("fechaRegistro") @set:PropertyName("fechaRegistro")
    var fechaRegistro: Long = System.currentTimeMillis()
) {
    val nombreCompleto: String
        get() = if (nombre.isBlank() && apellidos.isBlank()) "Usuario sin nombre" else "$nombre $apellidos".trim()

    val rolEnum: RolUsuario
        get() = RolUsuario.fromKey(rol)
}
