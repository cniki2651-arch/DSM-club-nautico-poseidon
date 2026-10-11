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
import kotlin.math.sin

// Medidas compartidas: "Crear cuenta" y "Continuar con Google" quedan idénticos
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
        // Decoración de fondo: resplandor dorado + olas en movimiento
        FondoNautico()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // El logo ocupa todo el círculo; el aro dorado queda justo en su borde
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.login_logo_descripcion),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .border(3.dp, Gold500, CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.welcome_titulo),
                style = TituloNautico,
                color = Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
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
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// Fondo decorativo dibujado con código (no necesita imágenes nuevas)
@Composable
private fun FondoNautico() {
    val dosPi = (2 * PI).toFloat()
    val transition = rememberInfiniteTransition(label = "olas")

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

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Resplandor dorado suave detrás del logo
        val centro = Offset(w / 2f, h * 0.22f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Gold500.copy(alpha = 0.20f), Color.Transparent),
                center = centro,
                radius = w * 0.85f
            ),
            radius = w * 0.85f,
            center = centro
        )

        // Ola ondulada: sinusoide que se desplaza según la fase
        fun ola(altura: Float, amplitud: Float, ciclos: Float, fase: Float, color: Color) {
            val path = Path().apply {
                moveTo(0f, h)
                var x = 0f
                while (x <= w) {
                    val y = altura + amplitud * sin((x / w) * ciclos * dosPi + fase)
                    lineTo(x, y)
                    x += 6f
                }
                lineTo(w, altura + amplitud * sin(ciclos * dosPi + fase))
                lineTo(w, h)
                close()
            }
            drawPath(path, color)
        }

        ola(h * 0.85f, h * 0.012f, 1.5f, fase1.value, Navy700.copy(alpha = 0.40f))
        ola(h * 0.90f, h * 0.015f, 1.0f, fase2.value, Navy600.copy(alpha = 0.35f))
        ola(h * 0.95f, h * 0.012f, 2.0f, fase3.value, Navy700.copy(alpha = 0.45f))
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