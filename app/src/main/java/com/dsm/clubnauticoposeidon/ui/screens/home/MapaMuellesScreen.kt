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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBoat
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

private val ColorDisponible = Color(0xFF1B5E20)
private val ColorOcupada = Color(0xFFB71C1C)
private val ColorMantenimiento = Color(0xFFF9A825)

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
                    color = Color.White
                )
            },
            confirmButton = {
                when (rada.estado) {
                    EstadoRada.DISPONIBLE -> Button(
                        onClick = { viewModel.marcarMantenimiento(rada.id); radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorMantenimiento)
                    ) { Text("Pasar a mantenimiento", color = Navy900) }

                    EstadoRada.MANTENIMIENTO -> Button(
                        onClick = { viewModel.marcarDisponible(rada.id); radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorDisponible)
                    ) { Text("Marcar disponible", color = Color.White) }

                    else -> Button(
                        onClick = { confirmarLiberar = rada; radaGestionada = null },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorOcupada)
                    ) { Text("Liberar rada", color = Color.White) }
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
                    color = Color.White
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (modoSeleccion) "Elegir Rada" else "Mapa de Muelles",
                        color = Gold500,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (ui.cargando) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Gold500) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Leyenda(ColorDisponible, "Disponible")
                Spacer(modifier = Modifier.width(20.dp))
                Leyenda(ColorOcupada, "Ocupada")
                Spacer(modifier = Modifier.width(20.dp))
                Leyenda(ColorMantenimiento, "Mantenimiento")
            }
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun RadaItem(
    codigo: String,
    estado: String,
    onClick: () -> Unit
) {
    val bgColor = when (estado) {
        EstadoRada.OCUPADA -> ColorOcupada
        EstadoRada.MANTENIMIENTO -> ColorMantenimiento
        else -> ColorDisponible
    }
    val borderColor = when (estado) {
        EstadoRada.OCUPADA -> Color(0xFFE57373)
        EstadoRada.MANTENIMIENTO -> Color(0xFFFFE082)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (estado) {
                EstadoRada.OCUPADA -> Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = "Barco ocupando rada",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                EstadoRada.MANTENIMIENTO -> Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Rada en mantenimiento",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                else -> Text(
                    text = codigo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}