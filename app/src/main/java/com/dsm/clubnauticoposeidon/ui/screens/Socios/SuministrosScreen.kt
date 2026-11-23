package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.components.SelectorEmbarcacion
import com.dsm.clubnauticoposeidon.ui.components.rememberEstadoEmbarcacion
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// Pantalla de Suministros Básicos: usa la misma lógica que Catering (ServicioViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuministrosScreen(
    onBackClick: () -> Unit,
    viewModel: SuministrosViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()

    // Embarcación: solo se elige de las registradas
    val estadoEmbarcacion = rememberEstadoEmbarcacion(viewModel.uid)

    // Mensajes del ViewModel
    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }

    // Pedido enviado: volver
    LaunchedEffect(ui.pedidoConfirmado) {
        if (ui.pedidoConfirmado) {
            viewModel.pedidoProcesado()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Suministros Básicos",
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
        // Panel fijo abajo: resumen, embarcación y botón solicitar
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy900)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Gold500.copy(alpha = 0.3f))
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    if (ui.carrito.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛒 ${ui.productos} ${if (ui.productos == 1) "producto" else "productos"}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "S/ ${String.format("%.2f", ui.total)}",
                                color = Gold500,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    SelectorEmbarcacion(estado = estadoEmbarcacion, enabled = !ui.guardando)

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.confirmar(estadoEmbarcacion.ubicacionEntrega) },
                        enabled = !ui.guardando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (ui.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Navy900
                            )
                        } else {
                            Text(
                                text = "Solicitar Suministros",
                                color = Navy900,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Seleccione las cantidades a reabastecer en su embarcación.",
                color = Color.LightGray,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )

            // Pestañas de categorías (vienen de Firestore)
            if (ui.categorias.isNotEmpty() && ui.categorias.contains(ui.categoriaSeleccionada)) {
                ScrollableTabRow(
                    selectedTabIndex = ui.categorias.indexOf(ui.categoriaSeleccionada),
                    containerColor = Navy900,
                    contentColor = Gold500,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val index = ui.categorias.indexOf(ui.categoriaSeleccionada)
                        if (index >= 0 && index < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = Gold500,
                                height = 3.dp
                            )
                        }
                    },
                    divider = { }
                ) {
                    ui.categorias.forEach { categoria ->
                        val isSelected = categoria == ui.categoriaSeleccionada
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.seleccionarCategoria(categoria) },
                            text = {
                                Text(
                                    text = categoria,
                                    color = if (isSelected) Gold500 else Color.LightGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                ui.cargando && ui.platos.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold500)
                }

                ui.platos.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se pudieron cargar los suministros.\nConéctate a internet una vez para descargarlos.",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ui.platosFiltrados) { producto ->
                        MenuItemCard(
                            nombre = producto.nombre,
                            precio = "S/ ${String.format("%.2f", producto.precio)}",
                            descripcion = producto.descripcion,
                            cantidad = ui.cantidadDe(producto.nombre),
                            onAgregar = { viewModel.agregar(producto) },
                            onQuitar = { viewModel.quitar(producto) },
                            // Solo muestra foto si el producto tiene link
                            imagenUrl = producto.imagen.takeIf { it.isNotBlank() }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// Tarjeta anterior: se mantiene por si otra pantalla la usa
@Composable
fun SuministroItem(
    nombre: String,
    descripcion: String,
    precio: String
) {
    var cantidad by rememberSaveable { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = descripcion, color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = precio, color = Gold500, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                IconButton(onClick = { if (cantidad > 0) cantidad-- }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Quitar",
                        tint = if (cantidad > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = cantidad.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { cantidad++ }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = Gold500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}