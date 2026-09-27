package com.dsm.clubnauticoposeidon.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSocioScreen(
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    // Fecha actual formateada
    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)
        LocalDate.now().format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Hola, Khalep", color = Gold500, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(currentDate, color = Color.LightGray, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900,
                    titleContentColor = Gold500,
                    actionIconContentColor = Gold500
                ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil del Socio",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        // Menú desplegable para opciones de perfil y cierre de sesión
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ver perfil", color = Navy900, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión", color = Color.Red, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    menuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = Navy900 // Fondo de toda la pantalla
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Clima y Mareas (HU05 Teaser)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { 
                        Toast.makeText(context, "Módulo de clima en el Sprint 2", Toast.LENGTH_SHORT).show() 
                    },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Clima",
                        tint = Gold500,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(text = "22°C Callao", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Marea: Baja | Bandera Verde", color = Color.LightGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Toca para ver detalles", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Rápido de Código QR (HU03)
            OutlinedButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = BorderStroke(1.dp, Gold500),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Mostrar mi Credencial QR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            val showToast = {
                Toast.makeText(context, "Módulo en desarrollo", Toast.LENGTH_SHORT).show()
            }

            // Cuadrícula de Servicios (Dashboard Grid)
            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardGridItem(
                    title = "Mis Naves",
                    icon = Icons.Default.DirectionsBoat,
                    onClick = showToast,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                DashboardGridItem(
                    title = "Servicios",
                    icon = Icons.Default.RoomService,
                    onClick = showToast,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                DashboardGridItem(
                    title = "Reservas",
                    icon = Icons.Default.Event,
                    onClick = showToast,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                DashboardGridItem(
                    title = "Reportar Daño",
                    icon = Icons.Default.Build,
                    onClick = showToast,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardGridItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Para hacerlo cuadrado
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Gold500,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
