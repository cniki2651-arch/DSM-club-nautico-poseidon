package com.dsm.clubnauticoposeidon.ui.screens.solicitudes

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

/**
 * Lista de solicitudes PENDIENTES.
 * - onSolicitudClick == null -> solo lectura ("Solicitudes de radar")
 * - onSolicitudClick != null -> al tocar una se abre la asignación ("Asignar nueva rada")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandejaSolicitudesRadaScreen(
    onBackClick: () -> Unit,
    titulo: String = "Solicitudes de Rada",
    onSolicitudClick: ((String) -> Unit)? = null,
    viewModel: BandejaRadasViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, color = Gold500, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                ui.cargando -> CircularProgressIndicator(color = Gold500, modifier = Modifier.align(Alignment.Center))
                ui.solicitudes.isEmpty() -> Text(
                    "No hay solicitudes pendientes",
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(ui.solicitudes, key = { it.id }) { sol ->
                        TarjetaPendiente(
                            sol = sol,
                            onClick = onSolicitudClick?.let { click -> { click(sol.id) } }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPendiente(sol: SolicitudRada, onClick: (() -> Unit)?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, Gold500.copy(alpha = 0.6f))
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(sol.nombreNave, color = Gold500, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Matrícula: ${sol.matricula}", color = Color.White, fontSize = 15.sp)
            Text("Tipo: ${sol.tipo}", color = Color.White, fontSize = 15.sp)
            Text("Eslora: ${sol.eslora}", color = Color.White, fontSize = 15.sp)
            Text("DNI del socio: ${sol.dniSocio}", color = Color.LightGray, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text("Ingreso: ${sol.fechaIngreso}   Salida: ${sol.fechaSalida}", color = Color.White, fontSize = 14.sp)
            if (onClick != null) {
                Spacer(Modifier.height(10.dp))
                Text("Toca para asignar rada", color = Gold500, fontSize = 13.sp)
            }
        }
    }
}