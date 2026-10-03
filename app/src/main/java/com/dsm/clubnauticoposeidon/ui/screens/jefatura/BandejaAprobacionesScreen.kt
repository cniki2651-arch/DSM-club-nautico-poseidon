package com.dsm.clubnauticoposeidon.ui.screens.jefatura

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.firestore.FirebaseFirestore

// Modelo de datos mapeado de Firestore
data class CandidatoModel(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val correo: String = "",
    val clasificacion: String = "",
    val verificacionExterna: Map<String, String>? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaAprobacionesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var candidatos by remember { mutableStateOf<List<CandidatoModel>>(emptyList()) }

    // Conexión y escucha en tiempo real a Firestore
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "revisado_secretaria")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar las aprobaciones pendientes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listaCandidatos = snapshot.documents.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        CandidatoModel(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            apellidos = doc.getString("apellidos") ?: "",
                            dni = doc.getString("dni") ?: "",
                            correo = doc.getString("correo") ?: "",
                            clasificacion = doc.getString("clasificacion") ?: "",
                            verificacionExterna = doc.get("verificacion_externa") as? Map<String, String>
                        )
                    }
                    candidatos = listaCandidatos
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aprobación de Socios",
                        color = Gold500,
                        fontWeight = FontWeight.Bold
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            Spacer(modifier = Modifier.height(8.dp))

            if (candidatos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay solicitudes pendientes de aprobación",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(candidatos) { candidato ->
                        PostulanteAprobacionCard(candidato = candidato)
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun PostulanteAprobacionCard(candidato: CandidatoModel) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var mostrarDialogoRechazo by remember { mutableStateOf(false) }
    var motivoRechazo by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Datos Básicos
            Text(
                text = "${candidato.nombre} ${candidato.apellidos}",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "DNI: ${candidato.dni}", color = Color.LightGray, fontSize = 14.sp)
            Text(text = "Correo: ${candidato.correo}", color = Color.LightGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // Reporte de Secretaría (Caja destacada)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Reporte de Secretaría",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val colorClasificacion = if (candidato.clasificacion == "socio pagador") Gold500 else Color(0xFFFF5252)
                    
                    Row {
                        Text(text = "Clasificación Asignada: ", color = Color.LightGray, fontSize = 14.sp)
                        Text(
                            text = candidato.clasificacion.uppercase(),
                            color = colorClasificacion,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    if (candidato.verificacionExterna != null) {
                        val club = candidato.verificacionExterna["club"] ?: "N/A"
                        val contacto = candidato.verificacionExterna["contacto"] ?: "N/A"
                        val historial = candidato.verificacionExterna["historial"] ?: "N/A"

                        Text(
                            text = "Verificación: Club $club, Contacto: $contacto",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Historial Reportado: $historial",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Verificación externa no disponible.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de Acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Botón Rechazar
                OutlinedButton(
                    onClick = { mostrarDialogoRechazo = true },
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Rechazar")
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                // Botón Aprobar
                Button(
                    onClick = {
                        if (candidato.clasificacion == "socio pagador") {
                            db.collection("usuarios").document(candidato.id)
                                .update(
                                    mapOf(
                                        "estado" to "aprobado",
                                        "rol" to "socio"
                                    )
                                )
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Socio aprobado exitosamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al aprobar al socio", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Error: La política del club solo admite socios clasificados como 'pagador'.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Aprobar", color = Navy900, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Cuadro de Diálogo para el Rechazo (Justificación)
    if (mostrarDialogoRechazo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRechazo = false },
            containerColor = Navy900,
            title = {
                Text(
                    text = "Rechazar Postulante",
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Por favor, detalle el motivo del rechazo de la solicitud.",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Motivo de Desaprobación", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5252),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFF5252)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (motivoRechazo.isBlank()) {
                            Toast.makeText(context, "El motivo es obligatorio", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        db.collection("usuarios").document(candidato.id)
                            .update(
                                mapOf(
                                    "estado" to "rechazado",
                                    "motivo_rechazo" to motivoRechazo
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(context, "Solicitud rechazada correctamente", Toast.LENGTH_SHORT).show()
                                mostrarDialogoRechazo = false
                                motivoRechazo = "" // Limpia el estado
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al rechazar solicitud", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Confirmar Rechazo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        mostrarDialogoRechazo = false 
                        motivoRechazo = ""
                    }
                ) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}
