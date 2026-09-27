

package com.dsm.clubnauticoposeidon.ui.screens.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// Modelo de datos que se guarda en Firestore

data class SolicitudPassword(
    val nombre: String = "",
    val correo: String = "",
    val mensaje: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "pendiente" // pendiente / atendido
)


// ViewModel

class RecuperarPasswordViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    var nombre by mutableStateOf("")
    var correo by mutableStateOf("")
    var mensaje by mutableStateOf("")
    var enviando by mutableStateOf(false)
    var exito by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun enviar() {
        if (nombre.isBlank() || correo.isBlank() || mensaje.isBlank()) {
            error = "Completa todos los campos"
            return
        }

        viewModelScope.launch {
            enviando = true
            error = null
            try {
                val solicitud = SolicitudPassword(
                    nombre = nombre,
                    correo = correo,
                    mensaje = mensaje
                )
                firestore.collection("solicitudes_password").add(solicitud).await()
                exito = true
            } catch (e: Exception) {
                error = e.message ?: "No se pudo enviar la solicitud"
            } finally {
                enviando = false
            }
        }
    }
}


// Pantalla

@Composable
fun RecuperarPasswordScreen(
    viewModel: RecuperarPasswordViewModel = viewModel(),
    onVolver: () -> Unit = {}
) {
    if (viewModel.exito) {
        PantallaConfirmacion(onVolver = onVolver)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(
            onClick = onVolver,
            modifier = Modifier.padding(top = 32.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(
                text = "Recuperar contraseña",
                color = Ink,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Envía tus datos y el área administrativa revisará tu solicitud para actualizar tu contraseña.",
                color = Muted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = viewModel.nombre,
                onValueChange = { viewModel.nombre = it },
                placeholder = { Text("Nombre", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Gold500,
                    unfocusedIndicatorColor = Muted
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = viewModel.correo,
                onValueChange = { viewModel.correo = it },
                placeholder = { Text("Correo electrónico", color = Muted) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Gold500,
                    unfocusedIndicatorColor = Muted
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = viewModel.mensaje,
                onValueChange = { viewModel.mensaje = it },
                placeholder = { Text("Mensaje", color = Muted) },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Gold500,
                    unfocusedIndicatorColor = Muted
                )
            )

            viewModel.error?.let { mensajeError ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = mensajeError, color = Color(0xFFE57373), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.enviar() },
                enabled = !viewModel.enviando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(50)
            ) {
                if (viewModel.enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Navy900,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Enviar",
                        color = Navy900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PantallaConfirmacion(onVolver: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Gold500,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Solicitud enviada",
            color = Ink,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "El área administrativa revisará tu solicitud y se pondrá en contacto contigo.",
            color = Muted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = "Volver al inicio de sesión",
                color = Navy900,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}