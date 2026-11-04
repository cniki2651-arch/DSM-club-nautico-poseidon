package com.dsm.clubnauticoposeidon.ui.screens.secretaria

data class Postulante(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val embarcacion_registrada: Boolean = false
)
