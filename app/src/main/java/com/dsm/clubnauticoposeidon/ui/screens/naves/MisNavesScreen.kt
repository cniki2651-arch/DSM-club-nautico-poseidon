package com.dsm.clubnauticoposeidon.ui.screens.naves

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EstadoSolicitud
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

private val TiposNave = listOf("lancha", "velero", "yate_mediano", "yate_grande")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisNavesScreen(
    onBackClick: () -> Unit,
    viewModel: MisNavesViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var naveSolicitud by remember { mutableStateOf<Embarcacion?>(null) }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }
    LaunchedEffect(ui.registrada) {
        if (ui.registrada) {
            mostrarFormulario = false
            viewModel.registroProcesado()
        }
    }
    LaunchedEffect(ui.solicitudEnviada) {
        if (ui.solicitudEnviada) {
            naveSolicitud = null
            viewModel.solicitudProcesada()
        }
    }

    if (mostrarFormulario) {
        FormularioNave(
            guardando = ui.guardando,
            onCancelar = { mostrarFormulario = false },
            onGuardar = viewModel::registrar
        )
    }

    naveSolicitud?.let { nave ->
        SolicitarRadaDialog(
            nave = nave,
            enviando = ui.guardando,
            onCancelar = { naveSolicitud = null },
            onEnviar = { ingreso, salida -> viewModel.solicitarRada(nave, ingreso, salida) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Naves", color = Gold500, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { mostrarFormulario = true },
                enabled = ui.dni != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(50)
            ) {
                Text("Registrar embarcación", color = Navy900, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(20.dp))

            if (ui.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold500)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (ui.naves.isEmpty()) {
                        item { Text("Aún no registraste embarcaciones", color = Color.LightGray) }
                    }
                    items(ui.naves, key = { "n_" + it.id }) { nave ->
                        TarjetaNave(nave = nave, onSolicitar = { naveSolicitud = nave })
                    }
                    if (ui.solicitudes.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Mis solicitudes de rada",
                                color = Gold500,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(ui.solicitudes, key = { "s_" + it.id }) { TarjetaSolicitud(it) }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaNave(nave: Embarcacion, onSolicitar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(nave.nombreNave, color = Gold500, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Matrícula: ${nave.matricula}", color = Color.White, fontSize = 15.sp)
            Text("Tipo: ${nave.tipo}", color = Color.White, fontSize = 15.sp)
            Text("Eslora: ${nave.eslora}", color = Color.White, fontSize = 15.sp)
            Spacer(Modifier.height(14.dp))
            OutlinedButton(
                onClick = onSolicitar,
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Gold500),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500)
            ) { Text("Solicitar rada", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun TarjetaSolicitud(s: SolicitudRada) {
    val (color, texto) = when (s.estado) {
        EstadoSolicitud.APROBADA -> Color(0xFF4CAF50) to "Aprobada"
        EstadoSolicitud.RECHAZADA -> Color(0xFFE57373) to "Rechazada"
        else -> Color(0xFFF9A825) to "Pendiente"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(texto, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Text("${s.nombreNave} (${s.matricula})", color = Color.White, fontSize = 15.sp)
            Text("Ingreso: ${s.fechaIngreso}   Salida: ${s.fechaSalida}", color = Color.LightGray, fontSize = 13.sp)
            if (s.estado == EstadoSolicitud.APROBADA) {
                Text("Rada asignada: ${s.radaAsignada}", color = Gold500, fontWeight = FontWeight.SemiBold)
            }
            if (s.estado == EstadoSolicitud.RECHAZADA && s.motivoRechazo.isNotBlank()) {
                Text("Motivo: ${s.motivoRechazo}", color = Color(0xFFFF8A80), fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioNave(
    guardando: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (matricula: String, nombre: String, tipo: String, eslora: String) -> Unit
) {
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TiposNave[0]) }
    var eslora by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }

    val colores = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold500,
        unfocusedBorderColor = Color.LightGray,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Gold500,
        focusedLabelColor = Gold500,
        unfocusedLabelColor = Color.LightGray
    )

    val valido = matricula.isNotBlank() && nombre.isNotBlank() && eslora.isNotBlank() && !guardando

    AlertDialog(
        onDismissRequest = { if (!guardando) onCancelar() },
        containerColor = Navy900,
        title = { Text("Registrar embarcación", color = Gold500, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it.uppercase().filter { c -> !c.isWhitespace() } },
                    label = { Text("Matrícula") },
                    singleLine = true,
                    colors = colores
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la nave") },
                    singleLine = true,
                    colors = colores
                )
                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                    OutlinedTextField(
                        value = tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.menuAnchor(),
                        colors = colores
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        TiposNave.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = { tipo = opcion; expandido = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = eslora,
                    onValueChange = { eslora = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(6) },
                    label = { Text("Eslora") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = colores
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(matricula, nombre, tipo, eslora) },
                enabled = valido,
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                if (guardando) CircularProgressIndicator(Modifier.height(18.dp), color = Navy900, strokeWidth = 2.dp)
                else Text("Guardar", color = Navy900, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !guardando) { Text("Cancelar", color = Color.White) }
        }
    )
}