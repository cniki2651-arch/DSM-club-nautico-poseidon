package com.dsm.clubnauticoposeidon.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VisorTelemetriaVectorial(
    latitud: Double,
    longitud: Double,
    timestamp: Long = 0L,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    val horaActualizada = remember(timestamp) {
        if (timestamp > 0) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Hace un momento"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Navy900,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Gold500)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Canvas del Radar Náutico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Navy800, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val maxRadius = minOf(centerX, centerY) * 0.85f

                    // 3 Círculos concéntricos
                    for (i in 1..3) {
                        drawCircle(
                            color = Gold500.copy(alpha = 0.25f),
                            radius = maxRadius * (i / 3f),
                            center = Offset(centerX, centerY),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                        )
                    }

                    // Ejes cardinales (N, S, E, O)
                    drawLine(
                        color = Gold500.copy(alpha = 0.25f),
                        start = Offset(centerX, centerY - maxRadius),
                        end = Offset(centerX, centerY + maxRadius),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Gold500.copy(alpha = 0.25f),
                        start = Offset(centerX - maxRadius, centerY),
                        end = Offset(centerX + maxRadius, centerY),
                        strokeWidth = 1.5f
                    )

                    // Barrido rotatorio del radar
                    rotate(angle, pivot = Offset(centerX, centerY)) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color.Transparent, Gold500.copy(alpha = 0.3f), Color.Transparent),
                                center = Offset(centerX, centerY)
                            ),
                            startAngle = 0f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset(centerX - maxRadius, centerY - maxRadius),
                            size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
                        )
                    }

                    // Indicador central de la embarcación (Punto dorado con anillo pulsante verde)
                    drawCircle(
                        color = Color(0xFF4EBA6F).copy(alpha = 0.4f),
                        radius = 16f,
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Gold500,
                        radius = 7f,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bloque Informativo Inferior
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lat: ${String.format(Locale.US, "%.4f", latitud)} | Lng: ${String.format(Locale.US, "%.4f", longitud)}",
                    color = Ink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Última actualización: $horaActualizada",
                    color = Muted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
