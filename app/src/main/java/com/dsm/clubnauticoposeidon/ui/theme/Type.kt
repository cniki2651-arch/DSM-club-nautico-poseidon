package com.dsm.clubnauticoposeidon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.R

// Tipografía general de Material 3
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

// Fuente y estilo personalizados para el título náutico
val CormorantFamily = FontFamily(
    Font(R.font.cormorantgaramond_semibold, FontWeight.SemiBold),
    Font(R.font.cormorantgaramond_bold, FontWeight.Bold)
)

val TituloNautico = TextStyle(
    fontFamily = CormorantFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 30.sp
)