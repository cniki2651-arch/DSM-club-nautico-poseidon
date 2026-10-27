package com.dsm.clubnauticoposeidon.ui.screens.secretaria

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeSecretariaScreen(
    viewModel: HomeSecretariaViewModel = viewModel(),
    onNavigateToSolicitudes: () -> Unit,
    onLogout: () -> Unit
) {
    val nombreSecretaria by viewModel.nombreSecretaria.collectAsState()
    val solicitudesPendientes by viewModel.solicitudesPendientes.collectAsState()

    // Cálculo de la fecha actual formateada
    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("'Hoy,' d 'de' MMMM", localeEs)
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
                // 2. Cabecera (Header)
                // Fila superior: Título del Portal + Cierre de sesión
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Anchor,
                            contentDescription = "Logo Club",
                            tint = Gold500,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Portal de Secretaría",
                            color = Gold500,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.cerrarSesion()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = Ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Textos apilados
                Text(
                    text = "Bienvenida, $nombreSecretaria",
                    color = Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentDate,
                    color = Muted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Panel de Gestión",
                    color = Ink,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pequeña línea dorada debajo del título
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(3.dp)
                        .background(Gold500, shape = RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Banner de Notificaciones
                if (solicitudesPendientes > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Navy800
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Alerta de Solicitudes",
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
                                color = Ink,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 4. Cuadrícula de Opciones (Grid de 2 columnas)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Tarjeta 1 (Destacada): Verificación de Solicitudes
                    item {
                        MenuCardSecretaria(
                            titulo = "Verificación de Solicitudes",
                            descripcion = "Evalúa y clasifica nuevos postulantes",
                            icono = Icons.Default.AssignmentInd,
                            destacado = true,
                            badgeCount = solicitudesPendientes,
                            onClick = onNavigateToSolicitudes
                        )
                    }

                    // Tarjeta 2: Gestión de Reclamos
                    item {
                        MenuCardSecretaria(
                            titulo = "Gestión de Reclamos",
                            descripcion = "Atiende consultas y reportes de socios",
                            icono = Icons.Default.SupportAgent,
                            destacado = false,
                            onClick = { /* TODO: HU Mesa de Ayuda */ }
                        )
                    }

                    // Tarjeta 3: Bajas y Liquidaciones
                    item {
                        MenuCardSecretaria(
                            titulo = "Bajas y Liquidaciones",
                            descripcion = "Procesa retiros y estados de cuenta",
                            icono = Icons.Default.MonetizationOn,
                            destacado = false,
                            onClick = { /* TODO: HU Liquidaciones */ }
                        )
                    }

                    // Tarjeta 4: Auditoría de Socios
                    item {
                        MenuCardSecretaria(
                            titulo = "Auditoría de Socios",
                            descripcion = "Consulta el historial de miembros",
                            icono = Icons.AutoMirrored.Filled.ManageSearch,
                            destacado = false,
                            onClick = { /* TODO: HU Buscador general */ }
                        )
                    }
                }

                // 5. Pie de Página (Footer)
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
                            imageVector = Icons.Default.DirectionsBoat,
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
 * Componente reutilizable MenuCard para la cuadrícula del Panel de Secretaría.
 */
@Composable
fun MenuCardSecretaria(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    destacado: Boolean = false,
    badgeCount: Int? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Navy800
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (destacado) {
            BorderStroke(1.5.dp, Gold500)
        } else {
            BorderStroke(1.dp, Navy700)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Franja lateral dorada para la tarjeta destacada
            if (destacado) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxSize()
                        .background(Gold500)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Fila Superior: Contenedor del ícono + Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (destacado) Gold500.copy(alpha = 0.15f) else Navy700),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = titulo,
                            tint = if (destacado) Gold500 else Muted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (badgeCount != null && badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFE57373),
                                    contentColor = Color.White
                                ) {
                                    Text("$badgeCount")
                                }
                            }
                        ) {
                            Spacer(modifier = Modifier.size(10.dp))
                        }
                    }
                }

                // Centro: Título y Descripción
                Column {
                    Text(
                        text = titulo,
                        color = Ink,
                        fontSize = 14.sp,
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

                // Parte inferior: Flechita indicadora de navegación
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Ir a $titulo",
                    tint = if (destacado) Gold500 else Muted,
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(20.dp)
                )
            }
        }
    }
}
