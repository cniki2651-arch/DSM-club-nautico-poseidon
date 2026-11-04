package com.dsm.clubnauticoposeidon.ui.screens.secretaria

data class PostulanteModel(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val embarcacion_registrada: Boolean = false
)
