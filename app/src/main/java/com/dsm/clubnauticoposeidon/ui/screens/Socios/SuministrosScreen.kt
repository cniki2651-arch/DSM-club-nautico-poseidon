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
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.firestore.FirebaseFirestore

// 1. Modelo de Datos adaptado a Firestore (tipos numéricos y String limpios)
data class SuministroItemModel(
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val modulo: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuministrosScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Estados para recibir los datos de la nube
    var listaSuministros by remember { mutableStateOf<List<SuministroItemModel>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf("Consumibles") }

    // 2. Descarga dinámica desde Firestore al abrir la pantalla (Módulo suministros)
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("catalogo_servicios")
            .whereEqualTo("modulo", "suministros")
            .get()
            .addOnSuccessListener { result ->
                val suministrosNuevos = result.toObjects(SuministroItemModel::class.java)
                listaSuministros = suministrosNuevos
                // Si la categoría por defecto no existe en los datos descargados, seleccionamos la primera disponible
                if (suministrosNuevos.isNotEmpty() && !suministrosNuevos.any { it.categoria == categoriaSeleccionada }) {
                    categoriaSeleccionada = suministrosNuevos.first().categoria
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar los suministros de la nube", Toast.LENGTH_SHORT).show()
            }
    }

    // Obtener las categorías únicas de forma dinámica de la base de datos
    val categorias = remember(listaSuministros) { listaSuministros.map { it.categoria }.distinct() }

    // 3. Filtrado reactivo
    val itemsFiltrados = remember(listaSuministros, categoriaSeleccionada) {
        listaSuministros.filter { it.categoria == categoriaSeleccionada }
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

            // Pestañas de Categorías Dinámicas (Solo se muestran si hay categorías cargadas)
            if (categorias.isNotEmpty() && categorias.contains(categoriaSeleccionada)) {
                ScrollableTabRow(
                    selectedTabIndex = categorias.indexOf(categoriaSeleccionada),
                    containerColor = Navy900,
                    contentColor = Gold500,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val index = categorias.indexOf(categoriaSeleccionada)
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Suministros Filtrados desde la Base de Datos
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(itemsFiltrados) { suministro ->
                    SuministroItem(
                        nombre = suministro.nombre,
                        descripcion = suministro.descripcion,
                        precio = "S/ ${String.format("%.2f", suministro.precio)}"
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
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

                Text(
                    text = cantidad.toString(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

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