package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.components.BarrasPedidos
import com.dsm.clubnauticoposeidon.ui.components.DatoGrafico
import com.dsm.clubnauticoposeidon.ui.components.DonaPedidos
import com.dsm.clubnauticoposeidon.ui.components.TarjetaGraficoPlegable
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import kotlin.math.roundToInt

// Mismo tono que las tarjetas de gráficos, para que todo el dashboard se vea igual
private val FondoIndicador = lerp(Navy800, Color(0xFF90D5FF), 0.14f)
private val BordeIndicador = lerp(Navy800, Color(0xFF90D5FF), 0.30f)

private val Verde = Color(0xFF66BB6A)
private val Celeste = Color(0xFF4FC3F7)
private val Rojo = Color(0xFFE57373)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardJefaturaScreen(
    onBackClick: () -> Unit,
    viewModel: DashboardJefaturaViewModel = viewModel()
) {
    val membresias by viewModel.membresias.collectAsState()
    val consumos by viewModel.consumoPorModulo.collectAsState()
    val pedidos by viewModel.pedidos.collectAsState()
    val error by viewModel.error.collectAsState()

    val activos = membresias["aprobado"] ?: 0
    val enTramite = (membresias["pendiente"] ?: 0) + (membresias["revisado_secretaria"] ?: 0)
    val rechazadas = membresias["rechazado"] ?: 0

    // Datos para los componentes de gráficos compartidos (ui/components)
    val datosMembresias = listOf(
        DatoGrafico("Aprobados", membresias["aprobado"] ?: 0, Verde),
        DatoGrafico("Pendientes", membresias["pendiente"] ?: 0, Gold500),
        DatoGrafico("Revisados", membresias["revisado_secretaria"] ?: 0, Celeste),
        DatoGrafico("Rechazados", membresias["rechazado"] ?: 0, Rojo)
    )
    // DatoGrafico guarda enteros: el consumo se muestra en soles enteros
    val datosConsumo = listOf(
        DatoGrafico("Catering", (consumos["catering"] ?: 0.0).roundToInt(), Gold500),
        DatoGrafico("Limpieza", (consumos["limpieza"] ?: 0.0).roundToInt(), Celeste),
        DatoGrafico("Sumin.", (consumos["suministros"] ?: 0.0).roundToInt(), Verde),
        DatoGrafico("Asistencia", (consumos["asistencia"] ?: 0.0).roundToInt(), Rojo)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard de Jefatura",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            error?.let {
                item { Text(text = it, color = Rojo, fontSize = 13.sp) }
            }

            // Indicadores de membresías
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador("Socios activos", activos.toString(), Verde, Modifier.weight(1f))
                    Indicador("En trámite", enTramite.toString(), Gold500, Modifier.weight(1f))
                    Indicador("Rechazadas", rechazadas.toString(), Rojo, Modifier.weight(1f))
                }
            }

            // Gráfico 1: membresías por estado (dona compartida)
            item {
                TarjetaGraficoPlegable(
                    titulo = "Membresías por estado",
                    subtitulo = "Solicitudes de ingreso y socios",
                    abiertaAlInicio = true
                ) {
                    DonaPedidos(
                        datos = datosMembresias,
                        etiquetaCentro = "membresías"
                    )
                }
            }

            // Indicadores de consumo
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Indicador(
                        "Pedidos por atender",
                        pedidos.pendientes.toString(),
                        Celeste,
                        Modifier.weight(1f)
                    )
                    Indicador(
                        "Consumo total",
                        "S/ ${String.format("%.0f", pedidos.montoTotal)}",
                        Gold500,
                        Modifier.weight(1f)
                    )
                }
            }

            // Gráfico 2: consumo por módulo (barras redondeadas compartidas)
            item {
                TarjetaGraficoPlegable(
                    titulo = "Consumo por módulo",
                    subtitulo = "Total en soles (S/) por servicio",
                    abiertaAlInicio = true
                ) {
                    BarrasPedidos(datos = datosConsumo)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun Indicador(
    etiqueta: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FondoIndicador),
        border = BorderStroke(1.dp, BordeIndicador)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
