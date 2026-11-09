package com.dsm.clubnauticoposeidon.ui.screens.zarpe

sealed interface ZarpeState {
    object Idle : ZarpeState
    object Cargando : ZarpeState
    object MorosoBloqueado : ZarpeState
    data class PendienteAprobacion(val zarpe: SolicitudZarpe) : ZarpeState
    data class AprobadoListoParaZarpar(val zarpe: SolicitudZarpe) : ZarpeState
    data class ZarpeActivo(
        val zarpeId: String,
        val destino: String,
        val latitud: Double = 0.0,
        val longitud: Double = 0.0
    ) : ZarpeState
    data class Error(val message: String) : ZarpeState
}
