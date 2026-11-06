package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dsm.clubnauticoposeidon.ui.components.ImagenesMenu
import com.dsm.clubnauticoposeidon.ui.components.SelectorEmbarcacion
import com.dsm.clubnauticoposeidon.ui.components.rememberEstadoEmbarcacion
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// Pantalla del menú de Catering (MVVM: la lógica está en CateringViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CateringScreen(
    onBackClick: () -> Unit,
    viewModel: CateringViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()

    // Embarcación: solo se elige de las registradas, se recuerda y muestra su rada
    val estadoEmbarcacion = rememberEstadoEmbarcacion(viewModel.uid)

    // Mensajes del ViewModel
    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }

    // Pedido enviado: volver al inicio
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
                        text = "Menú del Restaurante",
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
        // Panel fijo abajo: resumen del pedido, embarcación y botón confirmar
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
                                text = "Confirmar Pedido",
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
            // Categorías con ícono, estilo Yape
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(ui.categorias) { categoria ->
                    CategoriaChip(
                        categoria = categoria,
                        seleccionada = categoria == ui.categoriaSeleccionada,
                        onClick = { viewModel.seleccionarCategoria(categoria) }
                    )
                }
            }

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
                        text = "No se pudo cargar el menú.\nConéctate a internet una vez para descargarlo.",
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
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    items(ui.platosFiltrados) { plato ->
                        MenuItemCard(
                            nombre = plato.nombre,
                            precio = "S/ ${String.format("%.2f", plato.precio)}",
                            descripcion = plato.descripcion,
                            cantidad = ui.cantidadDe(plato.nombre),
                            onAgregar = { viewModel.agregar(plato) },
                            onQuitar = { viewModel.quitar(plato) },
                            imagenUrl = plato.imagen,
                            iconoRespaldo = iconoCategoria(plato.categoria)
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// Botón redondo de categoría con ícono y nombre debajo
@Composable
private fun CategoriaChip(
    categoria: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    if (seleccionada) Gold500.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f),
                    CircleShape
                )
                .border(
                    if (seleccionada) 2.dp else 1.dp,
                    if (seleccionada) Gold500 else Color.Gray.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconoCategoria(categoria), fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = categoria,
            color = if (seleccionada) Gold500 else Color.LightGray,
            fontSize = 12.sp,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// Ícono según el nombre de la categoría
fun iconoCategoria(categoria: String): String {
    val c = categoria.lowercase()
    return when {
        "bebida" in c || "jugo" in c || "trago" in c || "cóctel" in c -> "🥤"
        "desayuno" in c -> "🍳"
        "piqueo" in c || "entrada" in c || "snack" in c -> "🍤"
        "fuerte" in c || "fondo" in c -> "🍽️"
        "marino" in c || "ceviche" in c || "pescado" in c -> "🐟"
        "ensalada" in c -> "🥗"
        "postre" in c || "dulce" in c -> "🍰"
        else -> "🍴"
    }
}

// Tarjeta de producto.
// Los parámetros nuevos tienen valor por defecto, así que las otras pantallas
// que ya usan MenuItemCard (Limpieza, Suministros...) siguen funcionando igual.
@Composable
fun MenuItemCard(
    nombre: String,
    precio: String,
    descripcion: String,
    cantidad: Int = 0,
    onAgregar: (() -> Unit)? = null,
    onQuitar: (() -> Unit)? = null,
    imagenUrl: String? = null,
    iconoRespaldo: String = "🍽️"
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Foto del plato (URL de Firestore). Sin foto o sin internet: ícono de la categoría
            if (imagenUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconoRespaldo, fontSize = 48.sp)
                    if (imagenUrl.isNotBlank()) {
                        AsyncImage(
                            model = imagenUrl,
                            contentDescription = nombre,
                            imageLoader = ImagenesMenu.loader(context),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nombre,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = precio,
                        color = Gold500,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descripcion,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cantidad > 0 && onQuitar != null && onAgregar != null) {
                        // Contador  −  2  +
                        Row(
                            modifier = Modifier
                                .border(BorderStroke(1.dp, Gold500), RoundedCornerShape(50))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BotonContador(texto = "−", onClick = onQuitar)
                            Text(
                                text = "$cantidad",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(32.dp)
                            )
                            BotonContador(texto = "+", onClick = onAgregar)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (onAgregar != null) {
                                    onAgregar()
                                } else {
                                    Toast.makeText(context, "$nombre agregado", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "+ Agregar",
                                color = Navy900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonContador(texto: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = Gold500,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}