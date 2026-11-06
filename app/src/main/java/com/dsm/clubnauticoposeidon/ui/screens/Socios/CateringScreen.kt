package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.data.repository.ConsumoRepository
import com.dsm.clubnauticoposeidon.model.ItemCarrito
import com.dsm.clubnauticoposeidon.ui.components.SelectorEmbarcacion
import com.dsm.clubnauticoposeidon.ui.components.rememberEstadoEmbarcacion
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CateringScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para los datos de la nube
    var listaPlatos by remember { mutableStateOf<List<PlatoItem>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf("Desayunos") }

    // Pedido que el socio va armando
    val carrito = remember { mutableStateListOf<ItemCarrito>() }
    var guardando by remember { mutableStateOf(false) }

    // Embarcación donde se entrega: solo se elige de las registradas y se recuerda al retroceder
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val estadoEmbarcacion = rememberEstadoEmbarcacion(uid)

    // 2. Descarga dinámica desde Firebase al abrir la pantalla
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("catalogo_servicios")
            .whereEqualTo("modulo", "catering")
            .get()
            .addOnSuccessListener { result ->
                val platosNuevos = result.toObjects(PlatoItem::class.java)
                listaPlatos = platosNuevos
                // Si la categoría seleccionada por defecto no existe en la BD, asignamos la primera disponible
                if (platosNuevos.isNotEmpty() && !platosNuevos.any { it.categoria == categoriaSeleccionada }) {
                    categoriaSeleccionada = platosNuevos.first().categoria
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar el menú de la nube", Toast.LENGTH_SHORT).show()
            }
    }

    // Obtener las categorías únicas directamente de los datos descargados
    val categorias = remember(listaPlatos) { listaPlatos.map { it.categoria }.distinct() }

    // 3. Lógica de Filtrado reactiva
    val itemsFiltrados = remember(listaPlatos, categoriaSeleccionada) {
        listaPlatos.filter { it.categoria == categoriaSeleccionada }
    }

    // Funciones del pedido
    fun cantidadDe(nombre: String): Int =
        carrito.firstOrNull { it.nombre == nombre }?.cantidad ?: 0

    fun agregar(plato: PlatoItem) {
        val i = carrito.indexOfFirst { it.nombre == plato.nombre }
        if (i >= 0) {
            carrito[i] = carrito[i].copy(cantidad = carrito[i].cantidad + 1)
        } else {
            carrito.add(ItemCarrito(nombre = plato.nombre, precioUnitario = plato.precio))
        }
    }

    fun quitar(nombre: String) {
        val i = carrito.indexOfFirst { it.nombre == nombre }
        if (i < 0) return
        if (carrito[i].cantidad > 1) {
            carrito[i] = carrito[i].copy(cantidad = carrito[i].cantidad - 1)
        } else {
            carrito.removeAt(i)
        }
    }

    val total = carrito.sumOf { it.precioUnitario * it.cantidad }

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
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros de Categorías Dinámicos (LazyRow)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categorias) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    OutlinedButton(
                        onClick = { categoriaSeleccionada = categoria },
                        border = BorderStroke(1.dp, if (isSelected) Gold500 else Color.Gray),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) Gold500.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isSelected) Gold500 else Color.LightGray
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = categoria,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Lista de Platos Filtrados desde la Base de Datos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(itemsFiltrados) { item ->
                    MenuItemCard(
                        nombre = item.nombre,
                        precio = "S/ ${String.format("%.2f", item.precio)}",
                        descripcion = item.descripcion,
                        cantidad = cantidadDe(item.nombre),
                        onAgregar = { agregar(item) },
                        onQuitar = { quitar(item.nombre) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Dónde entregar el pedido: lista para elegir la embarcación (no se escribe)
                item {
                    SelectorEmbarcacion(
                        estado = estadoEmbarcacion,
                        enabled = !guardando
                    )
                }

                item {
                    Button(
                        onClick = {
                            val usuario = FirebaseAuth.getInstance().currentUser
                            val nave = estadoEmbarcacion.seleccionada
                            when {
                                carrito.isEmpty() ->
                                    Toast.makeText(context, "Agrega al menos un plato", Toast.LENGTH_SHORT).show()
                                nave == null ->
                                    Toast.makeText(context, "Elige tu embarcación", Toast.LENGTH_SHORT).show()
                                usuario == null ->
                                    Toast.makeText(context, "Inicia sesión para hacer un pedido", Toast.LENGTH_SHORT).show()
                                else -> scope.launch {
                                    guardando = true
                                    try {
                                        // 1) Se guarda en SQLite  2) WorkManager lo envía a Firestore
                                        ConsumoRepository(context).registrarPedido(
                                            socioUid = usuario.uid,
                                            correo = usuario.email.orEmpty(),
                                            modulo = "catering",
                                            ubicacionRada = estadoEmbarcacion.ubicacionEntrega
                                                ?: "${nave.matricula} · ${nave.nombreNave}",
                                            items = carrito.toList()
                                        )
                                        Toast.makeText(
                                            context,
                                            "Pedido registrado. Se enviará a cocina automáticamente",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onBackClick()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo guardar el pedido", Toast.LENGTH_SHORT).show()
                                        guardando = false
                                    }
                                }
                            }
                        },
                        enabled = !guardando,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (carrito.isEmpty()) "Confirmar Pedido"
                            else "Confirmar Pedido · S/ ${String.format("%.2f", total)}",
                            color = Navy900,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// Los parámetros nuevos tienen valor por defecto, así que las otras pantallas
// que ya usan MenuItemCard (Limpieza, Suministros...) siguen funcionando igual
@Composable
fun MenuItemCard(
    nombre: String,
    precio: String,
    descripcion: String,
    cantidad: Int = 0,
    onAgregar: (() -> Unit)? = null,
    onQuitar: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
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
                Text(
                    text = precio,
                    color = Gold500,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descripcion,
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cantidad > 0 && onQuitar != null) {
                    OutlinedButton(
                        onClick = onQuitar,
                        border = BorderStroke(1.dp, Gold500),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Quitar", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "x$cantidad",
                        color = Gold500,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

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
                        text = "Agregar",
                        color = Navy900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
