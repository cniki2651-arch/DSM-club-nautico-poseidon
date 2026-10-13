package com.dsm.clubnauticoposeidon.ui.screens.clima

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.ClimaDetalle
import com.dsm.clubnauticoposeidon.ui.theme.Gold300
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Gold600
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy600
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ──────────────────────────────────────────────────────────────────
   HU05 · Pantalla de detalle de Clima y Marea para Navegación
   ────────────────────────────────────────────────────────────────── */

/**
 * Pantalla principal del módulo de Clima y Mareas (HU05).
 * Muestra el estado meteorológico actual y marino del Callao en vivo,
 * orientando al socio sobre las condiciones de navegación y seguridad para zarpar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaDetalleScreen(
    viewModel: ClimaViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val clima by viewModel.clima.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()

    // Animación de aparición escalonada
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(cargando) {
        if (!cargando) {
            delay(100)
            visible = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Clima y Marea",
                            color = Gold500,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Callao, Lima – Perú",
                            color = Muted,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Gold500
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        visible = false
                        viewModel.cargarClima()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = Gold500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900
                )
            )
        },
        containerColor = Navy900
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Gold500)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Consultando clima del Callao…",
                                color = Muted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No se pudo cargar el clima",
                                color = Ink,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error ?: "",
                                color = Muted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            TextButton(onClick = {
                                visible = false
                                viewModel.cargarClima()
                            }) {
                                Text("Reintentar", color = Gold500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                clima != null -> {
                    ContenidoClima(
                        clima = clima!!,
                        visible = visible
                    )
                }
            }
        }
    }
}

// ── Contenido principal (datos cargados) ──

@Composable
private fun ContenidoClima(
    clima: ClimaDetalle,
    visible: Boolean
) {
    val fechaHoy = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", localeEs)
        LocalDate.now().format(formatter)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ── Sección Hero: Temperatura grande ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -40 }
        ) {
            HeroTemperatura(clima, fechaHoy)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Indicador de seguridad para zarpe ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 100)) + slideInVertically(tween(600, delayMillis = 100)) { -30 }
        ) {
            IndicadorSeguridadZarpe(clima)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Sección: Condiciones Atmosféricas ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { -30 }
        ) {
            SeccionAtmosferica(clima)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sección: Viento ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300)) { -30 }
        ) {
            SeccionViento(clima)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sección: Estado del Mar ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 400)) + slideInVertically(tween(600, delayMillis = 400)) { -30 }
        ) {
            SeccionMar(clima)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Sección: Sol y UV ──
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 500)) + slideInVertically(tween(600, delayMillis = 500)) { -30 }
        ) {
            SeccionSolYUV(clima)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ────────────────────────────────────────────────────────────────
// HERO: Temperatura principal
// ────────────────────────────────────────────────────────────────

@Composable
private fun HeroTemperatura(clima: ClimaDetalle, fecha: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Navy700.copy(alpha = 0.7f),
                            Navy800.copy(alpha = 0.9f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono del clima
                Text(
                    text = obtenerEmojiClima(clima.codigoClima),
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperatura principal
                Text(
                    text = "${clima.temperatura.toInt()}°",
                    color = Ink,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )

                // Descripción del clima
                Text(
                    text = obtenerDescripcionClima(clima.codigoClima),
                    color = Gold400,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sensación térmica y rango de temperatura
                Text(
                    text = "Sensación de ${clima.sensacionTermica.toInt()}°C",
                    color = Muted,
                    fontSize = 14.sp
                )

                if (clima.temperaturaMax != null && clima.temperaturaMin != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "▲ ${clima.temperaturaMax.toInt()}°  ▼ ${clima.temperaturaMin.toInt()}°",
                        color = Muted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = fecha,
                    color = Muted.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// INDICADOR DE SEGURIDAD PARA ZARPE
// ────────────────────────────────────────────────────────────────

/**
 * Evalúa las condiciones de viento y oleaje para clasificar el estado de zarpe
 * mediante un semáforo náutico (Bandera Verde: óptimo, Amarilla: precaución, Roja: no recomendado).
 */
