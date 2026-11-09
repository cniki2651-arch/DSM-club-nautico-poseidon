package com.dsm.clubnauticoposeidon.ui.screens.naviero

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.components.VisorTelemetriaVectorial
import com.dsm.clubnauticoposeidon.ui.screens.zarpe.SolicitudZarpe
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val cargosDisponibles = listOf(
    "Capitán / Patrón",
    "Ingeniero de Máquinas",
    "Marinero de Cubierta",
    "Operador de Radio / Telecom",
    "Patrón de Yate"
)

private fun formatFechaHora(timestamp: Long): String {
    if (timestamp <= 0) return "N/A"
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaZarpesScreen(
    onBackClick: () -> Unit,
    viewModel: BandejaZarpesViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var mostrarDialogoTripulante by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Autorización de Zarpes", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Servicios Navieros & Capitanía", color = Muted, fontSize = 13.sp)
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
            // Control de Pestañas (TabRow)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Navy800,
                contentColor = Gold500,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Gold500,
                        height = 3.dp
                    )
                }
            ) {
                val solicitudesCount = (uiState as? BandejaZarpesState.Exito)?.solicitudesPendientes?.size ?: 0

                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Operaciones de Zarpe", color = if (selectedTab == 0) Gold500 else Muted, fontWeight = FontWeight.Bold)
                            if (solicitudesCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = Color(0xFFE57373), contentColor = Color.White) {
                                    Text("$solicitudesCount")
                                }
                            }
                        }
                    }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("Padrón de Tripulación", color = if (selectedTab == 1) Gold500 else Muted, fontWeight = FontWeight.Bold)
                    }
                )
            }

            // Contenido según el Estado y la Pestaña seleccionada
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is BandejaZarpesState.Cargando -> {
                        CircularProgressIndicator(color = Gold500)
                    }

                    is BandejaZarpesState.Error -> {
                        Text(text = state.message, color = Color(0xFFE57373), fontSize = 16.sp, textAlign = TextAlign.Center)
                    }

                    is BandejaZarpesState.Exito -> {
                        if (selectedTab == 0) {
                            // TAB 1: Solicitudes y Operaciones de Zarpe
                            if (state.solicitudesPendientes.isEmpty()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(Icons.Default.Anchor, contentDescription = null, tint = Muted, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay operaciones ni solicitudes de zarpe activas en este momento.",
                                        color = Muted,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(state.solicitudesPendientes, key = { it.id }) { solicitud ->
                                        SolicitudZarpeCard(
                                            solicitud = solicitud,
                                            onAprobar = {
                                                viewModel.aprobarZarpe(
                                                    zarpeId = solicitud.id,
                                                    onSuccess = { Toast.makeText(context, "Zarpe aprobado exitosamente", Toast.LENGTH_SHORT).show() },
                                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                                )
                                            },
                                            onRechazar = { motivo ->
                                                viewModel.rechazarZarpe(
                                                    zarpeId = solicitud.id,
                                                    motivo = motivo,
                                                    onSuccess = { Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show() },
                                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                                )
                                            },
                                            onFinalizarArribo = {
                                                viewModel.finalizarZarpe(
                                                    zarpeId = solicitud.id,
                                                    onSuccess = { Toast.makeText(context, "Arribo registrado. Nave en muelle.", Toast.LENGTH_SHORT).show() },
                                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_SHORT).show() }
                                                )
                                            }
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(24.dp)) }
                                }
                            }
                        } else {
                            // TAB 2: Padrón de Tripulación
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { mostrarDialogoTripulante = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Navy900, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Registrar Tripulante", color = Navy900, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.tripulantesAutorizados.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "No hay tripulantes especializados registrados. Registre uno nuevo para habilitar la selección en zarpes",
                                            color = Muted,
                                            fontSize = 15.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(state.tripulantesAutorizados, key = { it.id }) { tripulante ->
                                            TripulanteCard(tripulante = tripulante)
                                        }
                                        item { Spacer(modifier = Modifier.height(24.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para Registrar Tripulante
    if (mostrarDialogoTripulante) {
        RegistrarTripulanteDialog(
            onDismiss = { mostrarDialogoTripulante = false },
            onGuardar = { nombre, dni, licencia, cargo ->
                viewModel.registrarTripulante(
                    nombre = nombre,
                    dni = dni,
                    licencia = licencia,
                    cargo = cargo,
                    onSuccess = {
                        Toast.makeText(context, "Tripulante registrado y autorizado", Toast.LENGTH_SHORT).show()
                        mostrarDialogoTripulante = false
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
fun SolicitudZarpeCard(
    solicitud: SolicitudZarpe,
    onAprobar: () -> Unit,
    onRechazar: (String) -> Unit,
    onFinalizarArribo: () -> Unit
) {
    val context = LocalContext.current
    var mostrarDialogoRechazo by remember { mutableStateOf(false) }
    var motivoRechazoText by remember { mutableStateOf("") }

    val colorBordeCard = when (solicitud.estado) {
        "activo", "en_mar" -> Color(0xFF4CAF50)
        "aprobado" -> Color(0xFF4EBA6F)
        else -> Color(0xFFFFB74D)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colorBordeCard.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Badge Superior según estado dinámico
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = solicitud.embarcacionNombre.ifBlank { "Embarcación sin nombre" },
                    color = Gold500,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                when (solicitud.estado) {
                    "activo", "en_mar" -> {
                        Surface(
                            color = Color(0xFF1B4332),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🟢 TRANSMITIENDO GPS EN VIVO",
                                color = Color(0xFF4EBA6F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    "aprobado" -> {
                        Surface(
                            color = Color(0xFF1B4332),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ZARPE AUTORIZADO",
                                color = Color(0xFF4EBA6F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            color = Navy800,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Gold500.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "PENDIENTE DE REVISIÓN",
                                color = Gold500,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Destino: ${solicitud.destino.ifBlank { "Mar Abierto" }}",
                    color = Ink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Partida: ${solicitud.fechaPartida} ${solicitud.horaPartida}",
                    color = Muted,
                    fontSize = 13.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(22.dp))
                Text(
                    text = "Retorno: ${solicitud.fechaRetorno} ${solicitud.horaRetorno}",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pasajeros / Tripulantes a bordo: ${solicitud.cantidadTripulantes}",
                    color = Muted,
                    fontSize = 13.sp
                )
            }

            if (solicitud.manifiestoPasajeros.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy900, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Manifiesto de Pasajeros (Capitanía):",
                        color = Gold500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = solicitud.manifiestoPasajeros,
                        color = Ink,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (solicitud.tripulacionEspecializada.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Especializada: ${solicitud.tripulacionEspecializada.joinToString(", ")}",
                    color = Gold500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (solicitud.observacionesTripulacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obs: ${solicitud.observacionesTripulacion}",
                    color = Muted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CONTROLES DYNAMICOS SEGÚN ESTADO (Bottom Row)
            when (solicitud.estado) {
                "pendiente_aprobacion" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mostrarDialogoRechazo = true },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0xFFE57373)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Rechazar", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onAprobar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("APROBAR ZARPE", color = Navy900, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                "aprobado" -> {
                    Surface(
                        color = Navy900,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4EBA6F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zarpe autorizado por Capitanía. Esperando que el socio inicie la navegación en su aplicación.",
                                color = Ink,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                "activo", "en_mar" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Visor Telemetría Vectorial (Reemplazo de Google Maps)
                        VisorTelemetriaVectorial(
                            latitud = solicitud.latitud,
                            longitud = solicitud.longitud
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onFinalizarArribo,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("REGISTRAR ARRIBO A PUERTO", color = Navy900, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Diálogo de Rechazo
    if (mostrarDialogoRechazo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRechazo = false },
            containerColor = Navy900,
            title = { Text("Rechazar Solicitud de Zarpe", color = Color(0xFFE57373), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Indique el motivo por el cual se rechaza esta salida marítima:", color = Muted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = motivoRechazoText,
                        onValueChange = { motivoRechazoText = it },
                        label = { Text("Motivo de rechazo", color = Muted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE57373),
                            unfocusedBorderColor = Muted,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                            cursorColor = Color(0xFFE57373)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (motivoRechazoText.isNotBlank()) {
                            onRechazar(motivoRechazoText)
                            mostrarDialogoRechazo = false
                            motivoRechazoText = ""
                        } else {
                            Toast.makeText(context, "El motivo es obligatorio", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
                ) {
                    Text("Confirmar Rechazo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRechazo = false; motivoRechazoText = "" }) {
                    Text("Cancelar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun TripulanteCard(tripulante: Tripulante) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Columna de datos (ocupa todo el espacio disponible a la izquierda)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tripulante.nombreCompleto,
                    color = Ink,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cargo: ${tripulante.cargo}",
                    color = Muted,
                    fontSize = 12.sp
                )
                Text(
                    text = "DNI: ${tripulante.dni}",
                    color = Muted,
                    fontSize = 12.sp
                )
                Text(
                    text = "Licencia Marítima: ${tripulante.numeroLicencia}",
                    color = Gold500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge compacto a la derecha
            Surface(
                color = Color(0xFF1B4332), // Verde oscuro contenedor
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "AUTORIZADO",
                    color = Color(0xFF4EBA6F), // Verde claro texto
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarTripulanteDialog(
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var licencia by remember { mutableStateOf("") }
    var cargoSeleccionado by remember { mutableStateOf(cargosDisponibles[0]) }
    var expandedCargo by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold500,
        unfocusedBorderColor = Muted,
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Gold500
    )

    val valido = nombre.isNotBlank() && dni.length == 8 && licencia.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Navy900,
        title = { Text("Registrar Nuevo Tripulante", color = Gold500, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre Completo", color = Muted) },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("DNI", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licencia,
                    onValueChange = { licencia = it },
                    label = { Text("Nº de Licencia Marítima", color = Muted) },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown para Cargo
                ExposedDropdownMenuBox(
                    expanded = expandedCargo,
                    onExpandedChange = { expandedCargo = !expandedCargo }
                ) {
                    OutlinedTextField(
                        value = cargoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo Especializado", color = Muted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCargo) },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCargo,
                        onDismissRequest = { expandedCargo = false },
                        modifier = Modifier.background(Navy800)
                    ) {
                        cargosDisponibles.forEach { cargo ->
                            DropdownMenuItem(
                                text = { Text(cargo, color = Ink) },
                                onClick = {
                                    cargoSeleccionado = cargo
                                    expandedCargo = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, dni, licencia, cargoSeleccionado) },
                enabled = valido,
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                Text("Registrar", color = Navy900, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}
