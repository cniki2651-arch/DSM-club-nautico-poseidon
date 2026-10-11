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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    var fechaIngreso by rememberSaveable { mutableStateOf("") }
    var fechaSalida by rememberSaveable { mutableStateOf("") }

    // Rada elegida: la que vuelve del mapa o la sugerida por la app
    var radaElegida by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(radaSeleccionada) {
        if (radaSeleccionada.isNotBlank()) radaElegida = radaSeleccionada
    }

    // Mensajes y cierre al guardar
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
            fechaIngreso.isNotBlank() &&
            fechaSalida.isNotBlank() &&
            radaElegida.isNotBlank() &&
            ui.embarcacion != null &&
            !ui.cargando

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

            // Buscar nave y sugerir rada
            OutlinedButton(
                onClick = { viewModel.buscarNave(dniSocio, matricula) },
                enabled = dniSocio.length == 8 && matricula.isNotBlank() && !ui.cargando,
                modifier = Modifier.fillMaxWidth().height(48.dp),
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
                    Text("Buscar nave y sugerir rada", fontWeight = FontWeight.SemiBold)
                }
            }

            // Datos de la nave y sugerencia
            ui.embarcacion?.let { nave ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${nave.nombreNave} · ${nave.tipo} · eslora ${nave.eslora}",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                ui.sugerida?.let { sugerida ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sugerida: ${sugerida.codigo} (${sugerida.muelle})",
                            color = Gold500,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(
                            onClick = { radaElegida = sugerida.id },
                            border = BorderStroke(1.dp, Gold500),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500)
                        ) { Text("Usar") }
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
                    onClick = { fechaIngreso = "15/10/2026" }, // simulado
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(1.dp, if (fechaIngreso.isEmpty()) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (fechaIngreso.isEmpty()) "Ingreso" else fechaIngreso, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = { fechaSalida = "20/10/2026" }, // simulado
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(1.dp, if (fechaSalida.isEmpty()) Color.LightGray else Gold500),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (fechaSalida.isEmpty()) "Salida" else fechaSalida, fontSize = 14.sp)
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
                modifier = Modifier.fillMaxWidth().height(64.dp),
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
                onClick = { viewModel.confirmar(dniSocio, radaElegida, fechaIngreso, fechaSalida) },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
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