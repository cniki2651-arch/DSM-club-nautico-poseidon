package com.dsm.clubnauticoposeidon.ui.screens.solicitudes

import com.dsm.clubnauticoposeidon.model.Rada
import com.dsm.clubnauticoposeidon.model.SolicitudRada

data class BandejaRadasUiState(
    val solicitudes: List<SolicitudRada> = emptyList(),
    val radas: List<Rada> = emptyList(),
    val cargando: Boolean = true,
    val procesando: Boolean = false,
    val completado: Boolean = false,
    val mensaje: String? = null
)
