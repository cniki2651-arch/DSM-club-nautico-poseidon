package com.dsm.clubnauticoposeidon.ui.screens.initial

import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.R
import com.dsm.clubnauticoposeidon.ui.components.AlturaBoton
import com.dsm.clubnauticoposeidon.ui.components.CustomButton
import com.dsm.clubnauticoposeidon.ui.components.FondoNautico
import com.dsm.clubnauticoposeidon.ui.components.MargenBoton
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.dsm.clubnauticoposeidon.ui.theme.TituloNautico

// Configuración del logo y los botones
private val TamanoLogo = 240.dp

private const val EscalaLogoX = 1.0f
private const val EscalaLogoY = 1.0f

private val EspacioTituloBotones = 40.dp

@Composable
fun InitialScreen(
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onNavigateToSocio: () -> Unit = {},
    onNavigateToSeguimiento: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSecretaria: () -> Unit = {},
    onNavigateToJefe: () -> Unit = {},
    onNavigateToFinanzas: () -> Unit = {},
    onNavigateToMantenimiento: () -> Unit = {},
    viewModel: InitialViewModel = viewModel()
) {
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)

    val state by viewModel.state.collectAsStateWithLifecycle()

    // Reacciona a los eventos del ViewModel (navegación y mensajes)
    LaunchedEffect(Unit) {
        viewModel.events.collect { evento ->
            when (evento) {
                is InitialEvent.Navegar -> when (evento.destino) {
                    DestinoInicial.SOCIO -> onNavigateToSocio()
                    DestinoInicial.ADMIN -> onNavigateToAdmin()
                    DestinoInicial.SECRETARIA -> onNavigateToSecretaria()
                    DestinoInicial.JEFE -> onNavigateToJefe()
                    DestinoInicial.SEGUIMIENTO -> onNavigateToSeguimiento()
                }

                is InitialEvent.MensajeRes ->
                    Toast.makeText(context, evento.resId, Toast.LENGTH_LONG).show()

                is InitialEvent.Mensaje ->
                    Toast.makeText(context, evento.texto, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Navy900, Navy800)
                )
            )
    ) {
        // Fondo decorativo
        FondoNautico()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Logo circular
            Box(
                modifier = Modifier
                    .size(TamanoLogo)
                    .clip(CircleShape)
                    .border(3.dp, Gold500, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(
                        R.string.login_logo_descripcion
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = EscalaLogoX,
                            scaleY = EscalaLogoY
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título de bienvenida
            Text(
                text = stringResource(R.string.welcome_titulo),
                style = TituloNautico,
                color = Ink,
                textAlign = TextAlign.Center
            )

            // Botón crear cuenta
            Spacer(
                modifier = Modifier.height(EspacioTituloBotones)
            )

            Button(
                onClick = { onSignUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MargenBoton)
                    .height(AlturaBoton),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold500
                )
            ) {
                Text(
                    text = stringResource(
                        R.string.welcome_crear_cuenta
                    ),
                    color = Navy900,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de inicio de sesión con Google
            CustomButton(
                modifier = Modifier,
                painter = painterResource(id = R.drawable.google),
                title = stringResource(R.string.welcome_google),
                enabled = !state.cargando,
                onClick = { viewModel.onGoogleSignIn(context, webClientId) }
            )

            // Botón de inicio de sesión tradicional
            Text(
                text = stringResource(
                    R.string.welcome_iniciar_sesion
                ),
                color = Gold400,
                modifier = Modifier
                    .padding(24.dp)
                    .clickable {
                        onLogin()
                    },
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1.4f))
        }
    }
}