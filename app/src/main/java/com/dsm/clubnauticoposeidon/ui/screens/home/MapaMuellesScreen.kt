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
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaMuellesScreen(
    onBackClick: () -> Unit,
    onRadaSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Lista del 1 al 20
    val radaList = (1..20).toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Mapa de Muelles", 
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

            // Cuadrícula (El Mapa)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f) // Ocupa el espacio disponible antes de la leyenda
            ) {
                items(radaList) { number ->
                    val isOcupada = number % 2 == 0
                    val formattedNumber = number.toString().padStart(2, '0') // "01", "02"...
                    
                    RadaItem(
                        numberStr = "R-$formattedNumber",
                        isOcupada = isOcupada,
                        onClick = {
                            if (isOcupada) {
                                Toast.makeText(context, "Rada no disponible", Toast.LENGTH_SHORT).show()
                            } else {
                                val radaName = "R-$formattedNumber"
                                Toast.makeText(context, "Rada $radaName seleccionada", Toast.LENGTH_SHORT).show()
                                onRadaSelected(radaName) // <-- Llamada para enviar el dato de regreso
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leyenda Inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leyenda Disponible
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF1B5E20), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Disponible", color = Color.White, fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                // Leyenda Ocupada
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFFB71C1C), RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ocupada", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RadaItem(
    numberStr: String,
    isOcupada: Boolean,
    onClick: () -> Unit
) {
    // Colores basados en el estado
    val bgColor = if (isOcupada) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    val borderColor = if (isOcupada) Color(0xFFE57373) else Color(0xFF4CAF50)

    Card(
        modifier = Modifier
            .aspectRatio(1f) // Para que sea cuadrada
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isOcupada) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = "Barco Ocupando Rada",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                Text(
                    text = numberStr,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}
