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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.data.repository.EmbarcacionLocalRepository
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted

// Estado compartido: lista de embarcaciones del socio y la que tiene elegida
class EstadoEmbarcacion(
    val embarcaciones: List<EmbarcacionEntity>,
    val seleccionada: EmbarcacionEntity?,
    val cargando: Boolean,
    val seleccionar: (EmbarcacionEntity) -> Unit
)

// Carga las embarcaciones (SQLite + actualización desde Firestore) y recuerda la elegida.
// Se puede usar igual en Catering, Limpieza, Suministros, etc.
@Composable
fun rememberEstadoEmbarcacion(uid: String): EstadoEmbarcacion {
    val context = LocalContext.current.applicationContext
    val repositorio = remember { EmbarcacionLocalRepository(context) }

    // null = SQLite todavía no respondió (evita mostrar "Sin embarcaciones" un instante al volver)
    val flujo = remember(uid) { repositorio.observar(uid) }
    val listaLocal by flujo.collectAsState(initial = null)
    val lista = listaLocal.orEmpty()

    // La elección se guarda en el celular: no se pierde al retroceder, girar la pantalla ni cerrar la app
    var idElegido by remember(uid) {
        mutableStateOf(PreferenciasSocio.embarcacionSeleccionada(context, uid))
    }

    var sincronizando by remember(uid) { mutableStateOf(true) }
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            try {
                repositorio.refrescar(uid)
            } catch (e: Exception) {
                // Sin internet: se usan las embarcaciones ya guardadas en SQLite
            }
        }
        sincronizando = false
    }

    // Si el socio tiene una sola embarcación, queda elegida automáticamente
    val seleccionada = lista.firstOrNull { it.id == idElegido } ?: lista.singleOrNull()

    // La elegida automáticamente también se guarda, para que siga igual si luego registra otra nave
    LaunchedEffect(seleccionada?.id) {
        val id = seleccionada?.id
        if (id != null && id != idElegido) {
            idElegido = id
            PreferenciasSocio.guardarEmbarcacion(context, uid, id)
        }
    }

    val cargando = listaLocal == null || (lista.isEmpty() && sincronizando)

    return EstadoEmbarcacion(lista, seleccionada, cargando) { embarcacion ->
        idElegido = embarcacion.id
        PreferenciasSocio.guardarEmbarcacion(context, uid, embarcacion.id)
    }
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
    }
}