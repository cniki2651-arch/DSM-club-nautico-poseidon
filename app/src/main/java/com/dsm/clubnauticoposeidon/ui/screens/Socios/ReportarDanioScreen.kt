package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.ReporteDanio
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarDanioScreen(
    onBackClick: () -> Unit,
    viewModel: ReportarDanioViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.mensajeExito, uiState.mensajeError) {
        uiState.mensajeExito?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
            tabSeleccionado = 1 // Cambiar automáticamente a la pestaña de historial
        }
        uiState.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reportar Daño o Avería",
                            color = Gold500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Asistencia técnica para socios",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Navy900,
                contentColor = Gold500
            ) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nuevo Reporte", fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = Gold500,
                    unselectedContentColor = Muted
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mis Reportes (${uiState.reportesHistorial.size})", fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = Gold500,
                    unselectedContentColor = Muted
                )
            }

            if (tabSeleccionado == 0) {
                // FORMULARIO SEGURO Y ROBUSTO DE NUEVO REPORTE
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Navy700)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Gold500.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Gold500, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Asistencia Técnica en Muelle / Dique",
                                        color = Ink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tu reporte alertará al operario de mantenimiento, quien evaluará la gravedad con visión artificial y bitácora técnica.",
                                        color = Muted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // 1. Selector de Embarcación Registrada
                    item {
                        Text(
                            text = "1. SELECCIONA TU EMBARCACIÓN REGISTRADA",
                            color = Gold500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.cargandoNaves) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Gold500, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                            }
                        } else if (uiState.navesSocio.isEmpty()) {
                            Surface(
                                color = Navy800,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFA000).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "No tienes embarcaciones registradas",
                                        color = Color(0xFFFFA000),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Para reportar una avería, primero registra tu embarcación en la sección 'Mis Naves'.",
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                uiState.navesSocio.forEach { nave ->
                                    val isSelected = uiState.naveSeleccionada?.id == nave.id ||
                                            (uiState.naveSeleccionada == null && uiState.navesSocio.firstOrNull()?.id == nave.id)

                                    ItemSelectorNaveSocio(
                                        nave = nave,
                                        seleccionada = isSelected,
                                        onClick = { viewModel.seleccionarNave(nave) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. ¿Cuándo ocurrió?
                    item {
                        Text(
                            text = "2. ¿CUÁNDO OCURRIÓ EL INCIDENTE?",
                            color = Gold500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.cuandoOcurrio,
                            onValueChange = { viewModel.onCuandoOcurrioChanged(it) },
                            placeholder = {
                                Text(
                                    "Ej. Hoy a las 11:00 AM durante atraque en muelle 3, o ayer navegando",
                                    color = Muted
                                )
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = Gold500)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold500,
                                unfocusedBorderColor = Navy700,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                                focusedContainerColor = Navy800,
                                unfocusedContainerColor = Navy800
                            )
                        )
                    }

                    // 3. Descripción del Daño
                    item {
                        Text(
                            text = "3. DETALLES Y DESCRIPCIÓN DEL DAÑO",
                            color = Gold500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.descripcion,
                            onValueChange = { viewModel.onDescripcionChanged(it) },
                            placeholder = {
                                Text(
                                    "Describe lo ocurrido: rajadura en proa, rozadura en gelcoat, vía de agua, hélice golpeada, corrosión, etc...",
                                    color = Muted
                                )
                            },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold500,
                                unfocusedBorderColor = Navy700,
                                focusedTextColor = Ink,
                                unfocusedTextColor = Ink,
                                focusedContainerColor = Navy800,
                                unfocusedContainerColor = Navy800
                            )
                        )
                    }

                    // 4. Botón de Envío
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.enviarReporte() },
                            enabled = !uiState.enviando && uiState.naveSeleccionada != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (uiState.enviando) {
                                CircularProgressIndicator(color = Navy900, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Enviando reporte...", color = Navy900, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Navy900)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Solicitar Asistencia de Mantenimiento", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                // PESTAÑA HISTORIAL DE REPORTES DEL SOCIO
                if (uiState.reportesHistorial.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Muted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No tienes reportes de daños activos", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Usa la pestaña 'Nuevo Reporte' si alguna nave presenta una avería.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.reportesHistorial, key = { it.id }) { reporte ->
                            CardReporteSocio(reporte = reporte)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta individual para seleccionar la nave del socio (100% segura contra crashes).
 */
@Composable
fun ItemSelectorNaveSocio(
    nave: Embarcacion,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (seleccionada) Navy800 else Navy900,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (seleccionada) Gold500 else Navy700),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (seleccionada) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (seleccionada) Gold500 else Muted,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (seleccionada) Gold500.copy(alpha = 0.2f) else Navy800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = if (seleccionada) Gold500 else Muted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nave.nombreNave,
                    color = if (seleccionada) Gold500 else Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${nave.tipo} · Matrícula: ${nave.matricula} · ${nave.eslora} m",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            if (seleccionada) {
                Surface(
                    color = Gold500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "SELECCIONADO",
                        color = Gold500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardReporteSocio(reporte: ReporteDanio) {
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaStr = remember(reporte.fechaReporte) {
        if (reporte.fechaReporte > 0) formatoFecha.format(Date(reporte.fechaReporte)) else "Fecha reciente"
    }

    val (colorEstado, textoEstado) = when (reporte.estado.lowercase()) {
        "en_reparacion" -> Gold500 to "En Reparación / Dique"
        "en_inspeccion" -> Color(0xFF64B5F6) to "En Inspección IA"
        "resuelto" -> Color(0xFF4CAF50) to "Resuelto / Reparado"
        else -> Color(0xFFFFA000) to "Pendiente de Asistencia"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        border = BorderStroke(1.dp, Navy700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = reporte.nombreNave,
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    color = colorEstado.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Matrícula: ${reporte.matricula} • $fechaStr",
                color = Muted,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Navy900,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reporte.descripcion,
                    color = Ink,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
