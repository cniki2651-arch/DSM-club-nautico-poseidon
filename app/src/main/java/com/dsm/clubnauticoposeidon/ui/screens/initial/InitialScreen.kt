package com.dsm.clubnauticoposeidon.ui.screens.initial

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dsm.clubnauticoposeidon.R
import com.dsm.clubnauticoposeidon.ui.screens.login.signInWithGoogle
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Navy600
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.dsm.clubnauticoposeidon.ui.theme.TituloNautico
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

// ---- Medidas ajustables ----
// Diámetro del círculo del logo (el aro dorado y la imagen miden lo mismo)
private val TamanoLogo = 240.dp

// El logo.png ya está recortado exactamente al círculo, así que no necesita escala.
// (Solo súbelos, por ejemplo a 1.05f, si notas un borde fino entre el logo y el aro)
private const val EscalaLogoX = 1.0f
private const val EscalaLogoY = 1.0f

// Espacio entre el título y el botón "Crear cuenta"
private val EspacioTituloBotones = 40.dp

// "Crear cuenta" y "Continuar con Google" usan exactamente las mismas medidas
private val AlturaBoton = 48.dp
private val MargenBoton = 32.dp

@Composable
fun InitialScreen(
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onNavigateToSocio: () -> Unit = {},
    onNavigateToSeguimiento: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSecretaria: () -> Unit = {},
    onNavigateToJefe: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.google_web_client_id)
    var cargando by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy900, Navy800)))
    ) {
        // Decoración de fondo: resplandor dorado, 3 olas en movimiento y un barco
        FondoNautico()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // El logo llena todo el círculo; el aro dorado queda en su borde
            Box(
                modifier = Modifier
                    .size(TamanoLogo)
                    .clip(CircleShape)
                    .border(3.dp, Gold500, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.login_logo_descripcion),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = EscalaLogoX, scaleY = EscalaLogoY)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.welcome_titulo),
                style = TituloNautico,
                color = Ink,
                textAlign = TextAlign.Center
            )

            // Botones justo debajo del título (ya no se empujan al fondo)
            Spacer(modifier = Modifier.height(EspacioTituloBotones))
            Button(
                onClick = { onSignUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MargenBoton)
                    .height(AlturaBoton),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                Text(
                    text = stringResource(R.string.welcome_crear_cuenta),
                    color = Navy900,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Botón Google
            CustomButton(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.google),
                title = stringResource(R.string.welcome_google),
                enabled = !cargando,
                onClick = {
                    cargando = true
                    scope.launch {
                        signInWithGoogle(
                            context = context,
                            auth = FirebaseAuth.getInstance(),
                            webClientId = webClientId,
                            onSocio = {
                                cargando = false
                                onNavigateToSocio()
                            },
                            onAdmin = {
                                cargando = false
                                onNavigateToAdmin()
                            },
                            onSeguimiento = {
                                cargando = false
                                onNavigateToSeguimiento()
                            },
                            onSecretaria = {
                                cargando = false
                                onNavigateToSecretaria()
                            },
                            onJefe = {
                                cargando = false
                                onNavigateToJefe()
                            },
                            onError = { msg ->
                                cargando = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }
            )

            Text(
                text = stringResource(R.string.welcome_iniciar_sesion),
                color = Gold400,
                modifier = Modifier
                    .padding(24.dp)
                    .clickable { onLogin() },
                fontWeight = FontWeight.Bold
            )

            // Un poco más de espacio abajo para dejar ver las olas y el barco
            Spacer(modifier = Modifier.weight(1.4f))
        }
    }
}

// Fondo decorativo dibujado con código (no necesita imágenes nuevas)
@Composable
private fun FondoNautico() {
    val dosPi = (2 * PI).toFloat()
    val transition = rememberInfiniteTransition(label = "mar")

    // Cada ola avanza a distinta velocidad y dirección
    val fase1 = transition.animateFloat(
        initialValue = 0f,
        targetValue = dosPi,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "fase1"
    )
    val fase2 = transition.animateFloat(
        initialValue = dosPi,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "fase2"
    )
    val fase3 = transition.animateFloat(
        initialValue = 0f,
        targetValue = dosPi,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Restart),
        label = "fase3"
    )
    // Posición horizontal del barco (de izquierda a derecha, 40 s por travesía)
    val barcoX = transition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Restart),
        label = "barco"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Resplandor dorado suave detrás del logo
        val centro = Offset(w / 2f, h * 0.30f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Gold500.copy(alpha = 0.20f), Color.Transparent),
                center = centro,
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = centro
        )

        // Altura de una ola en la posición x
        fun yOla(x: Float, altura: Float, amplitud: Float, ciclos: Float, fase: Float): Float =
            altura + amplitud * sin((x / w) * ciclos * dosPi + fase)

        // Dibuja una ola rellena hasta el fondo de la pantalla
        fun ola(altura: Float, amplitud: Float, ciclos: Float, fase: Float, color: Color) {
            val path = Path().apply {
                moveTo(0f, h)
                var x = 0f
                while (x <= w) {
                    lineTo(x, yOla(x, altura, amplitud, ciclos, fase))
                    x += 6f
                }
                lineTo(w, yOla(w, altura, amplitud, ciclos, fase))
                lineTo(w, h)
                close()
            }
            drawPath(path, color)
        }

        // Parámetros de las 3 olas
        val altura1 = h * 0.84f
        val altura2 = h * 0.89f
        val altura3 = h * 0.94f
        val amp1 = h * 0.012f
        val amp2 = h * 0.015f
        val amp3 = h * 0.012f

        // 1) Ola de atrás
        ola(altura1, amp1, 1.5f, fase1.value, Navy600.copy(alpha = 0.40f))

        // 2) Barco: navega sobre la ola del medio, sube y baja y se inclina con ella
        val bw = w * 0.18f
        val xb = w * barcoX.value
        val yb = yOla(xb, altura2, amp2, 1.0f, fase2.value)
        val pendiente = amp2 * cos((xb / w) * 1.0f * dosPi + fase2.value) * (1.0f * dosPi / w)
        val grados = atan(pendiente) * 180f / PI.toFloat() * 1.5f

        rotate(degrees = grados, pivot = Offset(xb, yb)) {
            fun pt(dx: Float, dy: Float) = Offset(xb + dx * bw, yb + dy * bw)

            fun poligono(color: Color, puntos: List<Offset>) {
                val path = Path().apply {
                    moveTo(puntos[0].x, puntos[0].y)
                    for (i in 1 until puntos.size) lineTo(puntos[i].x, puntos[i].y)
                    close()
                }
                drawPath(path, color)
            }

            // Casco
            poligono(
                Gold500,
                listOf(pt(-0.50f, -0.05f), pt(0.55f, -0.05f), pt(0.34f, 0.11f), pt(-0.34f, 0.11f))
            )
            // Vela mayor (atrás)
            poligono(
                Ink.copy(alpha = 0.92f),
                listOf(pt(-0.03f, -0.80f), pt(-0.03f, -0.12f), pt(-0.40f, -0.12f))
            )
            // Vela de proa (adelante)
            poligono(
                Ink.copy(alpha = 0.80f),
                listOf(pt(0.03f, -0.70f), pt(0.03f, -0.12f), pt(0.32f, -0.12f))
            )
            // Mástil
            drawLine(
                color = Ink,
                start = pt(0f, -0.84f),
                end = pt(0f, -0.05f),
                strokeWidth = bw * 0.025f
            )
        }

        // 3) Ola del medio (cubre un poco el casco) y 4) ola del frente, más oscura
        ola(altura2, amp2, 1.0f, fase2.value, Navy700.copy(alpha = 0.50f))
        ola(altura3, amp3, 2.0f, fase3.value, Navy900.copy(alpha = 0.60f))
    }
}

@Composable
fun CustomButton(
    modifier: Modifier,
    painter: Painter,
    title: String,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MargenBoton)
            .height(AlturaBoton)
            .clip(CircleShape)
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick)
                else Modifier
            )
            .background(Navy700)
            .border(1.dp, Navy600, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(16.dp)
        )
        Text(
            text = title,
            color = Ink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CustomIconButton(modifier: Modifier, title: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MargenBoton)
            .height(AlturaBoton)
            .clip(CircleShape)
            .background(Navy700)
            .border(1.dp, Navy600, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "",
            tint = Gold400,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(18.dp)
        )
        Text(
            text = title,
            color = Ink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}