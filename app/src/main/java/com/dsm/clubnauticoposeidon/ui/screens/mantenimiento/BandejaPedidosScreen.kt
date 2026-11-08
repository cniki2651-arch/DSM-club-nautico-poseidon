package com.dsm.clubnauticoposeidon.ui.screens.mantenimiento

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorEmergencia = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaPedidosScreen(
    onBackClick: () -> Unit,
    viewModel: PedidosPendientesViewModel = viewModel()
) {
    val context = LocalContext.current
    val pedidos by viewModel.pedidos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()

    val filtros = listOf("todos", "asistencia", "catering", "limpieza", "suministros")
    var filtro by remember { mutableStateOf("todos") }
    var pedidoPorEntregar by remember { mutableStateOf<PedidoPendiente?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarError()
        }
    }

    // Primero las asistencias (pueden ser emergencias) y luego los más antiguos
    val visibles = remember(pedidos, filtro) {
        pedidos
            .filter { filtro == "todos" || it.modulo == filtro }
            .sortedWith(
                compareByDescending<PedidoPendiente> { it.modulo == "asistencia" }
                    .thenBy { it.fecha }
            )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pedidos por atender",
                        color = Gold500,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
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
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { clave ->
                    val seleccionado = clave == filtro
                    val cantidad = if (clave == "todos") pedidos.size else pedidos.count { it.modulo == clave }
                    OutlinedButton(
                        onClick = { filtro = clave },
                        border = BorderStroke(1.dp, if (seleccionado) Gold500 else Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (seleccionado) Gold500.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (seleccionado) Gold500 else Color.LightGray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${clave.replaceFirstChar { it.uppercase() }} ($cantidad)",
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            when {
                cargando && pedidos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Gold500)
                    }
                }
                visibles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay pedidos pendientes",
                            color = Color.LightGray,
                            fontSize = 15.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibles, key = { it.id }) { pedido ->
                            PedidoPendienteCard(
                                pedido = pedido,
                                onMarcarEntregado = { pedidoPorEntregar = pedido }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    // Confirmación para evitar toques accidentales
    pedidoPorEntregar?.let { pedido ->
        AlertDialog(
            onDismissRequest = { pedidoPorEntregar = null },
            containerColor = Navy800,
            titleContentColor = Ink,
            textContentColor = Color.LightGray,
            title = { Text("¿Marcar como entregado?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${nombreModulo(pedido.modulo)}\n" +
                            if (pedido.ubicacion.isNotBlank()) pedido.ubicacion else "Sin ubicación"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.marcarEntregado(pedido.id)
                    pedidoPorEntregar = null
                }) {
                    Text("Sí, entregado", color = Gold500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pedidoPorEntregar = null }) {
                    Text("Cancelar", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
private fun PedidoPendienteCard(
    pedido: PedidoPendiente,
    onMarcarEntregado: () -> Unit
) {
    val formato = remember { SimpleDateFormat("dd/MM HH:mm", Locale("es", "PE")) }
    val esAsistencia = pedido.modulo == "asistencia"
    val acento = if (esAsistencia) ColorEmergencia else Gold500

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = if (esAsistencia) BorderStroke(1.dp, ColorEmergencia) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = iconoModulo(pedido.modulo),
                    contentDescription = null,
                    tint = acento,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreModulo(pedido.modulo),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (pedido.fecha > 0) {
                            "${formato.format(Date(pedido.fecha))} · ${hace(pedido.fecha)}"
                        } else {
                            "Sin fecha"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
                if (pedido.total > 0) {
                    Text(
                        text = "S/ ${String.format("%.2f", pedido.total)}",
                        color = Gold500,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dónde hay que llevarlo: lo más importante para quien entrega
            Text(text = "ENTREGAR EN", color = Muted, fontSize = 11.sp)
            Text(
                text = if (pedido.ubicacion.isNotBlank()) pedido.ubicacion else "Sin ubicación indicada",
                color = Ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (pedido.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                pedido.items.forEach { item ->
                    Text(
                        text = "${item.cantidad} x ${item.nombre}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            if (pedido.nota.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = pedido.nota, color = Color.LightGray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onMarcarEntregado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Marcar entregado",
                    color = Navy900,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun hace(fecha: Long): String {
    val minutos = (System.currentTimeMillis() - fecha) / 60_000
    return when {
        minutos < 1 -> "ahora"
        minutos < 60 -> "hace $minutos min"
        minutos < 1440 -> "hace ${minutos / 60} h"
        else -> "hace ${minutos / 1440} d"
    }
}

private fun nombreModulo(modulo: String): String = when (modulo) {
    "catering" -> "Catering y Restaurante"
    "limpieza" -> "Limpieza y Aseo"
    "suministros" -> "Suministros Básicos"
    "asistencia" -> "Asistencia en Muelle"
    else -> modulo.replaceFirstChar { it.uppercase() }
}

private fun iconoModulo(modulo: String): ImageVector = when (modulo) {
    "catering" -> Icons.Default.Restaurant
    "limpieza" -> Icons.Default.CleaningServices
    "suministros" -> Icons.Default.WaterDrop
    "asistencia" -> Icons.Default.SupportAgent
    else -> Icons.Default.ShoppingCart
}
