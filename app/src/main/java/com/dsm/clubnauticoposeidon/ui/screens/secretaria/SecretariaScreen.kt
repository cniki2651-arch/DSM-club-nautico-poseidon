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

@Composable
fun PostulanteCard(postulante: PostulanteModel, db: FirebaseFirestore) {
    val context = LocalContext.current
    var expandido by remember { mutableStateOf(false) }
    var clasificacionSeleccionada by remember { mutableStateOf("Seleccionar...") }
    val opciones = listOf("pagador", "honorario", "deportivo") // La HU exige que solo 'pagador' pueda ser aprobado luego por el jefe

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

            // Selector de Clasificación (Dropdown)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = clasificacionSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Clasificación de Socio") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar") },
                    modifier = Modifier.fillMaxWidth().clickable { expandido = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        focusedBorderColor = Gold500,
                        unfocusedBorderColor = Color.Gray
                    ),
                    enabled = false // Para que solo reaccione al click
                )
                DropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false },
                    modifier = Modifier.background(Navy900)
                ) {
                    opciones.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion.uppercase(), color = Color.White) },
                            onClick = {
                                clasificacionSeleccionada = opcion
                                expandido = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Enviar a Jefatura
            Button(
                onClick = {
                    if (clasificacionSeleccionada == "Seleccionar...") {
                        Toast.makeText(context, "Debe asignar una clasificación primero", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Actualizamos el estado en Firestore a "revisado_secretaria"
                    db.collection("usuarios").document(postulante.id)
                        .update(
                            mapOf(
                                "estado" to "revisado_secretaria",
                                "clasificacion" to clasificacionSeleccionada
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(context, "Enviado al Jefe de Atención al Cliente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                Text("Enviar a Jefatura", color = Navy900, fontWeight = FontWeight.Bold)
            }
        }
    }
}