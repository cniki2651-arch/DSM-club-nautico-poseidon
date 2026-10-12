package com.dsm.clubnauticoposeidon.ui.screens.solicitudes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaSolicitudesRadaScreen(
    onBackClick: () -> Unit,
    viewModel: BandejaRadasViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()
    var aAprobar by remember { mutableStateOf<SolicitudRada?>(null) }
    var aRechazar by remember { mutableStateOf<SolicitudRada?>(null) }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }

    // ---------- Aprobar: elegir rada compatible ----------
    aAprobar?.let { sol ->
        val compatibles = ui.radas
            .filter { it.estado == EstadoRada.DISPONIBLE && it.esloraMax >= sol.eslora }
            .sortedBy { it.esloraMax }
        var elegida by remember(sol.id) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { aAprobar = null },
            containerColor = Navy900,
            title = { Text("Asignar rada", color = Gold500, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "${sol.nombreNave} · eslora ${sol.eslora}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    if (compatibles.isEmpty()) {
                        Text("No hay radas disponibles donde quepa esta nave", color = Color(0xFFFF8A80))
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 280.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            compatibles.forEach { rada ->
                                val seleccionada = elegida == rada.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { elegida = rada.id },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (seleccionada) Gold500.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.08f)
                                    ),
                                    border = BorderStroke(1.dp, if (seleccionada) Gold500 else Color.Gray)
                                ) {
                                    Text(
                                        text = "${rada.codigo.ifBlank { rada.id }} · ${rada.muelle} · hasta ${rada.esloraMax}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.aprobar(sol, elegida); aAprobar = null },
                    enabled = elegida.isNotBlank() && !ui.procesando,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500)
                ) { Text("Aprobar", color = Navy900, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { aAprobar = null }) { Text("Cancelar", color = Color.White) }
            }
        )
    }

    // ---------- Rechazar: motivo obligatorio ----------
    aRechazar?.let { sol ->
        var motivo by remember(sol.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { aRechazar = null },
            containerColor = Navy900,
            title = { Text("Rechazar solicitud", color = Gold500, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it.take(120) },
                    label = { Text("Motivo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold500,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Gold500,
                        focusedLabelColor = Gold500,
                        unfocusedLabelColor = Color.LightGray
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.rechazar(sol.id, motivo); aRechazar = null },
                    enabled = motivo.isNotBlank() && !ui.procesando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
                ) { Text("Rechazar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { aRechazar = null }) { Text("Cancelar", color = Color.White) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de Rada", color = Gold500, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                ui.cargando -> CircularProgressIndicator(color = Gold500, modifier = Modifier.align(Alignment.Center))
                ui.solicitudes.isEmpty() -> Text(
                    "No hay solicitudes pendientes",
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(ui.solicitudes, key = { it.id }) { sol ->
                        TarjetaPendiente(
                            sol = sol,
                            onAprobar = { aAprobar = sol },
                            onRechazar = { aRechazar = sol }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPendiente(sol: SolicitudRada, onAprobar: () -> Unit, onRechazar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(sol.nombreNave, color = Gold500, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Matrícula: ${sol.matricula}", color = Color.White, fontSize = 15.sp)
            Text("Tipo: ${sol.tipo}", color = Color.White, fontSize = 15.sp)
            Text("Eslora: ${sol.eslora}", color = Color.White, fontSize = 15.sp)
            Text("DNI del socio: ${sol.dniSocio}", color = Color.LightGray, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text("Ingreso: ${sol.fechaIngreso}   Salida: ${sol.fechaSalida}", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color(0xFFE57373)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373))
                ) { Text("Rechazar") }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onAprobar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500)
                ) { Text("Aprobar", color = Navy900, fontWeight = FontWeight.Bold) }
            }
        }
    }
}