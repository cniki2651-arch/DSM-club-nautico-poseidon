package com.dsm.clubnauticoposeidon.ui.screens.secretaria

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// 3. Vista (UI)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaSecretariaScreen(
    viewModel: BandejaSecretariaViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val postulantes by viewModel.postulantes.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bandeja de Secretaría", color = Gold500, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Solicitudes Pendientes de Clasificación",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold500)
                }
            } else if (postulantes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(postulantes) { postulante ->
                        PostulanteCard(
                            postulante = postulante,
                            onGuardarVerificacion = { club, contacto, historial, onCompletado ->
                                viewModel.procesarVerificacion(
                                    postulanteId = postulante.id,
                                    club = club,
                                    contacto = contacto,
                                    historial = historial,
                                    onSuccess = { clasificacionCalculada ->
                                        Toast.makeText(
                                            context,
                                            "Enviado a Jefatura con clasificación: $clasificacionCalculada",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onCompletado()
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostulanteCard(
    postulante: Postulante,
    onGuardarVerificacion: (club: String, contacto: String, historial: String, onCompletado: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    var clubConsultado by remember { mutableStateOf("") }
    var nombreContacto by remember { mutableStateOf("") }

    val historiales = listOf("Puntual", "Atrasos ocasionales", "Deudor / Problemático")
    var historialSeleccionado by remember { mutableStateOf(historiales[0]) }
    var expandidoHistorial by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${postulante.nombre} ${postulante.apellidos}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "DNI: ${postulante.dni}", color = Color.LightGray, fontSize = 14.sp)
            Text(text = "Correo: ${postulante.correo}", color = Color.LightGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))

            if (postulante.embarcacion_registrada) {
                Text(text = "🚤 Incluye datos de embarcación", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(text = "Sin embarcación registrada", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                Text("Realizar Verificación Externa", color = Navy900, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Navy900,
            title = {
                Text(
                    text = "Verificación de Antecedentes",
                    color = Gold500,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = clubConsultado,
                        onValueChange = { clubConsultado = it },
                        label = { Text("Club Consultado", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold500,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Gold500
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nombreContacto,
                        onValueChange = { nombreContacto = it },
                        label = { Text("Nombre del Contacto", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold500,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Gold500
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandidoHistorial,
                        onExpandedChange = { expandidoHistorial = !expandidoHistorial }
                    ) {
                        OutlinedTextField(
                            value = historialSeleccionado,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Historial Reportado", color = Color.LightGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoHistorial) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold500,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandidoHistorial,
                            onDismissRequest = { expandidoHistorial = false },
                            modifier = Modifier.background(Navy900)
                        ) {
                            historiales.forEach { historial ->
                                DropdownMenuItem(
                                    text = { Text(historial, color = Color.White) },
                                    onClick = {
                                        historialSeleccionado = historial
                                        expandidoHistorial = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (clubConsultado.isBlank() || nombreContacto.isBlank()) {
                            Toast.makeText(context, "Debe completar todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onGuardarVerificacion(clubConsultado, nombreContacto, historialSeleccionado) {
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500)
                ) {
                    Text("Guardar y Enviar", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}
