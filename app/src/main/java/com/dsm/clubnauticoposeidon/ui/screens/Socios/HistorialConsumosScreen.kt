package com.dsm.clubnauticoposeidon.ui.screens.Socios

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialConsumosScreen(
    onBackClick: () -> Unit,
    viewModel: HistorialConsumosViewModel = viewModel()
) {
    val pedidos by viewModel.pedidos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val error by viewModel.error.collectAsState()

    val filtros = listOf("todos", "pendiente", "entregado", "facturado")
    var filtro by remember { mutableStateOf("todos") }

    val filtrados = remember(pedidos, filtro) {
        if (filtro == "todos") pedidos
        else pedidos.filter { it.estado.equals(filtro, ignoreCase = true) }
    }
    val total = filtrados.sumOf { it.total }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de consumos",
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
            // Resumen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Gold500.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Total consumido", color = Color.LightGray, fontSize = 13.sp)
                        Text(
                            text = "S/ ${String.format("%.2f", total)}",
                            color = Gold500,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (filtrados.size == 1) "1 pedido" else "${filtrados.size} pedidos",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Filtros por estado
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { estado ->
                    val seleccionado = estado == filtro
                    OutlinedButton(
                        onClick = { filtro = estado },
                        border = BorderStroke(1.dp, if (seleccionado) Gold500 else Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (seleccionado) Gold500.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (seleccionado) Gold500 else Color.LightGray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = estado.replaceFirstChar { it.uppercase() },
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
                error != null && pedidos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error.orEmpty(), color = Color.LightGray, fontSize = 15.sp)
                    }
                }
                filtrados.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (pedidos.isEmpty()) "Aún no tienes consumos registrados"
                            else "No hay pedidos con este estado",
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
                        items(filtrados, key = { it.id }) { pedido ->
                            PedidoCard(pedido)
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoCard(pedido: PedidoHistorial) {
    var expandido by remember { mutableStateOf(false) }
    val formato = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = iconoModulo(pedido.modulo),
                    contentDescription = null,
                    tint = Gold500,
                    modifier = Modifier.size(36.dp)
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
                        text = if (pedido.fecha > 0) formato.format(Date(pedido.fecha)) else "Sin fecha",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "S/ ${String.format("%.2f", pedido.total)}",
                        color = Gold500,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.estado.replaceFirstChar { it.uppercase() },
                        color = colorEstado(pedido.estado),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pedido guardado en el celular que todavía no llegó al club
            if (pedido.pendienteDeEnvio) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pendiente de envío: se enviará cuando haya conexión",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Detalle del pedido (al tocar la tarjeta)
            if (expandido) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (pedido.items.isEmpty()) {
                    Text(
                        text = "Sin detalle de productos",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                } else {
                    pedido.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.cantidad} x ${item.nombre}",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "S/ ${String.format("%.2f", item.cantidad * item.precioUnitario)}",
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (pedido.ubicacion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Entrega: ${pedido.ubicacion}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }
            }
        }
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

private fun colorEstado(estado: String): Color = when (estado.lowercase()) {
    "entregado" -> Color(0xFF66BB6A)
    "facturado" -> Color(0xFF4FC3F7)
    else -> Gold500
}
