package com.dsm.clubnauticoposeidon.ui.screens.login

import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.R
import com.dsm.clubnauticoposeidon.ui.components.AuthHeader
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// 3. La Vista (LoginScreen)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context.findActivity()
    val uiState by viewModel.uiState.collectAsState()

    val isCargando = uiState is LoginState.Loading

    // Reaccionar a los cambios de estado del ViewModel
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is LoginState.Success -> {
                viewModel.resetState()
                onLoginSuccess()
            }
            is LoginState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .verticalScroll(rememberScrollState())
            // Deja libre la barra de botones del sistema para que no tape el texto de abajo
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón de Retroceso en la esquina superior izquierda
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.Start)
                .statusBarsPadding()
                .padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver atrás",
                tint = Color.White
            )
        }

        // Cabecera a todo el ancho de la pantalla
        AuthHeader()

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = email,
                onValueChange = { email = it.filter { c -> !c.isWhitespace() } },
                placeholder = { Text(stringResource(R.string.login_email), color = Muted) },
                singleLine = true,
                enabled = !isCargando,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(stringResource(R.string.login_password), color = Muted) },
                singleLine = true,
                enabled = !isCargando,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Ink,
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Gold500,
                    unfocusedIndicatorColor = Muted
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) stringResource(R.string.login_password_ocultar) else stringResource(R.string.login_password_mostrar)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = Ink
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enlace "¿Olvidaste tu contraseña?" alineado a la derecha
            Text(
                text = stringResource(R.string.login_olvidaste_clave),
                color = Gold400,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clickable(enabled = !isCargando) { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón principal de Iniciar Sesión
            Button(
                onClick = {
                    val correo = email.trim().lowercase()
                    if (correo.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Correo o contraseña vacíos", Toast.LENGTH_SHORT).show()
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                        Toast.makeText(context, "Formato de correo inválido", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.iniciarSesion(correo, password)
                    }
                },
                enabled = !isCargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(50)
            ) {
                if (isCargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Navy900,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_inicio),
                        color = Navy900,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de huella digital (HU01 - Login Biométrico)
            IconButton(
                onClick = {
                    if (activity != null) {
                        BiometricHelper.showBiometricPrompt(
                            activity = activity,
                            title = "Acceso al Club Poseidón",
                            subtitle = "Autentícate con tu huella digital para ingresar",
                            onSuccess = {
                                viewModel.handleBiometricLogin(
                                    onNoSession = {
                                        Toast.makeText(context, "Inicie sesión con correo y contraseña por primera vez", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onError = { _, errString ->
                                Toast.makeText(context, "Error biométrico: $errString", Toast.LENGTH_SHORT).show()
                            },
                            onFailed = {
                                Toast.makeText(context, "Huella no reconocida", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Dispositivo no compatible con huella digital", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isCargando,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Acceso Biométrico",
                    tint = Gold500,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val annotatedText = buildAnnotatedString {
                append(stringResource(R.string.login_registro_pregunta))
                append(" ")
                pushStringAnnotation(tag = "signup", annotation = "signup")
                withStyle(style = SpanStyle(color = Gold400, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.login_registro_accion))
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    if (!isCargando) {
                        annotatedText.getStringAnnotations(tag = "signup", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onSignUp()
                            }
                    }
                },
                // Espacio extra abajo para separarlo de los botones del sistema
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
                style = TextStyle(color = Ink, fontSize = 14.sp)
            )
        }
    }
}

fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}