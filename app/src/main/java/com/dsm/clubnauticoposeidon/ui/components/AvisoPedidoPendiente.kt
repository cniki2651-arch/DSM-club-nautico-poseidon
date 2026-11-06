package com.dsm.clubnauticoposeidon.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500

// Aviso para la pantalla de inicio del socio:
// "Tienes un pedido sin confirmar · Continuar". Solo aparece si hay algo en el carrito.
@Composable
fun AvisoPedidoPendiente(
    uid: String,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier,
    modulo: String = "catering"
) {
    val context = LocalContext.current
    val carrito by remember(uid, modulo) {
        CarritoSocio.observar(context, uid, modulo)
    }.collectAsState()

    if (uid.isBlank() || carrito.isEmpty()) return

    val productos = carrito.sumOf { it.cantidad }
    val total = carrito.sumOf { it.precioUnitario * it.cantidad }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Gold500, RoundedCornerShape(16.dp))
            .clickable { onContinuar() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🛒", fontSize = 26.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tienes un pedido sin confirmar",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$productos ${if (productos == 1) "producto" else "productos"} · S/ ${String.format("%.2f", total)}",
                color = Color.LightGray,
                fontSize = 13.sp
            )
        }
        Text(
            text = "Continuar",
            color = Gold500,
            fontWeight = FontWeight.Bold
        )
    }
}