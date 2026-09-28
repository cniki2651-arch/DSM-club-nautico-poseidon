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
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// 1. Modelo de Datos
data class MenuItem(
    val nombre: String,
    val precio: String,
    val descripcion: String,
    val categoria: String
)

// 2. Mock Data
val menuItems = listOf(
    MenuItem("Desayuno Náutico", "S/ 35.00", "Huevos revueltos, tocino, pan artesanal y jugo natural.", "Desayunos"),
    MenuItem("Panqueques del Capitán", "S/ 28.00", "Panqueques con miel de maple y frutos rojos.", "Desayunos"),
    
    MenuItem("Ceviche Clásico", "S/ 45.00", "Pesca del día con limón sutil y ají limo.", "Piqueos"),
    MenuItem("Tabla de Piqueos Náuticos", "S/ 60.00", "Selección de mariscos y quesos para compartir.", "Piqueos"),
    MenuItem("Tequeños de Cangrejo", "S/ 30.00", "Masa crujiente rellena de pulpa de cangrejo con salsa golf.", "Piqueos"),
    
    MenuItem("Lomo Saltado Poseidón", "S/ 55.00", "Fino lomo de res flambeado con pisco, cebolla y tomate.", "Platos Fuertes"),
    MenuItem("Arroz con Mariscos", "S/ 50.00", "Arroz meloso con mix de mariscos y un toque de ají panca.", "Platos Fuertes"),
    
    MenuItem("Chilcano con Pisco Queirolo", "S/ 25.00", "Pisco Queirolo, Ginger Ale, amargo de Angostura y limón.", "Bebidas"),
    MenuItem("Limonada Frozen", "S/ 15.00", "Refrescante limonada con hielo frozen y un toque de menta.", "Bebidas"),
    MenuItem("Cerveza Artesanal", "S/ 20.00", "Cerveza rubia artesanal, muy fría.", "Bebidas")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CateringScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    // Obtener las categorías únicas de la lista
    val categorias = remember { menuItems.map { it.categoria }.distinct() }
    
    // 3. Gestión de Estado
    var categoriaSeleccionada by remember { mutableStateOf("Piqueos") }
    
    // 4. Lógica de Filtrado
    val itemsFiltrados = remember(categoriaSeleccionada) {
        menuItems.filter { it.categoria == categoriaSeleccionada }
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
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros de Categorías (LazyRow)
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

            // Lista de Platos Filtrados
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Iteramos exclusivamente sobre itemsFiltrados
                items(itemsFiltrados) { item ->
                    MenuItemCard(
                        nombre = item.nombre,
                        precio = item.precio,
                        descripcion = item.descripcion
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // Botón de confirmar pedido intacto
                item {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Pedido enviado a cocina", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Confirmar Pedido", 
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

@Composable
fun MenuItemCard(
    nombre: String, 
    precio: String, 
    descripcion: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f) // Fondo oscuro translúcido
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
            
            Button(
                onClick = { 
                    Toast.makeText(context, "$nombre agregado", Toast.LENGTH_SHORT).show() 
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                modifier = Modifier.align(Alignment.End),
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
