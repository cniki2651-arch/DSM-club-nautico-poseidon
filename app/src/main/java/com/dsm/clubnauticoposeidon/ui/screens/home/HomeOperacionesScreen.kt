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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.dsm.clubnauticoposeidon.ui.components.DashboardRadas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeOperacionesScreen(
    onLogout: () -> Unit,
    onNavigateToAsignarRada: () -> Unit = {},
    onNavigateToMapa: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {},
    onNavigateToSolicitudes: () -> Unit = {},
    onNavigateToAutorizarZarpes: () -> Unit = {},
    viewModel: HomeOperacionesViewModel = viewModel()
) {
    val context = LocalContext.current
    val resumen by viewModel.resumen.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    // Cálculo de la fecha actual
    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("'Hoy,' d 'de' MMMM", localeEs)
        LocalDate.now().format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    // Datos del dashboard: mismos colores que los contadores del resumen
    val enServicio = resumen.ocupadas + resumen.disponibles   // mantenimiento no se puede usar
    val porcentajeOcupacion = if (enServicio == 0) 0 else resumen.ocupadas * 100 / enServicio
    val datosRadas = listOf(
        DatoGrafico("Ocupadas", resumen.ocupadas, Gold500),
        DatoGrafico("Disponibles", resumen.disponibles, Color(0xFF4CAF50)),
        DatoGrafico("Mantenimiento", resumen.mantenimiento, Muted)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
    ) {
        // Marca de agua
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
                // Cabecera (fija)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Anchor,
                            contentDescription = "Logo Club",
                            tint = Gold500,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Panel del Naviero",
                            color = Gold500,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil Naviero",
                                tint = Ink,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
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

                Spacer(modifier = Modifier.height(20.dp))

                val showToast = {
                    Toast.makeText(context, "Módulo en desarrollo", Toast.LENGTH_SHORT).show()
                }
                val anchoCompleto: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

                // Todo el contenido se desplaza dentro del grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Saludo y título
                    item(span = anchoCompleto) {
                        Column {
                            Text(
                                text = "Bienvenido, Naviero",
                                color = Ink,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = currentDate, color = Muted, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Panel de Control Marítimo",
                                color = Ink,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(3.dp)
                                    .background(Gold500, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    }

                    // Tarjeta de Resumen de Muelle
                    item(span = anchoCompleto) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Navy800),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Navy700)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Estado del Muelle Principal",
                                    color = Ink,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ContadorRadas(resumen.totales.toString(), "Totales", Ink)
                                    ContadorRadas(resumen.ocupadas.toString(), "Ocupadas", Gold500)
                                    ContadorRadas(resumen.disponibles.toString(), "Disponibles", Color(0xFF4CAF50))
                                    ContadorRadas(resumen.mantenimiento.toString(), "Mant.", Muted)
                                }
                            }
                        }
                    }

                    // Acciones rápidas (Radas)
                    item(span = anchoCompleto) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onNavigateToAsignarRada,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Asignar Nueva Rada",
                                    color = Navy900,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onNavigateToSolicitudes,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                border = BorderStroke(1.dp, Gold500),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Solicitudes de Rada",
                                    color = Gold500,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Tarjetas de opciones principales (2 columnas)
                    item {
                        MenuCardNaviero(
                            titulo = "Mapa de Muelles",
                            descripcion = "Monitorea la ocupación de radas en tiempo real",
                            icono = Icons.Default.Map,
                            destacado = true,
                            onClick = onNavigateToMapa
                        )
                    }
                    item {
                        MenuCardNaviero(
                            titulo = "Autorizar Zarpes",
                            descripcion = "Evalúa solicitudes de salida y valida la tripulación especializada",
                            icono = Icons.Default.DirectionsBoat,
                            destacado = true,
                            onClick = onNavigateToAutorizarZarpes
                        )
                    }
                    item {
                        MenuCardNaviero(
                            titulo = "Reportes de Daños",
                            descripcion = "Gestiona incidencias en embarcaciones",
                            icono = Icons.Default.Build,
                            destacado = false,
                            onClick = showToast
                        )
                    }
                    item {
                        MenuCardNaviero(
                            titulo = "Historial de Tránsito",
                            descripcion = "Registro de entradas, salidas y bitácora",
                            icono = Icons.Default.History,
                            destacado = false,
                            onClick = onNavigateToHistorial
                        )
                    }

                    // Dashboard plegable (cerrado al inicio, no estorba)
                    item(span = anchoCompleto) {
                        TarjetaGraficoPlegable(
                            titulo = "Estadísticas de radas",
                            subtitulo = "Estado actual del muelle"
                        ) {
                            DonaPedidos(
                                datos = datosRadas,
                                valorCentro = "$porcentajeOcupacion%",
                                etiquetaCentro = "ocupación"
                            )
                        }
                    }
                }

                // Pie de página (fijo)
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