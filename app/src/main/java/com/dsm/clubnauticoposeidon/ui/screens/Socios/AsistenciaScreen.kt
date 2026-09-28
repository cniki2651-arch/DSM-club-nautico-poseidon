package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// Modelo de datos
data class OpcionAsistencia(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector
)

// Mock Data
val opcionesAsistencia = listOf(
    OpcionAsistencia(
        "Atraque / Zarpe",
        "Asistencia de un marinero en muelle para amarrar o soltar cabos de forma segura.",
        Icons.Default.Anchor
    ),
    OpcionAsistencia(
        "Revisión de Cabos",
        "Inspección de las amarras, boyas y defensas de su yate en rada.",
        Icons.Default.Policy
    ),
    OpcionAsistencia(
        "Remolque Menor",
        "Servicio de remolque de emergencia dentro de la dársena del club.",
        Icons.Default.DirectionsBoat
    ),
    OpcionAsistencia(
        "Traslado en Bote",
        "Transporte rápido desde el muelle principal hacia su embarcación fondeada.",
        Icons.Default.SyncAlt
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Variables de estado
    var opcionSeleccionada by remember { mutableStateOf<String?>(null) }
    var ubicacion by rememberSaveable { mutableStateOf("") }

    val isFormValid = opcionSeleccionada != null && ubicacion.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Asistencia en Muelle",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Texto de introducción
            Text(
                text = "¿En qué podemos ayudarle hoy con su embarcación?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista vertical de opciones de asistencia
            opcionesAsistencia.forEach { opcion ->
                val isSelected = opcionSeleccionada == opcion.titulo
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { opcionSeleccionada = opcion.titulo },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color.White.copy(alpha = 0.1f) else Navy900
                    ),
                    border = if (isSelected) BorderStroke(2.dp, Gold500) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = opcion.icono,
                            contentDescription = opcion.titulo,
                            tint = Gold500,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = opcion.titulo,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = opcion.descripcion,
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Ubicación
            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación / Rada actual") },
                placeholder = { Text("Ej. Muelle Principal, Rada R-12", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Gold500,
                    focusedLabelColor = Gold500,
                    unfocusedLabelColor = Color.LightGray,
                    focusedLeadingIconColor = Gold500,
                    unfocusedLeadingIconColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botón de Alerta a Capitanía
            Button(
                onClick = {
                    Toast.makeText(context, "Marinero asignado y en camino", Toast.LENGTH_LONG).show()
                    onBackClick()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp), // Más alto para que se vea como un botón de emergencia
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC62828), // Rojo oscuro para alerta
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Enviar Alerta a Capitanía",
                    color = if (isFormValid) Color.White else Color.LightGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
