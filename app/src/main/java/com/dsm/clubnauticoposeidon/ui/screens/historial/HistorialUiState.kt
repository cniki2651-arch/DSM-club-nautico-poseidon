package com.dsm.clubnauticoposeidon.ui.screens.historial

import com.dsm.clubnauticoposeidon.model.HistorialItem

data class HistorialUiState(
    val items: List<HistorialItem> = emptyList(),
    val filtro: String = "todas",   // todas | activa | finalizada
    val cargando: Boolean = true,
    val error: String? = null
) {
    val visibles: List<HistorialItem>
        get() = if (filtro == "todas") items else items.filter { it.estado == filtro }
}
