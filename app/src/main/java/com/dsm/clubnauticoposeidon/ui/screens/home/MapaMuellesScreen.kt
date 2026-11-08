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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

/**
 * modoSeleccion = true  -> viene de "Nueva Asignación": solo se elige una rada disponible.
 * modoSeleccion = false -> viene del panel del naviero: se gestiona el estado de cada rada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaMuellesScreen(
    onBackClick: () -> Unit,
    modoSeleccion: Boolean = false,
    onRadaSelected: (String) -> Unit = {},
    viewModel: MapaMuellesViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()

    var radaGestionada by remember { mutableStateOf<Rada?>(null) }
    var confirmarLiberar by remember { mutableStateOf<Rada?>(null) }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mensajeMostrado()
        }
    }

    // ---------- Opciones de la rada (modo gestión) ----------
    radaGestionada?.let { rada ->
        val codigo = rada.codigo.ifBlank { rada.id }
        AlertDialog(
            onDismissRequest = { radaGestionada = null },
            containerColor = Navy900,
            title = { Text("Rada $codigo", color = Gold500, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = when (rada.estado) {
                        EstadoRada.DISPONIBLE -> "Estado: disponible"
                        EstadoRada.MANTENIMIENTO -> "Estado: en mantenimiento"
                        else -> "Estado: ocupada"
                    },
                    color = Ink
                )
            },
            confirmButton = {
                when (rada.estado) {
                    EstadoRada.DISPONIBLE -> Button(
                        onClick = { viewModel.marcarMantenimiento(rada.id); radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500)
                    ) { Text("Pasar a mantenimiento", color = Navy900, fontWeight = FontWeight.Bold) }

                    EstadoRada.MANTENIMIENTO -> Button(
                        onClick = { viewModel.marcarDisponible(rada.id); radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4EBA6F))
                    ) { Text("Marcar disponible", color = Color.White, fontWeight = FontWeight.Bold) }

                    else -> Button(
                        onClick = { confirmarLiberar = rada; radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) { Text("Liberar rada", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            },
            dismissButton = {
                TextButton(onClick = { radaGestionada = null }) { Text("Cerrar", color = Color.White) }
            }
        )
    }

    // ---------- Confirmación para liberar ----------
    confirmarLiberar?.let { rada ->
        AlertDialog(
            onDismissRequest = { confirmarLiberar = null },
            containerColor = Navy900,
            title = { Text("¿Liberar rada?", color = Gold500, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se cerrará la asignación activa de la rada ${rada.codigo.ifBlank { rada.id }} y quedará disponible.",
                    color = Ink
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.liberar(rada.id); confirmarLiberar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold500)
                ) { Text("Liberar", color = Navy900, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLiberar = null }) { Text("Cancelar", color = Color.White) }
            }
        )
    }

    // 1. Contenedor Principal con Marca de Agua
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
    ) {
        // Marca de agua: Silueta/Ilustración superior derecha
        Icon(
            imageVector = Icons.Default.DirectionsBoat,
            contentDescription = null,
            tint = Ink.copy(alpha = 0.06f),
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-20).dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Anchor,
                                contentDescription = null,
                                tint = Gold500,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (modoSeleccion) "Elegir Rada" else "Mapa de Muelles",
                                color = Ink,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás",
                                tint = Gold500
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            Toast.makeText(context, "Sincronizando muelles...", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                tint = Gold500
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Subcabecera: Leyendas de Estado
                Surface(
                    color = Navy800,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Navy700),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Disponible (Verde)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4EBA6F))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disponible", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.width(10.dp))
                        Text("|", color = Muted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(10.dp))

                        // Ocupada (Rojo)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ocupada", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.width(10.dp))
                        Text("|", color = Muted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(10.dp))

                        // Mantenimiento (Dorado)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Gold500)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mantenimiento", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Cuadrícula de Radas (Grid 3 columnas)
                if (ui.cargando) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Gold500) }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(ui.radas, key = { it.id }) { rada ->
                            RadaItem(
                                codigo = rada.codigo.ifBlank { rada.id },
                                estado = rada.estado,
                                onClick = {
                                    if (modoSeleccion) {
                                        if (rada.estado == EstadoRada.DISPONIBLE) {
                                            onRadaSelected(rada.id)
                                        } else {
                                            Toast.makeText(context, "Rada no disponible", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        radaGestionada = rada
                                    }
                                }
                            )
                        }
                    }
                }

                // 4. Pie de Página (Banner Informativo)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Gold500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Selecciona una rada para auditar el estado del amarre.",
                            color = Muted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadaItem(
    codigo: String,
    estado: String,
    onClick: () -> Unit
) {
    val (bgColor, borderColor, badgeBgColor, badgeTextColor, labelTexto, icono) = when (estado) {
        EstadoRada.OCUPADA -> TupleEstilosRada(
            bgColor = Color(0xFF2A0A0A),
            borderColor = Color(0xFFE53935),
            badgeBgColor = Color(0xFF420F0F),
            badgeTextColor = Color(0xFFE53935),
            labelTexto = "Ocupada",
            icono = Icons.Default.DirectionsBoat
        )
        EstadoRada.MANTENIMIENTO -> TupleEstilosRada(
            bgColor = Color(0xFF251704),
            borderColor = Gold500,
            badgeBgColor = Color(0xFF3D270B),
            badgeTextColor = Gold500,
            labelTexto = "Mantenimiento",
            icono = Icons.Default.Build
        )
        else -> TupleEstilosRada(
            bgColor = Color(0xFF082115),
            borderColor = Color(0xFF4EBA6F),
            badgeBgColor = Color(0xFF133822),
            badgeTextColor = Color(0xFF4EBA6F),
            labelTexto = "Disponible",
            icono = Icons.Default.DirectionsBoat
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = codigo,
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Surface(
                color = badgeBgColor,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = labelTexto,
                    color = badgeTextColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private data class TupleEstilosRada(
    val bgColor: Color,
    val borderColor: Color,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val labelTexto: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector
)
