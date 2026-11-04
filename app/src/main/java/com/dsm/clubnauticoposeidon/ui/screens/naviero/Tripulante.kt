package com.dsm.clubnauticoposeidon.ui.screens.naviero

data class Tripulante(
    val id: String = "",
    val nombreCompleto: String = "",
    val dni: String = "",
    val numeroLicencia: String = "",
    val cargo: String = "", // "Capitán", "Ingeniero de Máquinas", "Marinero", etc.
    val estado: String = "autorizado" // "autorizado", "suspendido"
)