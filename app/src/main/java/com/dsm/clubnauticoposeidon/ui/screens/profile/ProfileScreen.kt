package com.dsm.clubnauticoposeidon.ui.screens.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

// Datos del socio leídos desde Firestore
data class PerfilSocio(
    val nombreCompleto: String = "",
    val dni: String = "",
    val tipoDocumento: String = "",
    val correo: String = "",
    val telefono: String = "",
    val estado: String = ""
)

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit
) {
    var showQR by remember { mutableStateOf(false) }
    var perfil by remember { mutableStateOf<PerfilSocio?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Carga el perfil del usuario que inició sesión (búsqueda por correo, igual que validarAcceso)
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val correo = user?.email?.trim()?.lowercase()
        if (correo == null) {
            error = "No hay una sesión activa"
            cargando = false
            return@LaunchedEffect
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("correo", correo)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()
                if (doc == null) {
                    error = "No se encontró tu registro de socio"
                } else {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    perfil = PerfilSocio(
                        nombreCompleto = "$nombre $apellidos".trim(),
                        dni = doc.getString("dni") ?: "",
                        tipoDocumento = doc.getString("tipoDocumento") ?: "DNI",
                        correo = doc.getString("correo") ?: correo,
                        telefono = doc.getString("telefono") ?: "",
                        estado = doc.getString("estado") ?: ""
                    )
                }
                cargando = false
            }
            .addOnFailureListener {
                error = "No se pudieron cargar tus datos"
                cargando = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón de Retroceso en la esquina superior izquierda
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 32.dp, start = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = Color.White
            )
        }

        Text(
            text = "Mi Perfil",
            color = Gold500,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when {
            cargando -> {
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator(color = Gold500)
            }

            error != null -> {
                Text(
                    text = error ?: "",
                    color = Color(0xFFE57373),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                )
            }

            perfil != null -> {
                val p = perfil!!

                ProfileDataCard(
                    label = "Nombres y Apellidos",
                    value = p.nombreCompleto.ifBlank { "No registrado" }
                )
                ProfileDataCard(
                    label = p.tipoDocumento.ifBlank { "DNI" },
                    value = p.dni.ifBlank { "No registrado" }
                )
                ProfileDataCard(
                    label = "Correo Electrónico",
                    value = p.correo.ifBlank { "No registrado" }
                )
                ProfileDataCard(
                    label = "Teléfono",
                    value = p.telefono.ifBlank { "No registrado" }
                )

                val (textoEstado, colorEstado) = when (p.estado) {
                    "activo" -> "SOCIO ACTIVO" to Color(0xFF4CAF50)
                    "pendiente" -> "PENDIENTE DE APROBACIÓN" to Color(0xFFFFB74D)
                    "" -> "SIN ESTADO" to Color.LightGray
                    else -> p.estado.uppercase() to Color.LightGray
                }
                ProfileDataCard(
                    label = "Estado",
                    value = textoEstado,
                    valueColor = colorEstado
                )

                Spacer(modifier = Modifier.height(48.dp))

                // El QR solo se habilita para socios activos
                Button(
                    onClick = { showQR = true },
                    enabled = p.estado == "activo" && p.dni.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold500,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Mostrar Credencial QR",
                        color = Navy900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Cuadro de Diálogo para el código QR
    val p = perfil
    if (showQR && p != null) {
        val idSocio = "POS-${p.dni}"

        Dialog(onDismissRequest = { showQR = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Credencial Poseidón",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = p.nombreCompleto,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR generado dinámicamente con ZXing usando el ID real del socio
                    QrCodeImage(
                        content = idSocio,
                        size = 250.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ID: $idSocio",
                        fontSize = 18.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showQR = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDataCard(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f) // Fondo oscuro translúcido sobre Navy900
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun QrCodeImage(content: String, size: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val sizePx = remember(size, density) {
        with(density) { size.roundToPx() }
    }

    val bitmap = remember(content, sizePx) {
        try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Código QR generado dinámicamente",
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        // Fallback visual en caso de que ocurra algún error
        Box(
            modifier = modifier
                .size(size)
                .background(Color.LightGray)
        )
    }
}