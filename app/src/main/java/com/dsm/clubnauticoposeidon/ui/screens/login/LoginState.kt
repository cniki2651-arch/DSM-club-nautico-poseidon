package com.dsm.clubnauticoposeidon.ui.screens.login

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val destino: Destino) : LoginState
    data class Error(val message: String) : LoginState
}
