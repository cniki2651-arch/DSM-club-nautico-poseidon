package com.dsm.clubnauticoposeidon.ui.screens.historial



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.HistorialItem
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialTransitoScreen(
    onBackClick: () -> Unit,
    viewModel: HistorialViewModel = viewModel()
) {
    val ui by viewModel.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Tránsito", color = Gold500, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(Icons.Default.Refresh, "Actualizar", tint = Gold500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("todas" to "Todas", "activa" to "Activas", "finalizada" to "Finalizadas")
                    .forEach { (clave, texto) ->
                        FilterChip(
                            selected = ui.filtro == clave,
                            onClick = { viewModel.cambiarFiltro(clave) },
                            label = { Text(texto) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = Color.White,
                                selectedContainerColor = Gold500,
                                selectedLabelColor = Navy900
                            )
                        )
                    }
            }

            when {
                ui.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold500)
                }
                ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(ui.error ?: "", color = Color(0xFFFF8A80))
                }
                ui.visibles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay asignaciones para mostrar", color = Color.LightGray)
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(ui.visibles, key = { it.id }) { item -> TarjetaHistorial(item) }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaHistorial(item: HistorialItem) {
    val colorEstado = when {
        item.salidaPendiente -> Color(0xFFF9A825)
        item.estado == "activa" -> Color(0xFF4CAF50)
        else -> Color.LightGray
    }
    val textoEstado = when {
        item.salidaPendiente -> "Salida pendiente"
        item.estado == "activa" -> "Activa"
        else -> "Finalizada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, colorEstado.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rada ${item.rada}",
                    color = Gold500,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(textoEstado, color = colorEstado, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))
            Text(item.socio, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("DNI ${item.dni}", color = Color.LightGray, fontSize = 13.sp)

            Spacer(Modifier.height(10.dp))
            Text("${item.nave} (${item.matricula})", color = Color.White, fontSize = 15.sp)
            Text(
                text = "Tipo: ${item.tipo}  ·  Eslora: ${item.eslora ?: "-"}",
                color = Color.LightGray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(10.dp))
            Text(
                text = "Ingreso: ${item.fechaIngreso}   Salida: ${item.fechaSalida}",
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}