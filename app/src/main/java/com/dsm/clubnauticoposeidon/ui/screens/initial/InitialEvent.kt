package com.dsm.clubnauticoposeidon.ui.screens.initial

import androidx.annotation.StringRes

enum class DestinoInicial {
    SOCIO,
    ADMIN,
    SECRETARIA,
    JEFE,
    SEGUIMIENTO
}

// Eventos de una sola vez (navegar o mostrar un mensaje)
sealed interface InitialEvent {
    data class Navegar(val destino: DestinoInicial) : InitialEvent
    data class MensajeRes(@StringRes val resId: Int) : InitialEvent
    data class Mensaje(val texto: String) : InitialEvent
}
