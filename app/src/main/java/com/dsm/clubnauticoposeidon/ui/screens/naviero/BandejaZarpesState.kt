package com.dsm.clubnauticoposeidon.ui.screens.naviero

import com.dsm.clubnauticoposeidon.ui.screens.zarpe.SolicitudZarpe

sealed interface BandejaZarpesState {
    object Cargando : BandejaZarpesState
    data class Exito(
        val solicitudesPendientes: List<SolicitudZarpe>,
        val tripulantesAutorizados: List<Tripulante>
    ) : BandejaZarpesState
    data class Error(val message: String) : BandejaZarpesState
}