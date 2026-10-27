package com.dsm.clubnauticoposeidon.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.screens.clima.ClimaViewModel
import com.dsm.clubnauticoposeidon.ui.screens.profile.FotoPerfilViewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSocioScreen(
    viewModel: HomeViewModel = viewModel(),
    climaViewModel: ClimaViewModel = viewModel(),
    fotoViewModel: FotoPerfilViewModel = viewModel(),
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToServicios: () -> Unit = {},
    onNavigateToMisNaves: () -> Unit = {},
    onNavigateToClima: () -> Unit = {}
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val climaDetalle by climaViewModel.clima.collectAsState()
    val climaCargando by climaViewModel.cargando.collectAsState()
    val fotoUi by fotoViewModel.ui.collectAsState()

    val nombreSocio by viewModel.nombreSocio.collectAsState()

    // Fecha actual formateada
    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", localeEs)
        LocalDate.now().format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    // 1. Fondo principal con Marca de Agua
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
    ) {
        // Marca de agua: Ícono gigante de ancla en la esquina superior derecha
        Icon(
            imageVector = Icons.Default.Anchor,
            contentDescription = null,
            tint = Ink.copy(alpha = 0.05f),
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-30).dp)
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // 2. Cabecera (Header con SpaceBetween)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, $nombreSocio",
                            color = Ink,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentDate,
                            color = Muted,
                            fontSize = 14.sp
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            val foto = fotoUi.foto
                            if (foto != null) {
                                Image(
                                    bitmap = foto.asImageBitmap(),
                                    contentDescription = "Perfil del Socio",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Gold500, CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Perfil del Socio",
                                    tint = Gold500,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
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
                                    viewModel.cerrarSesion()
                                    onLogout()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Bloque de Clima (Weather Card - Mantiene 100% la lógica de la API)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToClima() },
                    colors = CardDefaults.cardColors(
                        containerColor = Navy800
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (climaCargando) {
                            CircularProgressIndicator(
                                color = Gold500,
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gold500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Clima",
                                    tint = Gold500,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            val textoTemp = if (climaDetalle != null) {
                                "${climaDetalle!!.temperatura.toInt()}°C Callao"
                            } else {
                                "-- °C Callao"
                            }
                            Text(
                                text = textoTemp,
                                color = Ink,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            val textoMar = if (climaDetalle != null) {
                                val alturaOlas = climaDetalle!!.alturaOlas
                                val viento = climaDetalle!!.velocidadViento
                                val bandera = when {
                                    viento < 20.0 && (alturaOlas ?: 0.0) < 1.5 -> "Bandera Verde"
                                    viento < 30.0 && (alturaOlas ?: 0.0) < 2.5 -> "Bandera Amarilla"
                                    else -> "Bandera Roja"
                                }
                                "Oleaje: ${String.format("%.1f", alturaOlas ?: 0.0)} m | $bandera"
                            } else {
                                "Cargando datos del mar…"
                            }
                            Text(
                                text = textoMar,
                                color = Muted,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Toca para ver detalles ›",
                                color = Gold400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Botón Elegante de Credencial QR (Ancho completo)
                OutlinedButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    border = BorderStroke(1.dp, Gold500),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = "QR",
                        tint = Gold500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mostrar mi Credencial QR",
                        color = Gold500,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val showToast = {
                    Toast.makeText(context, "Módulo en desarrollo", Toast.LENGTH_SHORT).show()
                }

                // 5. Cuadrícula de Opciones (Grid de 2 columnas)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Tarjeta 1: Mis Naves
                    item {
                        MenuCardSocio(
                            titulo = "Mis Naves",
                            descripcion = "Gestiona tus embarcaciones",
                            icono = Icons.Default.DirectionsBoat,
                            onClick = onNavigateToMisNaves
                        )
                    }

                    // Tarjeta 2: Servicios
                    item {
                        MenuCardSocio(
                            titulo = "Servicios",
                            descripcion = "Solicita servicios del club",
                            icono = Icons.Default.RoomService,
                            onClick = onNavigateToServicios
                        )
                    }

                    // Tarjeta 3: Reservas
                    item {
                        MenuCardSocio(
                            titulo = "Reservas",
                            descripcion = "Programa tus visitas",
                            icono = Icons.Default.Event,
                            onClick = showToast
                        )
                    }

                    // Tarjeta 4: Reportar Daño
                    item {
                        MenuCardSocio(
                            titulo = "Reportar Daño",
                            descripcion = "Notifica incidencias",
                            icono = Icons.Default.Build,
                            onClick = showToast
                        )
                    }
                }

                // 6. Pie de Página (Footer)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalDivider(
                        color = Navy700,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Anchor,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Juntos por un mejor club",
                            color = Muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente reutilizable MenuCard para la cuadrícula del Portal del Socio.
 */
@Composable
fun MenuCardSocio(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Navy800
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Navy700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícono en contenedor pequeño
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = Gold500,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Título y Descripción
            Column {
                Text(
                    text = titulo,
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    color = Muted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            // Flechita indicadora de navegación
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ir a $titulo",
                tint = Muted,
                modifier = Modifier
                    .align(Alignment.End)
                    .size(20.dp)
            )
        }
    }
}
