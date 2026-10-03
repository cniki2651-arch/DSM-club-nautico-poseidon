package com.dsm.clubnauticoposeidon.ui.screens.secretaria

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.firestore.FirebaseFirestore
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSecretariaScreen(
    onNavigateToSolicitudes: () -> Unit,
    onLogout: () -> Unit
) {
    var solicitudesPendientes by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeSecretaria", "Error al obtener solicitudes pendientes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    solicitudesPendientes = snapshot.size()
                }
            }
        onDispose {
            listener.remove()
        }
    }

    // Calculo de la fecha actual
    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("'Hoy,' d 'de' MMMM", localeEs)
        LocalDate.now().format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Portal de Secretaría",
                        color = Gold500,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabecera: Saludo
            Text(
                text = "Bienvenida,",
                color = Color.LightGray,
                fontSize = 18.sp
            )
            Text(
                text = currentDate,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Panel de Gestión",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Banner de Estado Rápido Dinámico
            if (solicitudesPendientes > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerta",
                            tint = Gold500,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        val textoBanner = if (solicitudesPendientes == 1) {
                            "Tienes 1 solicitud nueva por revisar"
                        } else {
                            "Tienes $solicitudesPendientes solicitudes nuevas por revisar"
                        }
                        
                        Text(
                            text = textoBanner,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Grid de módulos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Módulo 1: Verificación de Solicitudes (con Badge)
                item {
                    DashboardCard(
                        titulo = "Verificación de\nSolicitudes",
                        icono = Icons.Default.AssignmentInd,
                        colorActivo = Gold500,
                        badgeCount = solicitudesPendientes, // Valor en tiempo real
                        onClick = onNavigateToSolicitudes
                    )
                }

                // Módulo 2: Gestión de Reclamos (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Gestión de\nReclamos",
                        icono = Icons.Default.SupportAgent,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: HU Mesa de Ayuda */ }
                    )
                }

                // Módulo 3: Bajas y Liquidaciones (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Bajas y\nLiquidaciones",
                        icono = Icons.Default.MonetizationOn,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: HU Liquidaciones */ }
                    )
                }

                // Módulo 4: Auditoría de Socios (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Auditoría\nde Socios",
                        icono = Icons.AutoMirrored.Filled.ManageSearch,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: HU Buscador general */ }
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    titulo: String,
    icono: ImageVector,
    colorActivo: Color,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Lo hace completamente cuadrado
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // Borde muy sutil
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // BadgedBox para notificaciones
            BadgedBox(
                badge = {
                    if (badgeCount != null && badgeCount > 0) {
                        Badge(
                            containerColor = Color(0xFFC62828), // Rojo oscuro/alerta
                            contentColor = Color.White
                        ) {
                            Text("$badgeCount")
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorActivo,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = titulo,
                color = if (colorActivo == Color.Gray) Color.Gray else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
