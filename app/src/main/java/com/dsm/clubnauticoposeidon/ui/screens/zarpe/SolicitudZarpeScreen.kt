package com.dsm.clubnauticoposeidon.ui.screens.zarpe

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.components.VisorTelemetriaVectorial
import com.dsm.clubnauticoposeidon.ui.screens.naviero.Tripulante
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SolicitudZarpeScreen(
    embarcacionId: String,
    embarcacionNombre: String,
    onBackClick: () -> Unit,
    viewModel: ZarpeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Cargar tripulantes autorizados en tiempo real desde Firestore
    val tripulantesDisponibles by viewModel.tripulantesAutorizados.collectAsState()

    // 1. Campos del formulario de Declaración Náutica
    var destino by remember { mutableStateOf("") }
    var fechaPartida by remember { mutableStateOf("") }
    var horaPartida by remember { mutableStateOf("") }
    var fechaRetorno by remember { mutableStateOf("") }
    var horaRetorno by remember { mutableStateOf("") }

    var manifiestoPasajeros by remember { mutableStateOf("") }
    var observacionesTripulacion by remember { mutableStateOf("") }

    // 2. Estado para la selección múltiple de Tripulación Especializada
    var busquedaTripulante by remember { mutableStateOf("") }
    var menuBusquedaExpandido by remember { mutableStateOf(false) }
    var tripulantesSeleccionados by remember { mutableStateOf<List<Tripulante>>(emptyList()) }

    var zarpeAprobadoPendienteGPS by remember { mutableStateOf<SolicitudZarpe?>(null) }

    // Filtrar candidatos disponibles evitando duplicados
    val tripulantesFiltrados = remember(busquedaTripulante, tripulantesSeleccionados, tripulantesDisponibles) {
        if (busquedaTripulante.isBlank()) {
            emptyList()
        } else {
            tripulantesDisponibles.filter { t ->
                !tripulantesSeleccionados.contains(t) &&
                        (t.nombreCompleto.contains(busquedaTripulante, ignoreCase = true) ||
                                t.cargo.contains(busquedaTripulante, ignoreCase = true))
            }
        }
    }

    // Launcher de Permisos GPS (ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocation || coarseLocation) {
            val zarpeToStart = zarpeAprobadoPendienteGPS
            if (zarpeToStart != null) {
                viewModel.iniciarTransmisionGps(zarpeToStart.id, context)
            }
        } else {
            Toast.makeText(context, "Se requiere permiso GPS para iniciar la navegación", Toast.LENGTH_LONG).show()
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold500,
        unfocusedBorderColor = Muted,
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Gold500
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Declaración de Zarpe", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Nave: $embarcacionNombre", color = Muted, fontSize = 13.sp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is ZarpeState.Cargando -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Gold500, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Procesando solicitud de zarpe...",
                            color = Ink,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                is ZarpeState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Tarjeta Informativa
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Gold500,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Declaración Náutica: Capitanía revisará tu plan de navegación y el manifiesto de pasajeros antes del zarpe.",
                                    color = Ink,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // a) Destino
                        OutlinedTextField(
                            value = destino,
                            onValueChange = { destino = it },
                            label = { Text("Destino / Zona de Navegación", color = Muted) },
                            placeholder = { Text("Ej: Isla San Lorenzo / Mar Abierto", color = Muted.copy(alpha = 0.6f)) },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // b) Fila 1: Fecha de Partida y Hora de Partida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        mostrarDatePicker(context) { fechaPartida = it }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = fechaPartida,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha Partida", color = Muted) },
                                    placeholder = { Text("DD/MM/AAAA", color = Muted.copy(alpha = 0.6f)) },
                                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Gold500) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = if (fechaPartida.isNotBlank()) Gold500 else Muted,
                                        disabledTextColor = Ink,
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Gold500
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        mostrarTimePicker(context) { horaPartida = it }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = horaPartida,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Hora Partida", color = Muted) },
                                    placeholder = { Text("HH:MM AM", color = Muted.copy(alpha = 0.6f)) },
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Gold500) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = if (horaPartida.isNotBlank()) Gold500 else Muted,
                                        disabledTextColor = Ink,
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Gold500
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // c) Fila 2: Fecha de Retorno y Hora de Retorno
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        mostrarDatePicker(context) { fechaRetorno = it }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = fechaRetorno,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha Retorno", color = Muted) },
                                    placeholder = { Text("DD/MM/AAAA", color = Muted.copy(alpha = 0.6f)) },
                                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Gold500) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = if (fechaRetorno.isNotBlank()) Gold500 else Muted,
                                        disabledTextColor = Ink,
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Gold500
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        mostrarTimePicker(context) { horaRetorno = it }
                                    }
                            ) {
                                OutlinedTextField(
                                    value = horaRetorno,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Hora Retorno", color = Muted) },
                                    placeholder = { Text("HH:MM PM", color = Muted.copy(alpha = 0.6f)) },
                                    trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Gold500) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = if (horaRetorno.isNotBlank()) Gold500 else Muted,
                                        disabledTextColor = Ink,
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Gold500
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Manifiesto Obligatorio de Pasajeros / Personas a Bordo (Capitanía)
                        OutlinedTextField(
                            value = manifiestoPasajeros,
                            onValueChange = { manifiestoPasajeros = it },
                            label = { Text("Manifiesto de Pasajeros (DNI / CE / Pasaporte y Nombres)", color = Muted) },
                            placeholder = {
                                Text(
                                    "Ejemplo:\n- DNI 70123456 - Juan Pérez Ruiz\n- CE 001234567 - María Thorne\n- PAS 9876543 - Carlos Smith",
                                    color = Muted.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            },
                            supportingText = {
                                Text(
                                    "Ingresa a todos los pasajeros e invitados (incluyéndote si no vas como tripulación especializada). Requerido por Capitanía.",
                                    color = Muted,
                                    fontSize = 11.sp
                                )
                            },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // e) Búsqueda y Selección Múltiple de Tripulación Especializada (Conectado a Firestore)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = busquedaTripulante,
                                onValueChange = {
                                    busquedaTripulante = it
                                    menuBusquedaExpandido = it.isNotBlank()
                                },
                                label = { Text("Buscar Tripulación Especializada", color = Muted) },
                                placeholder = { Text("Ej: Capitán, Miguel Grau", color = Muted.copy(alpha = 0.6f)) },
                                trailingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = Gold500)
                                },
                                colors = textFieldColors,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            DropdownMenu(
                                expanded = menuBusquedaExpandido && tripulantesFiltrados.isNotEmpty(),
                                onDismissRequest = { menuBusquedaExpandido = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(Navy800)
                            ) {
                                tripulantesFiltrados.forEach { tripulante ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(tripulante.nombreCompleto, color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(tripulante.cargo, color = Gold500, fontSize = 12.sp)
                                            }
                                        },
                                        onClick = {
                                            if (!tripulantesSeleccionados.contains(tripulante)) {
                                                tripulantesSeleccionados = tripulantesSeleccionados + tripulante
                                            }
                                            busquedaTripulante = ""
                                            menuBusquedaExpandido = false
                                        }
                                    )
                                }
                            }
                        }

                        // FlowRow de Tripulantes Seleccionados con InputChips
                        if (tripulantesSeleccionados.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                tripulantesSeleccionados.forEach { t ->
                                    InputChip(
                                        selected = true,
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = "${t.nombreCompleto} (${t.cargo})",
                                                color = Ink,
                                                fontSize = 12.sp
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    tripulantesSeleccionados = tripulantesSeleccionados - t
                                                },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remover",
                                                    tint = Gold500,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        },
                                        colors = InputChipDefaults.inputChipColors(
                                            containerColor = Gold500.copy(alpha = 0.2f),
                                            labelColor = Ink
                                        ),
                                        border = InputChipDefaults.inputChipBorder(
                                            borderColor = Gold500,
                                            borderWidth = 1.dp,
                                            enabled = true,
                                            selected = true
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // f) Observaciones generales
                        OutlinedTextField(
                            value = observacionesTripulacion,
                            onValueChange = { observacionesTripulacion = it },
                            label = { Text("Observaciones Adicionales (Opcional)", color = Muted) },
                            placeholder = { Text("Notas de carga, equipo especial, etc.", color = Muted.copy(alpha = 0.6f)) },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Botón Enviar Solicitud a Capitanía
                        Button(
                            onClick = {
                                val pasajesLineas = manifiestoPasajeros.lines().filter { it.isNotBlank() }.size
                                val totalPersonas = tripulantesSeleccionados.size + pasajesLineas
                                val cant = if (totalPersonas > 0) totalPersonas else 1

                                if (destino.isBlank()) {
                                    Toast.makeText(context, "Ingresa el destino antes de enviar", Toast.LENGTH_SHORT).show()
                                } else if (fechaPartida.isBlank() || horaPartida.isBlank()) {
                                    Toast.makeText(context, "Selecciona la fecha y hora de partida", Toast.LENGTH_SHORT).show()
                                } else if (fechaRetorno.isBlank() || horaRetorno.isBlank()) {
                                    Toast.makeText(context, "Selecciona la fecha y hora de retorno", Toast.LENGTH_SHORT).show()
                                } else {
                                    val tripulacionFormateada = tripulantesSeleccionados.map { "${it.nombreCompleto} (${it.cargo})" }
                                    viewModel.solicitarZarpe(
                                        embarcacionId = embarcacionId,
                                        embarcacionNombre = embarcacionNombre,
                                        destino = destino,
                                        fechaPartida = fechaPartida,
                                        horaPartida = horaPartida,
                                        fechaRetorno = fechaRetorno,
                                        horaRetorno = horaRetorno,
                                        cantidadTripulantes = cant,
                                        manifiestoPasajeros = manifiestoPasajeros,
                                        observacionesTripulacion = observacionesTripulacion,
                                        tripulacionEspecializada = tripulacionFormateada
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Navy900,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ENVIAR SOLICITUD A CAPITANÍA",
                                color = Navy900,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                is ZarpeState.PendienteAprobacion -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFFB74D))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB74D).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = "Pendiente",
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Solicitud Enviada a Capitanía",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Tu declaración de zarpe está siendo revisada. Recibirás la notificación de aprobación para iniciar el zarpe.",
                                    color = Ink,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Navy900, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("Destino: ${state.zarpe.destino}", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Partida: ${state.zarpe.fechaPartida} - ${state.zarpe.horaPartida}", color = Muted, fontSize = 12.sp)
                                    Text("Retorno: ${state.zarpe.fechaRetorno} - ${state.zarpe.horaRetorno}", color = Muted, fontSize = 12.sp)
                                    Text("Personas a bordo: ${state.zarpe.cantidadTripulantes}", color = Muted, fontSize = 12.sp)
                                    if (state.zarpe.manifiestoPasajeros.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Manifiesto: ${state.zarpe.manifiestoPasajeros}", color = Ink, fontSize = 12.sp)
                                    }
                                    if (state.zarpe.tripulacionEspecializada.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Especializada: ${state.zarpe.tripulacionEspecializada.joinToString(", ")}", color = Gold500, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            border = BorderStroke(1.dp, Gold500),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Volver a Mis Naves", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                is ZarpeState.AprobadoListoParaZarpar -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF4CAF50))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Aprobado",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(60.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "¡Solicitud Aprobada por Capitanía!",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Tu zarpe hacia ${state.zarpe.destino} fue autorizado. Presiona el botón para iniciar la transmisión GPS en tiempo real.",
                                    color = Ink,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = {
                                zarpeAprobadoPendienteGPS = state.zarpe
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Navy900,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "INICIAR ZARPE Y TRANSMITIR GPS",
                                color = Navy900,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                is ZarpeState.MorosoBloqueado -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFF5252))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Bloqueado",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Zarpe Bloqueado",
                                    color = Color(0xFFFF5252),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Registras deudas pendientes o clasificación renuente a pago. Regulariza tu situación en Secretaría para habilitar salidas.",
                                    color = Ink,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                viewModel.resetState()
                                onBackClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Entendido / Volver", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }

                is ZarpeState.ZarpeActivo -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, Gold500)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Gold500.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radar,
                                        contentDescription = "Radar",
                                        tint = Gold500,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "TRANSMITIENDO GPS EN VIVO",
                                    color = Gold500,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                VisorTelemetriaVectorial(
                                    latitud = state.latitud,
                                    longitud = state.longitud
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "En navegación hacia:",
                                    color = Muted,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = state.destino,
                                    color = Ink,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                viewModel.finalizarZarpe(state.zarpeId)
                                Toast.makeText(context, "Arribo a puerto registrado con éxito", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Anchor,
                                contentDescription = null,
                                tint = Navy900,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RETORNO A PUERTO (FINALIZAR ZARPE)",
                                color = Navy900,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                is ZarpeState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE57373))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    color = Ink,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Gold500),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500)
                        ) {
                            Text("Reintentar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Helpers para DatePickerDialog y TimePickerDialog
private fun mostrarDatePicker(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun mostrarTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val hour12 = when {
                hourOfDay == 0 -> 12
                hourOfDay > 12 -> hourOfDay - 12
                else -> hourOfDay
            }
            val formattedTime = String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    ).show()
}
