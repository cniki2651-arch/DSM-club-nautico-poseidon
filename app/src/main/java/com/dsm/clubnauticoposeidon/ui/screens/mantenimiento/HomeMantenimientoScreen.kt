package com.dsm.clubnauticoposeidon.ui.screens.mantenimiento

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.screens.home.ContadorRadas
import com.dsm.clubnauticoposeidon.ui.screens.home.MenuCardNaviero
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
fun HomeMantenimientoScreen(
    onLogout: () -> Unit,
    onNavigateToPedidos: () -> Unit = {},
    viewModel: PedidosPendientesViewModel = viewModel()
) {
    val context = LocalContext.current
    val pedidos by viewModel.pedidos.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    val currentDate = remember {
        val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val formatter = DateTimeFormatter.ofPattern("'Hoy,' d 'de' MMMM", localeEs)
        LocalDate.now().format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }

    val catering = pedidos.count { it.modulo == "catering" }
    val limpieza = pedidos.count { it.modulo == "limpieza" }
    val suministros = pedidos.count { it.modulo == "suministros" }
    val asistencia = pedidos.count { it.modulo == "asistencia" }

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

        Scaffold(containerColor = Color.Transparent) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Cabecera
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
                            text = "Panel de Mantenimiento",
                            color = Gold500,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Perfil",
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

                Text(
                    text = "Bienvenido, Encargado",
                    color = Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = currentDate, color = Muted, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Atención de Servicios",
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

                Spacer(modifier = Modifier.height(20.dp))

                // Resumen de pendientes por módulo
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
                            text = "Pedidos por atender: ${pedidos.size}",
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
                            ContadorRadas(catering.toString(), "Catering", Gold500)
                            ContadorRadas(limpieza.toString(), "Limpieza", Ink)
                            ContadorRadas(suministros.toString(), "Sumin.", Muted)
                            // La asistencia se resalta en rojo cuando hay algo pendiente
                            ContadorRadas(
                                asistencia.toString(),
                                "Asistencia",
                                if (asistencia > 0) Color(0xFFE57373) else Muted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val showToast = {
                    Toast.makeText(context, "Módulo en desarrollo", Toast.LENGTH_SHORT).show()
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    item {
                        MenuCardNaviero(
                            titulo = "Pedidos por atender",
                            descripcion = "Catering, limpieza, suministros y asistencia en un solo lugar",
                            icono = Icons.Default.Inventory2,
                            destacado = true,
                            onClick = onNavigateToPedidos
                        )
                    }
                    item {
                        MenuCardNaviero(
                            titulo = "Reportes de Daños",
                            descripcion = "Registra incidencias con fotos",
                            icono = Icons.Default.Build,
                            destacado = false,
                            onClick = showToast
                        )
                    }
                }

                // Pie de página
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
