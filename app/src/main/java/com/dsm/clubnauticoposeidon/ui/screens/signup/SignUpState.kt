package com.dsm.clubnauticoposeidon.ui.screens.signup

sealed interface SignUpState {
    object Idle : SignUpState
    object Loading : SignUpState
    object Success : SignUpState
    data class Error(val message: String) : SignUpState
}
