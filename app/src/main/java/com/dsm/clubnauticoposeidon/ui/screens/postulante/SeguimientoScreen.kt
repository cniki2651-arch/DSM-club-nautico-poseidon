package com.dsm.clubnauticoposeidon.ui.screens.postulante

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoScreen(
    auth: FirebaseAuth,
    onSignOut: () -> Unit,
    onNavigateToHomeSocio: () -> Unit = {}
) {
    val context = LocalContext.current
    var estadoActual by remember { mutableStateOf("Cargando...") }
    var motivoRechazo by remember { mutableStateOf("") }
    val uid = auth.currentUser?.uid

    // Escucha en tiempo real del documento del postulante
    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose {}

        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al sincronizar datos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    estadoActual = snapshot.getString("estado") ?: "desconocido"
                    motivoRechazo = snapshot.getString("motivo_rechazo") ?: ""
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
                        text = "Estado de tu Solicitud",
                        color = Gold500,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onSignOut()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
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
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                when (estadoActual) {
                    "Cargando..." -> {
                        CircularProgressIndicator(color = Gold500, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Obteniendo información...",
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    "pendiente", "revisado_secretaria" -> {
                        CircularProgressIndicator(color = Gold500, modifier = Modifier.size(60.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tu solicitud está en evaluación. Por favor, espera.",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Recibirás una notificación cuando la directiva apruebe tu membresía.",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    "aprobado" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Aprobado",
                            tint = Color(0xFF4CAF50), // Verde brillante
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "¡Felicidades! Has sido aceptado.",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ya puedes disfrutar de todos los beneficios y módulos del Club Poseidón.",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { onNavigateToHomeSocio() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Ir al Portal de Socios", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    "rechazado" -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Observado",
                            tint = Color(0xFFFF9800), // Naranja / Ámbar de advertencia
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tu solicitud ha sido observada",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.15f) // Rojo transparente
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Motivo del rechazo:",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = motivoRechazo.ifBlank { "No se especificó un motivo." },
                                    color = Color.LightGray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                if (uid != null) {
                                    FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                                        .update(
                                            mapOf(
                                                "estado" to "pendiente",
                                                "motivo_rechazo" to ""
                                            )
                                        )
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Solicitud reenviada con éxito", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al reenviar la solicitud", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Subsanar y Volver a Enviar", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    else -> {
                        // Fallback de seguridad
                        Text(
                            text = "Estado desconocido.",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
