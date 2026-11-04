package com.dsm.clubnauticoposeidon.ui.screens.zarpe

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudZarpeScreen(
    embarcacionId: String,
    embarcacionNombre: String,
    onBackClick: () -> Unit,
    viewModel: ZarpeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var destino by remember { mutableStateOf("") }
    var fechaHoraSalida by remember { mutableStateOf("") }
    var fechaHoraRegreso by remember { mutableStateOf("") }
    var cantidadTripulantesTexto by remember { mutableStateOf("1") }
    var observacionesTripulacion by remember { mutableStateOf("") }

    var zarpeAprobadoPendienteGPS by remember { mutableStateOf<SolicitudZarpe?>(null) }

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
                                    text = "Declaración Náutica: Capitanía revisará tu plan de navegación antes del zarpe. Al aprobarse, transmitirás tu posición GPS.",
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // b) Fecha y hora salida
                        OutlinedTextField(
                            value = fechaHoraSalida,
                            onValueChange = { fechaHoraSalida = it },
                            label = { Text("Fecha y Hora Estimada de Salida", color = Muted) },
                            placeholder = { Text("Ej: 04/11/2026 - 08:00 AM", color = Muted.copy(alpha = 0.6f)) },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // c) Fecha y hora regreso
                        OutlinedTextField(
                            value = fechaHoraRegreso,
                            onValueChange = { fechaHoraRegreso = it },
                            label = { Text("Fecha y Hora Estimada de Regreso", color = Muted) },
                            placeholder = { Text("Ej: 04/11/2026 - 18:00 PM", color = Muted.copy(alpha = 0.6f)) },
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // d) Cantidad tripulantes
                        OutlinedTextField(
                            value = cantidadTripulantesTexto,
                            onValueChange = { cantidadTripulantesTexto = it.filter { c -> c.isDigit() } },
                            label = { Text("Número de Tripulantes / Pasajeros", color = Muted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // e) Observaciones tripulación (Opcional)
                        OutlinedTextField(
                            value = observacionesTripulacion,
                            onValueChange = { observacionesTripulacion = it },
                            label = { Text("Nombres / Observaciones de Tripulación (Opcional)", color = Muted) },
                            placeholder = { Text("Ej: Juan Pérez (Capitán), Pedro Soto", color = Muted.copy(alpha = 0.6f)) },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Botón Enviar Solicitud a Capitanía
                        Button(
                            onClick = {
                                val cant = cantidadTripulantesTexto.toIntOrNull() ?: 0
                                if (destino.isBlank()) {
                                    Toast.makeText(context, "Ingresa el destino antes de enviar", Toast.LENGTH_SHORT).show()
                                } else if (fechaHoraSalida.isBlank() || fechaHoraRegreso.isBlank()) {
                                    Toast.makeText(context, "Completa las fechas estimadas de salida y regreso", Toast.LENGTH_SHORT).show()
                                } else if (cant <= 0) {
                                    Toast.makeText(context, "Ingresa al menos 1 tripulante", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.solicitarZarpe(
                                        embarcacionId = embarcacionId,
                                        embarcacionNombre = embarcacionNombre,
                                        destino = destino,
                                        fechaHoraSalida = fechaHoraSalida,
                                        fechaHoraRegreso = fechaHoraRegreso,
                                        cantidadTripulantes = cant,
                                        observacionesTripulacion = observacionesTripulacion
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
                                imageVector = Icons.Default.Send,
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
                                    Text("Salida: ${state.zarpe.fechaHoraSalida}", color = Muted, fontSize = 12.sp)
                                    Text("Regreso: ${state.zarpe.fechaHoraRegreso}", color = Muted, fontSize = 12.sp)
                                    Text("Tripulantes: ${state.zarpe.cantidadTripulantes}", color = Muted, fontSize = 12.sp)
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

                                Spacer(modifier = Modifier.height(8.dp))

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
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Anchor,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "RETORNO A PUERTO (FINALIZAR ZARPE)",
                                color = Color.White,
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
