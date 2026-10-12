package com.dsm.clubnauticoposeidon.ui.screens.naves

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val SIN_FECHA = -1L
private val Formato: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatear(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(Formato)

private fun hoyUtc(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarRadaDialog(
    nave: Embarcacion,
    enviando: Boolean,
    onCancelar: () -> Unit,
    onEnviar: (fechaIngreso: String, fechaSalida: String) -> Unit
) {
    var ingreso by remember { mutableLongStateOf(SIN_FECHA) }
    var salida by remember { mutableLongStateOf(SIN_FECHA) }
    var verIngreso by remember { mutableStateOf(false) }
    var verSalida by remember { mutableStateOf(false) }

    if (verIngreso) {
        val state = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= hoyUtc()
            }
        )
        DatePickerDialog(
            onDismissRequest = { verIngreso = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { elegida ->
                        ingreso = elegida
                        if (salida != SIN_FECHA && salida < elegida) salida = SIN_FECHA
                    }
                    verIngreso = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { verIngreso = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }

    if (verSalida) {
        val minimo = if (ingreso != SIN_FECHA) ingreso else hoyUtc()
        val state = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minimo
            }
        )
        DatePickerDialog(
            onDismissRequest = { verSalida = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { salida = it }
                    verSalida = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { verSalida = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        containerColor = Navy900,
        title = { Text("Solicitar rada", color = Gold500, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(nave.nombreNave, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("${nave.matricula} · ${nave.tipo} · eslora ${nave.eslora}", color = Color.LightGray, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("Periodo", color = Gold500, fontWeight = FontWeight.SemiBold)

                OutlinedButton(
                    onClick = { verIngreso = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, if (ingreso == SIN_FECHA) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (ingreso == SIN_FECHA) "Fecha de ingreso" else "Ingreso: ${formatear(ingreso)}")
                }
                OutlinedButton(
                    onClick = { verSalida = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, if (salida == SIN_FECHA) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (salida == SIN_FECHA) "Fecha de salida" else "Salida: ${formatear(salida)}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEnviar(formatear(ingreso), formatear(salida)) },
                enabled = ingreso != SIN_FECHA && salida != SIN_FECHA && !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                if (enviando) CircularProgressIndicator(Modifier.size(18.dp), color = Navy900, strokeWidth = 2.dp)
                else Text("Enviar", color = Navy900, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !enviando) { Text("Cancelar", color = Color.White) }
        }
    )
}