package com.dsm.clubnauticoposeidon.ui.screens.radas

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val SIN_FECHA = -1L
private val FormatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatearFecha(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(FormatoFecha)

// El selector de Material trabaja en UTC: "hoy" también debe calcularse en UTC
private fun hoyUtcMillis(): Long =
    LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarRadaScreen(
    onBackClick: () -> Unit,
    onAbrirMapaClick: () -> Unit,
    radaSeleccionada: String = "",
    viewModel: RadaViewModel = viewModel()
) {
    val context = LocalContext.current
    val ui by viewModel.ui.collectAsState()

    var dniSocio by rememberSaveable { mutableStateOf("") }
    var matricula by rememberSaveable { mutableStateOf("") }

    // Fechas guardadas en milisegundos (SIN_FECHA = aún no elegida)
    var ingresoMillis by rememberSaveable { mutableLongStateOf(SIN_FECHA) }
    var salidaMillis by rememberSaveable { mutableLongStateOf(SIN_FECHA) }
    var mostrarCalendarioIngreso by rememberSaveable { mutableStateOf(false) }
    var mostrarCalendarioSalida by rememberSaveable { mutableStateOf(false) }

    // Rada elegida: la del mapa, o la sugerida si todavía no se eligió ninguna
    var radaElegida by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(radaSeleccionada) {
        if (radaSeleccionada.isNotBlank()) radaElegida = radaSeleccionada
    }
    LaunchedEffect(ui.sugerida) {
        val sugerida = ui.sugerida
        if (sugerida != null && radaElegida.isBlank()) radaElegida = sugerida.id
    }

    LaunchedEffect(ui.mensaje) {
        ui.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mensajeMostrado()
        }
    }
    LaunchedEffect(ui.guardado) {
        if (ui.guardado) onBackClick()
    }

    val isFormValid = dniSocio.length == 8 &&
            matricula.isNotBlank() &&
            ingresoMillis != SIN_FECHA &&
            salidaMillis != SIN_FECHA &&
            radaElegida.isNotBlank() &&
            ui.embarcacion != null &&
            !ui.cargando

    // ---------- Calendario de ingreso ----------
    if (mostrarCalendarioIngreso) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (ingresoMillis != SIN_FECHA) ingresoMillis else null,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= hoyUtcMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendarioIngreso = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { elegida ->
                        ingresoMillis = elegida
                        // Si la salida quedó antes del nuevo ingreso, se borra
                        if (salidaMillis != SIN_FECHA && salidaMillis < elegida) salidaMillis = SIN_FECHA
                    }
                    mostrarCalendarioIngreso = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendarioIngreso = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = state) }
    }

    // ---------- Calendario de salida ----------
    if (mostrarCalendarioSalida) {
        val minimo = if (ingresoMillis != SIN_FECHA) ingresoMillis else hoyUtcMillis()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = if (salidaMillis != SIN_FECHA) salidaMillis else null,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minimo
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendarioSalida = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { salidaMillis = it }
                    mostrarCalendarioSalida = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendarioSalida = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = state) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nueva Asignación", color = Color.White, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Aviso del clima (Retrofit)
            ui.clima?.let { clima ->
                val seguro = ui.vientoSeguro == true
                Text(
                    text = if (seguro)
                        "Clima favorable: viento ${clima.wind_speed_10m} km/h"
                    else
                        "Viento fuerte (${clima.wind_speed_10m} km/h): no se recomienda amarrar",
                    color = if (seguro) Color(0xFF7FD18B) else Color(0xFFFF8A80),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold500,
                unfocusedBorderColor = Color.LightGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Gold500,
                focusedLabelColor = Gold500,
                unfocusedLabelColor = Color.LightGray,
                focusedLeadingIconColor = Gold500,
                unfocusedLeadingIconColor = Color.LightGray
            )

            OutlinedTextField(
                value = dniSocio,
                onValueChange = { dniSocio = it.filter { c -> c.isDigit() }.take(8) },
                label = { Text("DNI del Socio") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it.uppercase().filter { c -> !c.isWhitespace() } },
                label = { Text("Matrícula de la Embarcación") },
                leadingIcon = { Icon(Icons.Default.DirectionsBoat, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = textFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.buscarNave(dniSocio, matricula) },
                enabled = dniSocio.length == 8 && matricula.isNotBlank() && !ui.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                border = BorderStroke(1.dp, Gold500),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500)
            ) {
                if (ui.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Gold500,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Buscar nave", fontWeight = FontWeight.SemiBold)
                }
            }

            // Datos de la nave, más grandes y con espacio
            ui.embarcacion?.let { nave ->
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, Gold500)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = nave.nombreNave,
                            color = Gold500,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tipo: ${nave.tipo}",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Eslora: ${nave.eslora}",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Periodo de Asignación",
                color = Gold500,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { mostrarCalendarioIngreso = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.dp, if (ingresoMillis == SIN_FECHA) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (ingresoMillis == SIN_FECHA) "Ingreso" else formatearFecha(ingresoMillis),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = { mostrarCalendarioSalida = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    border = BorderStroke(1.dp, if (salidaMillis == SIN_FECHA) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (salidaMillis == SIN_FECHA) "Salida" else formatearFecha(salidaMillis),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ubicación",
                color = Gold500,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAbrirMapaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Gold500),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Mapa",
                    tint = Gold500,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (radaElegida.isEmpty()) "Seleccionar Rada en el Mapa"
                    else "Rada seleccionada: $radaElegida",
                    color = if (radaElegida.isEmpty()) Color.White else Gold500,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.confirmar(
                        dni = dniSocio,
                        radaId = radaElegida,
                        fechaIngreso = formatearFecha(ingresoMillis),
                        fechaSalida = formatearFecha(salidaMillis)
                    )
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold500,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                    disabledContentColor = Color.LightGray
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "Confirmar Asignación",
                    color = if (isFormValid) Navy900 else Color.LightGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}