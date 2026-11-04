package com.dsm.clubnauticoposeidon.ui.screens.jefatura

data class PostulanteAprobacion(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val clasificacion: String = "",
    val verificacionExterna: Map<String, String>? = null
)
