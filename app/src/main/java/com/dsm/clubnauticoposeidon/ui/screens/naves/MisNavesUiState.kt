package com.dsm.clubnauticoposeidon.ui.screens.naves

import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.SolicitudRada

data class MisNavesUiState(
    val naves: List<Embarcacion> = emptyList(),
    val solicitudes: List<SolicitudRada> = emptyList(),
    val dni: String? = null,
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val registrada: Boolean = false,
    val solicitudEnviada: Boolean = false
)
