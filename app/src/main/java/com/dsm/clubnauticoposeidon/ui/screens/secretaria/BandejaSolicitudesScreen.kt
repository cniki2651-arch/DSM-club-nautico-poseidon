package com.dsm.clubnauticoposeidon.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.firestore.FirebaseFirestore

// Modelo para mapear los datos de Firestore
data class PostulanteModel(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val embarcacion_registrada: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretariaScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var solicitudes by remember { mutableStateOf<List<PostulanteModel>>(emptyList()) }

    // Descargar las solicitudes en tiempo real
    LaunchedEffect(Unit) {
        db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val lista = snapshot.documents.map { doc ->
                        PostulanteModel(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            apellidos = doc.getString("apellidos") ?: "",
                            dni = doc.getString("dni") ?: "",
                            correo = doc.getString("correo") ?: "",
                            embarcacion_registrada = doc.getBoolean("embarcacion_registrada") ?: false
                        )
                    }
                    solicitudes = lista
                }
            }
    }

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

            if (solicitudes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(solicitudes) { postulante ->
                        PostulanteCard(postulante, db)
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostulanteCard(postulante: PostulanteModel, db: FirebaseFirestore) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    // Estados del formulario dentro del AlertDialog
    var clubConsultado by remember { mutableStateOf("") }
    var nombreContacto by remember { mutableStateOf("") }
    
    // Lista exacta de historiales requeridos
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

            // Botón Único: Realizar Verificación Externa
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                Text("Realizar Verificación Externa", color = Navy900, fontWeight = FontWeight.Bold)
            }
        }
    }

    // ALertDialog Temático
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

                    // Selector de Historial Reportado
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
                        // Validación de campos vacíos
                        if (clubConsultado.isBlank() || nombreContacto.isBlank()) {
                            Toast.makeText(context, "Debe completar todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Lógica de Clasificación
                        val clasificacionCalculada = when (historialSeleccionado) {
                            "Puntual" -> "socio pagador"
                            "Atrasos ocasionales" -> "socio pagador esporádico"
                            "Deudor / Problemático" -> "socio renuente a pago"
                            else -> "socio pagador" // Fallback de seguridad
                        }

                        // Actualización a Firestore
                        db.collection("usuarios").document(postulante.id)
                            .update(
                                mapOf(
                                    "estado" to "revisado_secretaria",
                                    "clasificacion" to clasificacionCalculada,
                                    "verificacion_externa" to mapOf(
                                        "club" to clubConsultado,
                                        "contacto" to nombreContacto,
                                        "historial" to historialSeleccionado
                                    )
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context, 
                                    "Enviado a Jefatura con clasificación: $clasificacionCalculada", 
                                    Toast.LENGTH_LONG
                                ).show()
                                showDialog = false
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al guardar verificación", Toast.LENGTH_SHORT).show()
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