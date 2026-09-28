package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// 1. Modelo de Datos
data class Suministro(
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val categoria: String
)

// 2. Mock Data Ampliada
val suministrosData = listOf(
    Suministro("Bolsas de Hielo (5kg)", "Hielo filtrado en cubos.", "S/ 15.00", "Consumibles"),
    Suministro("Agua Potable (Galones)", "Recarga directa al tanque.", "S/ 8.00", "Consumibles"),
    
    Suministro("Balón de Gas (10kg)", "Para cocina de la embarcación.", "S/ 50.00", "Combustible y Energía"),
    Suministro("Recarga de Batería 12V", "Carga completa.", "S/ 45.00", "Combustible y Energía"),
    Suministro("Aceite de Motor Marino", "Botella de 1L.", "S/ 85.00", "Combustible y Energía"),
    
    Suministro("Kit de Limpieza Biodegradable", "Jabón y productos marinos.", "S/ 60.00", "Limpieza"),
    Suministro("Bolsas de Basura Industriales", "Paquete de 20 bolsas extra fuertes.", "S/ 12.00", "Limpieza"),
    
    Suministro("Servicio de Lavandería (Saco)", "Lavado y secado por saco.", "S/ 35.00", "Servicios")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuministrosScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Obtener las categorías únicas
    val categorias = remember { suministrosData.map { it.categoria }.distinct() }

    // 3. Gestión de Estado
    var categoriaSeleccionada by remember { mutableStateOf("Consumibles") }

    // 4. Filtrado reactivo
    val itemsFiltrados = remember(categoriaSeleccionada) {
        suministrosData.filter { it.categoria == categoriaSeleccionada }
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

            // Pestañas de Categorías
            ScrollableTabRow(
                selectedTabIndex = categorias.indexOf(categoriaSeleccionada),
                containerColor = Navy900,
                contentColor = Gold500,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[categorias.indexOf(categoriaSeleccionada)]),
                        color = Gold500,
                        height = 3.dp
                    )
                },
                divider = { } // Sin línea divisoria para un diseño más limpio
            ) {
                categorias.forEach { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Tab(
                        selected = isSelected,
                        onClick = { categoriaSeleccionada = categoria },
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

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Suministros Filtrados
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Usamos el nombre como Key para que el contador no se pierda/mezcle al cambiar de pestaña
                items(itemsFiltrados, key = { it.nombre }) { suministro ->
                    SuministroItem(
                        nombre = suministro.nombre,
                        descripcion = suministro.descripcion,
                        precio = suministro.precio
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // Botón Principal
                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Suministros en camino", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Solicitar Suministros",
                            color = Navy900,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun SuministroItem(
    nombre: String,
    descripcion: String,
    precio: String
) {
    // Estado del contador preservado en navegación y cambio de tabs
    var cantidad by rememberSaveable { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f) // Fondo translúcido oscuro
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sección Izquierda: Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = precio,
                    color = Gold500,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Sección Derecha: Contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                // Botón Menos
                IconButton(
                    onClick = { if (cantidad > 0) cantidad-- },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Quitar",
                        tint = if (cantidad > 0) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cantidad
                Text(
                    text = cantidad.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Botón Más
                IconButton(
                    onClick = { cantidad++ },
                    modifier = Modifier.size(32.dp)
                ) {
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
