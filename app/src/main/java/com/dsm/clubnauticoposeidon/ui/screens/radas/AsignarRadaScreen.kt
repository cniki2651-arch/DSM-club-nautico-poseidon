package com.dsm.clubnauticoposeidon.ui.screens.radas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.screens.solicitudes.BandejaRadasViewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarRadaScreen(
    solicitudId: String,
    onBackClick: () -> Unit,
    onAbrirMapaClick: () -> Unit,
    radaSeleccionada: String = "",
    viewModel: BandejaRadasViewModel = viewModel(),
    radaViewModel: RadaViewModel = viewModel()   // solo para el aviso de clima
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()
    val clima by radaViewModel.ui.collectAsState()

    val solicitud = ui.solicitudes.firstOrNull { it.id == solicitudId }

    var radaElegida by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(radaSeleccionada) {
        if (radaSeleccionada.isNotBlank()) radaElegida = radaSeleccionada
    }

    var mostrarRechazo by rememberSaveable { mutableStateOf(false) }
    var motivo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }
    LaunchedEffect(ui.completado) {
        if (ui.completado) onBackClick()
    }

    // ---------- Diálogo de rechazo ----------
    if (mostrarRechazo && solicitud != null) {
        AlertDialog(
            onDismissRequest = { if (!ui.procesando) mostrarRechazo = false },
            title = { Text("Rechazar solicitud") },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo del rechazo") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rechazar(solicitud.id, motivo)
                        mostrarRechazo = false
                    },
                    enabled = motivo.isNotBlank() && !ui.procesando
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarRechazo = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asignar Rada", color = Color.White, fontWeight = FontWeight.Bold) },
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
        when {
            ui.cargando -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator(color = Gold500)
            }

            solicitud == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    "Esta solicitud ya fue atendida o no existe",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Aviso del clima (solo informativo, no bloquea la aprobación)
                clima.clima?.let { c ->
                    val seguro = clima.vientoSeguro == true
                    Text(
                        text = if (seguro)
                            "Clima favorable: viento ${c.wind_speed_10m} km/h"
                        else
                            "Viento fuerte (${c.wind_speed_10m} km/h): se recomienda precaución",
                        color = if (seguro) Color(0xFF7FD18B) else Color(0xFFFF8A80),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Datos de la solicitud (solo lectura)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Gold500)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = solicitud.nombreNave,
                            color = Gold500,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Matrícula: ${solicitud.matricula}", color = Color.White, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Eslora: ${solicitud.eslora}", color = Color.White, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha de ingreso: ${solicitud.fechaIngreso}", color = Color.White, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha de salida: ${solicitud.fechaSalida}", color = Color.White, fontSize = 17.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("DNI del socio: ${solicitud.dniSocio}", color = Color.LightGray, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Ubicación",
                    color = Gold500,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onAbrirMapaClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Gold500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Mapa",
                        tint = Gold500,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (radaElegida.isEmpty()) "Seleccionar Rada en el Mapa"
                        else "Rada seleccionada: $radaElegida",
                        color = if (radaElegida.isEmpty()) Color.White else Gold500,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                val puedeConfirmar = radaElegida.isNotBlank() && !ui.procesando

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { mostrarRechazo = true },
                        enabled = !ui.procesando,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF8A80)),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80))
                    ) {
                        Text("Rechazar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = { viewModel.aprobar(solicitud, radaElegida) },
                        enabled = puedeConfirmar,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Gold500,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (ui.procesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Navy900,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirmar",
                                color = if (puedeConfirmar) Navy900 else Color.LightGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}