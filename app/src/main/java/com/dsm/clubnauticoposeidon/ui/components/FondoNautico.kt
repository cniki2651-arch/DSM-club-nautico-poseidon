package com.dsm.clubnauticoposeidon.ui.components



import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Navy600
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

// Fondo decorativo con olas animadas y un barco
@Composable
fun FondoNautico() {
    val dosPi = (2 * PI).toFloat()

    val transition = rememberInfiniteTransition(label = "mar")

    // Animación de las olas
    val fase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = dosPi,
        animationSpec = infiniteRepeatable(
            tween(7000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "fase1"
    )

    val fase2 by transition.animateFloat(
        initialValue = dosPi,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "fase2"
    )

    val fase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = dosPi,
        animationSpec = infiniteRepeatable(
            tween(13000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "fase3"
    )

    // Movimiento horizontal del barco
    val barcoX by transition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            tween(40000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "barco"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        // Evitar divisiones entre cero
        if (w <= 0f || h <= 0f) {
            return@Canvas
        }

        // Resplandor dorado detrás del logo
        val centro = Offset(w / 2f, h * 0.30f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Gold500.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                center = centro,
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = centro
        )

        // Calcula la altura de una ola
        fun yOla(
            x: Float,
            altura: Float,
            amplitud: Float,
            ciclos: Float,
            fase: Float
        ): Float {
            return altura +
                    amplitud * sin(
                (x / w) * ciclos * dosPi + fase
            )
        }

        // Dibuja una ola
        fun ola(
            altura: Float,
            amplitud: Float,
            ciclos: Float,
            fase: Float,
            color: Color
        ) {
            val path = Path().apply {
                moveTo(0f, h)

                var x = 0f

                while (x <= w) {
                    lineTo(
                        x,
                        yOla(
                            x,
                            altura,
                            amplitud,
                            ciclos,
                            fase
                        )
                    )
                    x += 6f
                }

                lineTo(
                    w,
                    yOla(
                        w,
                        altura,
                        amplitud,
                        ciclos,
                        fase
                    )
                )

                lineTo(w, h)
                close()
            }

            drawPath(
                path = path,
                color = color
            )
        }

        // Parámetros de las tres olas
        val altura1 = h * 0.84f
        val altura2 = h * 0.89f
        val altura3 = h * 0.94f

        val amp1 = h * 0.012f
        val amp2 = h * 0.015f
        val amp3 = h * 0.012f

        // Ola del fondo
        ola(
            altura1,
            amp1,
            1.5f,
            fase1,
            Navy600.copy(alpha = 0.40f)
        )

        // Posición y orientación del barco
        val bw = w * 0.18f
        val xb = w * barcoX

        val yb = yOla(
            xb,
            altura2,
            amp2,
            1.0f,
            fase2
        )

        val pendiente =
            amp2 *
                    cos(
                        (xb / w) * dosPi + fase2
                    ) *
                    (dosPi / w)

        val grados =
            atan(pendiente) * 180f / PI.toFloat() * 1.5f

        // Dibujo del barco
        rotate(
            degrees = grados,
            pivot = Offset(xb, yb)
        ) {
            fun pt(dx: Float, dy: Float): Offset {
                return Offset(
                    xb + dx * bw,
                    yb + dy * bw
                )
            }

            fun poligono(
                color: Color,
                puntos: List<Offset>
            ) {
                val path = Path().apply {
                    if (puntos.isNotEmpty()) {
                        moveTo(
                            puntos[0].x,
                            puntos[0].y
                        )

                        for (i in 1 until puntos.size) {
                            lineTo(
                                puntos[i].x,
                                puntos[i].y
                            )
                        }

                        close()
                    }
                }

                drawPath(
                    path = path,
                    color = color
                )
            }

            // Casco del barco
            poligono(
                Gold500,
                listOf(
                    pt(-0.50f, -0.05f),
                    pt(0.55f, -0.05f),
                    pt(0.34f, 0.11f),
                    pt(-0.34f, 0.11f)
                )
            )

            // Vela mayor
            poligono(
                Ink.copy(alpha = 0.92f),
                listOf(
                    pt(-0.03f, -0.80f),
                    pt(-0.03f, -0.12f),
                    pt(-0.40f, -0.12f)
                )
            )

            // Vela de proa
            poligono(
                Ink.copy(alpha = 0.80f),
                listOf(
                    pt(0.03f, -0.70f),
                    pt(0.03f, -0.12f),
                    pt(0.32f, -0.12f)
                )
            )

            // Mástil
            drawLine(
                color = Ink,
                start = pt(0f, -0.84f),
                end = pt(0f, -0.05f),
                strokeWidth = bw * 0.025f
            )
        }

        // Ola intermedia
        ola(
            altura2,
            amp2,
            1.0f,
            fase2,
            Navy700.copy(alpha = 0.50f)
        )

        // Ola delantera
        ola(
            altura3,
            amp3,
            2.0f,
            fase3,
            Navy900.copy(alpha = 0.60f)
        )
    }
}