@Composable
private fun IndicadorSeguridadZarpe(clima: ClimaDetalle) {
    val vientoSeguro = clima.velocidadViento < 30.0
    val olasSeguras = (clima.alturaOlas ?: 0.0) < 2.5
    val esSeguro = vientoSeguro && olasSeguras

    val colorBandera = when {
        esSeguro && clima.velocidadViento < 20.0 && (clima.alturaOlas ?: 0.0) < 1.5 ->
            Color(0xFF4CAF50) // Verde
        esSeguro ->
            Color(0xFFF9A825) // Amarillo
        else ->
            Color(0xFFE57373) // Rojo
    }

    val textoBandera = when {
        esSeguro && clima.velocidadViento < 20.0 && (clima.alturaOlas ?: 0.0) < 1.5 ->
            "Bandera Verde – Condiciones óptimas para zarpe"
        esSeguro ->
            "Bandera Amarilla – Zarpe con precaución"
        else ->
            "Bandera Roja – Zarpe no recomendado"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorBandera.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador circular de bandera
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colorBandera),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Bandera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = textoBandera,
                    color = colorBandera,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Viento: ${String.format("%.1f", clima.velocidadViento)} km/h · Olas: ${
                        String.format("%.1f", clima.alturaOlas ?: 0.0)
                    } m",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// SECCIÓN: Condiciones Atmosféricas
// ────────────────────────────────────────────────────────────────

@Composable
private fun SeccionAtmosferica(clima: ClimaDetalle) {
    SeccionTarjeta(titulo = "Condiciones Atmosféricas") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DatoClimaItem(
                icon = Icons.Default.WaterDrop,
                label = "Humedad",
                valor = "${clima.humedad}%",
                modifier = Modifier.weight(1f)
            )
            DatoClimaItem(
                icon = Icons.Default.Cloud,
                label = "Nubosidad",
                valor = "${clima.nubosidad}%",
                modifier = Modifier.weight(1f)
            )
            DatoClimaItem(
                icon = Icons.Default.Umbrella,
                label = "Precipitación",
                valor = "${String.format("%.1f", clima.precipitacion)} mm",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DatoClimaItem(
                icon = Icons.Default.Compress,
                label = "Presión",
                valor = "${clima.presionAtmosferica.toInt()} hPa",
                modifier = Modifier.weight(1f)
            )
            DatoClimaItem(
                icon = Icons.Default.Thermostat,
                label = "Sens. Térmica",
                valor = "${clima.sensacionTermica.toInt()}°C",
                modifier = Modifier.weight(1f)
            )
            // Espacio vacío para alinear
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ────────────────────────────────────────────────────────────────
// SECCIÓN: Viento
// ────────────────────────────────────────────────────────────────

@Composable
private fun SeccionViento(clima: ClimaDetalle) {
    SeccionTarjeta(titulo = "Viento") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            DatoClimaItem(
                icon = Icons.Default.Air,
                label = "Velocidad",
                valor = "${String.format("%.1f", clima.velocidadViento)} km/h",
                modifier = Modifier.weight(1f),
                iconTint = Color(0xFF81D4FA)
            )
            DatoDireccionItem(
                grados = clima.direccionViento,
                label = "Dirección",
                modifier = Modifier.weight(1f),
                iconTint = Gold400,
                subtexto = "Rumbo viento"
            )
            DatoClimaItem(
                icon = Icons.Default.Bolt,
                label = "Ráfagas",
                valor = "${String.format("%.1f", clima.rafagasViento)} km/h",
                modifier = Modifier.weight(1f),
                iconTint = Color(0xFFFFB74D)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Barra visual de intensidad de viento
        val progresoViento = (clima.velocidadViento / 60.0).coerceIn(0.0, 1.0).toFloat()
        val colorViento = when {
            clima.velocidadViento < 15 -> Color(0xFF4CAF50)
            clima.velocidadViento < 30 -> Color(0xFFF9A825)
            else -> Color(0xFFE57373)
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Intensidad", color = Muted, fontSize = 12.sp)
                Text(
                    text = when {
                        clima.velocidadViento < 12 -> "Brisa ligera"
                        clima.velocidadViento < 20 -> "Brisa moderada"
                        clima.velocidadViento < 30 -> "Viento fresco"
                        clima.velocidadViento < 40 -> "Viento fuerte"
                        else -> "Temporal"
                    },
                    color = colorViento,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Navy600.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progresoViento)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(colorViento.copy(alpha = 0.6f), colorViento)
                            )
                        )
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// SECCIÓN: Estado del Mar
// ────────────────────────────────────────────────────────────────

@Composable
private fun SeccionMar(clima: ClimaDetalle) {
    SeccionTarjeta(titulo = "Estado del Mar") {
        if (clima.alturaOlas != null) {
            val altura = clima.alturaOlas
            // Información interpretativa del estado del mar
            val infoEscala = when {
                altura < 0.1 -> InfoEscalaMar("Mar en calma", Color(0xFF4CAF50), "Superficie como un espejo, ideal para navegar")
                altura < 0.5 -> InfoEscalaMar("Mar rizado", Color(0xFF66BB6A), "Olas muy pequeñas y suaves sin rompientes")
                altura < 1.25 -> InfoEscalaMar("Marejadilla", Color(0xFF8BC34A), "Oleaje suave, óptimo para yates y veleros")
                altura < 2.5 -> InfoEscalaMar("Marejada", Color(0xFFF9A825), "Precaución: oleaje pronunciado en bahía")
                altura < 4.0 -> InfoEscalaMar("Fuerte marejada", Color(0xFFFF9800), "Riesgo: olas grandes y rompientes frecuentes")
                altura < 6.0 -> InfoEscalaMar("Mar gruesa", Color(0xFFE57373), "Peligroso: navegación restringida")
                else -> InfoEscalaMar("Mar muy gruesa", Color(0xFFD32F2F), "Condición extrema: alerta marítima")
            }

            // ── 1. Banner con Estado del Mar y Barra Visual de Nivel de Olas ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(infoEscala.color.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, infoEscala.color.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(infoEscala.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = infoEscala.nombre,
                            color = infoEscala.color,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = infoEscala.recomendacion,
                        color = Ink.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barra visual de nivel de ola (0 a 3.5 m)
                    val progresoOla = (altura / 3.5).coerceIn(0.0, 1.0).toFloat()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.0 m", color = Muted, fontSize = 10.sp)
                            Text(
                                "${String.format("%.2f", altura)} m actual",
                                color = infoEscala.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("3.5 m+", color = Muted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Navy600.copy(alpha = 0.6f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progresoOla)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(infoEscala.color.copy(alpha = 0.5f), infoEscala.color)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── 2. Métricas Principales (Altura, Rumbo y Periodo) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // A. Altura de Ola con ícono de agua
                DatoClimaItem(
                    icon = Icons.Default.Water,
                    label = "Altura Oleaje",
                    valor = "${String.format("%.2f", altura)} m",
                    modifier = Modifier.weight(1f),
                    iconTint = Color(0xFF29B6F6),
                    subtexto = "Significativa"
                )

                // B. Dirección con flecha brújula rotando dinámicamente según grados reales
                DatoDireccionItem(
                    grados = clima.direccionOlas ?: 0,
                    label = "Dir. Oleaje",
                    modifier = Modifier.weight(1f),
                    iconTint = Gold400,
                    subtexto = "Rumbo del mar"
                )

                // C. Periodo de Olas con cronómetro / cadencia
                val periodo = clima.periodoOlas ?: 0.0
                val descPeriodo = if (periodo >= 9.0) "Oleaje tendido" else "Oleaje picado"
                DatoClimaItem(
                    icon = Icons.Default.Timer,
                    label = "Periodo",
                    valor = "${String.format("%.1f", periodo)} s",
                    modifier = Modifier.weight(1f),
                    iconTint = Color(0xFF26A69A),
                    subtexto = descPeriodo
                )
            }
        } else {
            Text(
                text = "Datos marinos no disponibles en este momento",
                color = Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────
// SECCIÓN: Sol y UV
// ────────────────────────────────────────────────────────────────

@Composable
private fun SeccionSolYUV(clima: ClimaDetalle) {
    SeccionTarjeta(titulo = "Sol e Índice UV") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DatoClimaItem(
                icon = Icons.Default.WbSunny,
                label = "Amanecer",
                valor = clima.amanecer?.substringAfterLast("T") ?: "--:--",
                modifier = Modifier.weight(1f)
            )
            DatoClimaItem(
                icon = Icons.Default.WbTwilight,
                label = "Atardecer",
                valor = clima.atardecer?.substringAfterLast("T") ?: "--:--",
                modifier = Modifier.weight(1f)
            )

            val uv = clima.indiceUV ?: 0.0
            val colorUV = when {
                uv < 3 -> Color(0xFF4CAF50)
                uv < 6 -> Color(0xFFF9A825)
                uv < 8 -> Color(0xFFFF9800)
                uv < 11 -> Color(0xFFE57373)
                else -> Color(0xFFD32F2F)
            }
            val nivelUV = when {
                uv < 3 -> "Bajo"
                uv < 6 -> "Moderado"
                uv < 8 -> "Alto"
                uv < 11 -> "Muy alto"
                else -> "Extremo"
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "UV",
                    tint = colorUV,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.1f", uv),
                    color = colorUV,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "UV $nivelUV",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// COMPONENTES REUTILIZABLES
// ────────────────────────────────────────────────────────────────

/** Tarjeta con título y contenido interno */
@Composable
private fun SeccionTarjeta(
    titulo: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = titulo,
                color = Gold400,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

/** Ítem individual con ícono, etiqueta y valor */
@Composable
private fun DatoClimaItem(
    icon: ImageVector,
    label: String,
    valor: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Gold500,
    subtexto: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            color = Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            color = Muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        if (subtexto != null) {
            Text(
                text = subtexto,
                color = Muted.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Representa una dirección cardinal y angular mediante una brújula circular.
 * Anima la rotación de la flecha de navegación según los grados exactos recibidos de la API.
 */
@Composable
private fun DatoDireccionItem(
    grados: Int,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color = Gold400,
    subtexto: String? = null
) {
    val direccionCardinal = remember(grados) { obtenerDireccionCardinal(grados) }
    val anguloAnimado by animateFloatAsState(
        targetValue = grados.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "rotacion_direccion"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Navy700.copy(alpha = 0.7f))
                .border(BorderStroke(1.dp, iconTint.copy(alpha = 0.5f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = "$label: $direccionCardinal ($grados°)",
                tint = iconTint,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(anguloAnimado)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$direccionCardinal • $grados°",
            color = Ink,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            color = Muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        if (subtexto != null) {
            Text(
                text = subtexto,
                color = Muted.copy(alpha = 0.7f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Modelo auxiliar para la descripción del estado del mar */
private data class InfoEscalaMar(
    val nombre: String,
    val color: Color,
    val recomendacion: String
)

// ────────────────────────────────────────────────────────────────
// UTILIDADES
// ────────────────────────────────────────────────────────────────

/** Convierte grados en dirección cardinal */
private fun obtenerDireccionCardinal(grados: Int): String {
    val direcciones = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO")
    val indice = ((grados % 360 + 360) % 360 / 22.5 + 0.5).toInt() % 16
    return direcciones[indice]
}

/** Emoji representativo del código WMO de clima */
private fun obtenerEmojiClima(codigo: Int): String = when (codigo) {
    0 -> "☀️"          // Despejado
    1 -> "🌤️"          // Parcialmente despejado
    2 -> "⛅"           // Parcialmente nublado
    3 -> "☁️"           // Nublado
    45, 48 -> "🌫️"     // Neblina
    51, 53, 55 -> "🌦️" // Llovizna
    61, 63, 65 -> "🌧️" // Lluvia
    66, 67 -> "🌨️"     // Lluvia congelada
    71, 73, 75 -> "❄️" // Nieve
    77 -> "🌨️"         // Granizo
    80, 81, 82 -> "🌧️" // Chubascos
    85, 86 -> "🌨️"     // Chubascos de nieve
    95 -> "⛈️"          // Tormenta
    96, 99 -> "⛈️"      // Tormenta con granizo
    else -> "🌡️"
}

/** Descripción en español del código WMO de clima */
private fun obtenerDescripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1 -> "Parcialmente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45 -> "Neblina"
    48 -> "Neblina con escarcha"
    51 -> "Llovizna ligera"
    53 -> "Llovizna moderada"
    55 -> "Llovizna intensa"
    61 -> "Lluvia ligera"
    63 -> "Lluvia moderada"
    65 -> "Lluvia intensa"
    66 -> "Lluvia congelada ligera"
    67 -> "Lluvia congelada intensa"
    71 -> "Nevada ligera"
    73 -> "Nevada moderada"
    75 -> "Nevada intensa"
    77 -> "Granizo"
    80 -> "Chubascos ligeros"
    81 -> "Chubascos moderados"
    82 -> "Chubascos violentos"
    85 -> "Chubascos de nieve ligeros"
    86 -> "Chubascos de nieve intensos"
    95 -> "Tormenta eléctrica"
    96 -> "Tormenta con granizo ligero"
    99 -> "Tormenta con granizo fuerte"
    else -> "Clima variable"
}
