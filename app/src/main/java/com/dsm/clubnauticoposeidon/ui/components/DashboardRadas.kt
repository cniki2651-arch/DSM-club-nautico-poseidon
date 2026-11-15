package com.dsm.clubnauticoposeidon.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Muted

/** Dashboard plegable del Panel del Naviero: estado de las radas del muelle. */
@Composable
fun DashboardRadas(
    ocupadas: Int,
    disponibles: Int,
    mantenimiento: Int,
    modifier: Modifier = Modifier
) {
    // Las radas en mantenimiento no se pueden asignar, por eso no cuentan en la ocupación
    val enServicio = ocupadas + disponibles
    val porcentaje = if (enServicio == 0) 0 else ocupadas * 100 / enServicio

    TarjetaGraficoPlegable(
        titulo = "Estadísticas de radas",
        subtitulo = "Estado actual del muelle",
        modifier = modifier
    ) {
        DonaPedidos(
            datos = listOf(
                DatoGrafico("Ocupadas", ocupadas, Gold500),
                DatoGrafico("Disponibles", disponibles, Color(0xFF4CAF50)),
                DatoGrafico("Mantenimiento", mantenimiento, Muted)
            ),
            valorCentro = "$porcentaje%",
            etiquetaCentro = "ocupación"
        )
    }
}