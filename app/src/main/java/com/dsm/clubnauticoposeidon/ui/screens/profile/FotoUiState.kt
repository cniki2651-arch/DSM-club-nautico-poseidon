package com.dsm.clubnauticoposeidon.ui.screens.profile

import android.graphics.Bitmap

data class FotoUiState(
    val foto: Bitmap? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null
)
