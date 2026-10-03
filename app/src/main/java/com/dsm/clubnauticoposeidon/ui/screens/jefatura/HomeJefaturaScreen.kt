package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import android.util.Log
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
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeJefaturaScreen(
    onNavigateToAprobaciones: () -> Unit,
    onLogout: () -> Unit
) {
    // Estado para nombre del jefe
    var nombreJefe by remember { mutableStateOf("Cargando...") }

    // Estado para notificaciones en tiempo real
    var aprobacionesPendientes by remember { mutableIntStateOf(0) }

    // Listener de Firestore
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Obtener nombre del jefe
        if (uid != null) {
            db.collection("usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombre = document.getString("nombre")
                        if (!nombre.isNullOrBlank()) {
                            nombreJefe = nombre
                        } else {
                            nombreJefe = "Jefatura"
                        }
                    } else {
                        nombreJefe = "Jefatura"
                    }
                }
                .addOnFailureListener {
                    nombreJefe = "Jefatura"
                    Log.e("HomeJefatura", "Error al obtener el nombre del jefe", it)
                }
        } else {
            nombreJefe = "Jefatura"
        }

        val listener = db.collection("usuarios")
            .whereEqualTo("rol", "postulante")
            .whereEqualTo("estado", "revisado_secretaria")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeJefatura", "Error al obtener solicitudes pendientes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    aprobacionesPendientes = snapshot.size()
                }
            }
        onDispose {
            listener.remove()
        }
    }

    // Cálculo de la fecha actual
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
                        text = "Portal de Jefatura",
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

            // Cabecera: Saludo (Limpia sin avatar)
            Column {
                Text(
                    text = "Hola, $nombreJefe",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = currentDate,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Banner de Estado Rápido Dinámico
            if (aprobacionesPendientes > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Gold500)
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val textoBanner = if (aprobacionesPendientes == 1) {
                            "Tienes 1 solicitud esperando aprobación final"
                        } else {
                            "Tienes $aprobacionesPendientes solicitudes esperando aprobación final"
                        }
                        
                        Text(textoBanner, color = Color.White)
                    }
                }
            }

            // Grid de módulos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Módulo 1: Aprobación de Nuevos Socios (con Badge)
                item {
                    DashboardCard(
                        titulo = "Aprobación de\nNuevos Socios",
                        icono = Icons.Default.VerifiedUser,
                        colorActivo = Gold500,
                        badgeCount = aprobacionesPendientes,
                        onClick = onNavigateToAprobaciones
                    )
                }

                // Módulo 2: Fichas de Embarcación (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Fichas de\nEmbarcación",
                        icono = Icons.Default.DirectionsBoat,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: HU Entrega de Fichas */ }
                    )
                }

                // Módulo 3: Conformidad de Liquidaciones (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Conformidad de\nLiquidaciones",
                        icono = Icons.Default.FactCheck,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: HU Bajas Definitivas */ }
                    )
                }

                // Módulo 4: Reportes de Área (Próximamente)
                item {
                    DashboardCard(
                        titulo = "Reportes de\nÁrea",
                        icono = Icons.Default.QueryStats,
                        colorActivo = Color.Gray,
                        onClick = { /* TODO: Estadísticas */ }
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
        border = if (colorActivo == Gold500) {
            BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
        } else {
            BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        }
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
