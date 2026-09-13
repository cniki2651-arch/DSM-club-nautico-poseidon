package com.dsm.clubnauticoposeidon.ui.screens.signup

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.clubnauticoposeidon.R
import com.dsm.clubnauticoposeidon.ui.components.AuthHeader
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    auth: FirebaseAuth,
    onLogin: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    
    val documentTypes = listOf("DNI", "Pasaporte", "CE")
    var expanded by remember { mutableStateOf(false) }
    var tipoDocumento by remember { mutableStateOf(documentTypes[0]) }
    
    var numDocumento by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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

        AuthHeader()

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Gold500,
                unfocusedIndicatorColor = Muted
            )

            // Nombres
            TextField(
                value = nombres,
                onValueChange = { nombres = it },
                placeholder = { Text("Nombres", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Apellidos
            TextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                placeholder = { Text("Apellidos", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de Documento
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = tipoDocumento,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Tipo de Documento", color = Muted) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    documentTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, color = Navy900, fontWeight = FontWeight.Medium) },
                            onClick = {
                                tipoDocumento = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Número de Documento
            TextField(
                value = numDocumento,
                onValueChange = { numDocumento = it },
                placeholder = { Text("Número de Documento", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Fecha de Nacimiento
            TextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                placeholder = { Text("Fecha de Nacimiento (DD/MM/AAAA)", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Número Telefónico
            TextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = { Text("Número telefónico", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Correo electrónico
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text(stringResource(R.string.login_email), color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text(stringResource(R.string.login_password), color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
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
            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar Contraseña
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Confirmar contraseña", color = Muted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (confirmPasswordVisible) stringResource(R.string.login_password_ocultar) else stringResource(R.string.login_password_mostrar)

                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = Ink
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || nombres.isBlank() || apellidos.isBlank() || numDocumento.isBlank() || fechaNacimiento.isBlank() || telefono.isBlank()) {
                        Log.e("AUTH", "Faltan campos por llenar")
                    } else if (password != confirmPassword) {
                        Log.e("AUTH", "Las contraseñas no coinciden")
                    } else {
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                Log.d("AUTH", "Usuario creado: ${user?.email}")
                            } else {
                                Log.e("AUTH", "Error: ${task.exception?.message}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Enviar Solicitud", color = Navy900, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val annotatedText = buildAnnotatedString {
                append(stringResource(R.string.signup_login_pregunta))
                append(" ") // Agrega un espacio entre la pregunta y la acción
                pushStringAnnotation(tag = "login", annotation = "login")
                withStyle(style = SpanStyle(color = Gold400, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.signup_login_accion))
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "login", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onLogin()
                        }
                },
                modifier = Modifier.padding(bottom = 32.dp),
                style = TextStyle(color = Ink, fontSize = 14.sp)
            )
        }
    }
}