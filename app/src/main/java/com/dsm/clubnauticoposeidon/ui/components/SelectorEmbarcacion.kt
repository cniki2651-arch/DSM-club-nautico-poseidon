package com.dsm.clubnauticoposeidon.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.model.Rada
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted

// Estado que recibe el selector: lista de naves, la elegida y su rada (si tiene una aprobada)
class EstadoEmbarcacion(
    val embarcaciones: List<EmbarcacionEntity>,
    val seleccionada: EmbarcacionEntity?,
    val cargando: Boolean,
    val rada: Rada?,
    val radaCargando: Boolean,
    val seleccionar: (EmbarcacionEntity) -> Unit
) {
    // Texto que se guarda en el pedido para que el personal sepa a dónde llevarlo.
    // Ej: "Rada R-01 · ABC-1234 · Poseidón I"   o   "ABC-1234 · Poseidón I" si no tiene rada
    val ubicacionEntrega: String?
        get() {
            val nave = seleccionada ?: return null
            val textoRada = rada?.let { "Rada ${it.codigo.ifBlank { it.id }}" }
            return listOfNotNull(textoRada, nave.matricula, nave.nombreNave.ifBlank { null })
                .joinToString(" · ")
        }
}

// Conecta la pantalla con SelectorEmbarcacionViewModel.
// Se usa igual en Catering, Limpieza, Suministros, etc.
@Composable
fun rememberEstadoEmbarcacion(uid: String): EstadoEmbarcacion {
    val vm: SelectorEmbarcacionViewModel = viewModel()
    LaunchedEffect(uid) { vm.iniciar(uid) }
    val ui by vm.estado.collectAsState()

    return EstadoEmbarcacion(
        embarcaciones = ui.embarcaciones,
        seleccionada = ui.seleccionada,
        cargando = ui.cargando,
        rada = ui.rada,
        radaCargando = ui.radaCargando,
        seleccionar = vm::seleccionar
    )
}

// Lista desplegable: solo se elige, no se escribe
@Composable
fun SelectorEmbarcacion(
    estado: EstadoEmbarcacion,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val seleccionada = estado.seleccionada
    val sePuedeAbrir = enabled && !estado.cargando && estado.embarcaciones.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Entregar en mi embarcación",
            color = Muted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (seleccionada != null) Gold500 else Muted,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = sePuedeAbrir) { expandido = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    when {
                        seleccionada != null -> {
                            Text(
                                text = seleccionada.nombreNave.ifBlank { "Embarcación" },
                                color = Ink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Matrícula: ${seleccionada.matricula}",
                                color = Muted,
                                fontSize = 13.sp
                            )
                        }
                        estado.cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Gold500
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargando tus embarcaciones…", color = Muted)
                        }
                        estado.embarcaciones.isEmpty() ->
                            Text("Sin embarcaciones registradas", color = Muted)
                        else ->
                            Text("Selecciona tu embarcación", color = Muted)
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Elegir embarcación",
                    tint = if (sePuedeAbrir) Gold500 else Muted
                )
            }

            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                estado.embarcaciones.forEach { embarcacion ->
                    val esLaElegida = embarcacion.id == seleccionada?.id
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = embarcacion.nombreNave.ifBlank { "Embarcación" },
                                    fontWeight = if (esLaElegida) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Matrícula: ${embarcacion.matricula}",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                            }
                        },
                        trailingIcon = {
                            if (esLaElegida) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Gold500)
                            }
                        },
                        onClick = {
                            estado.seleccionar(embarcacion)
                            expandido = false
                        }
                    )
                }
            }
        }

        // Rada de la nave elegida: solo aparece si el naviero aprobó la solicitud
        val rada = estado.rada
        if (seleccionada != null && rada != null) {
            Spacer(modifier = Modifier.height(6.dp))
            val codigo = rada.codigo.ifBlank { rada.id }
            val muelle = if (rada.muelle.isNotBlank()) " · Muelle ${rada.muelle}" else ""
            Text(
                text = "📍 Rada $codigo$muelle",
                color = Gold500,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}