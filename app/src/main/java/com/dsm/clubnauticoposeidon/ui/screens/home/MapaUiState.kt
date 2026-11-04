package com.dsm.clubnauticoposeidon.ui.screens.home

import com.dsm.clubnauticoposeidon.model.Rada

data class MapaUiState(
    val radas: List<Rada> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)
