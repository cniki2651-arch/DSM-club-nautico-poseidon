package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.clubnauticoposeidon.model.RolUsuario
import com.dsm.clubnauticoposeidon.model.UsuarioGestion
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

// Roles permitidos para administración en este módulo (Excluye 'socio')
private val rolesAdministrativos = listOf(
    RolUsuario.NAVIERO,
    RolUsuario.SECRETARIA,
    RolUsuario.FINANZAS,
    RolUsuario.MANTENIMIENTO,
    RolUsuario.JEFE
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onBackClick: () -> Unit,
    viewModel: GestionUsuariosViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestión de Usuarios",
                            color = Gold500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Personal & Roles Administrativos",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.abrirDialogoCrear() },
                containerColor = Gold500,
                contentColor = Navy900
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Usuario",
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Buscador por nombre o DNI
            OutlinedTextField(
                value = state.busquedaQuery,
                onValueChange = { viewModel.onBusquedaChanged(it) },
                placeholder = { Text("Buscar por nombre, correo o DNI...", color = Muted) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Gold500
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500,
                    unfocusedBorderColor = Muted,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    cursorColor = Gold500
                ),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips de Filtro por Rol Administrativo (Excluye socio)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = state.filtroRol == null,
                        onClick = { viewModel.onFiltroRolChanged(null) },
                        label = { Text("Todos (${state.usuarios.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold500,
                            selectedLabelColor = Navy900,
                            containerColor = Navy800,
                            labelColor = Ink
                        )
                    )
                }

                items(rolesAdministrativos) { rol ->
                    val cantidad = state.usuarios.count { it.rolEnum == rol }
                    FilterChip(
                        selected = state.filtroRol == rol,
                        onClick = {
                            viewModel.onFiltroRolChanged(if (state.filtroRol == rol) null else rol)
                        },
                        label = { Text("${rol.displayName} ($cantidad)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Gold500,
                            selectedLabelColor = Navy900,
                            containerColor = Navy800,
                            labelColor = Ink
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contenido Principal (Lista o Indicador de Carga)
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold500)
                }
            } else if (state.usuariosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ManageAccounts,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No se encontraron usuarios administrativos",
                            color = Muted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.usuariosFiltrados, key = { it.id }) { usuario ->
                        TarjetaUsuarioGestion(
                            usuario = usuario,
                            onEditar = { viewModel.abrirDialogoEditar(usuario) },
                            onCambiarEstado = { activo ->
                                viewModel.cambiarEstadoUsuario(usuario, activo)
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de Crear / Editar Usuario
    if (state.mostrarDialogo) {
        CrearEditarUsuarioDialog(
            usuario = state.usuarioEnEdicion,
            guardando = state.guardando,
            onDismiss = { viewModel.cerrarDialogo() },
            onGuardar = { id, nombre, apellidos, correo, dni, telefono, rolKey ->
                viewModel.guardarUsuario(id, nombre, apellidos, correo, dni, telefono, rolKey)
            }
        )
    }
}

@Composable
fun TarjetaUsuarioGestion(
    usuario: UsuarioGestion,
    onEditar: () -> Unit,
    onCambiarEstado: (Boolean) -> Unit
) {
    val esActivo = usuario.estado.lowercase() == "activo"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (esActivo) Gold500.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usuario.nombreCompleto,
                    color = Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Badge del Rol Administrativo
                Surface(
                    color = Gold500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Gold500)
                ) {
                    Text(
                        text = usuario.rolEnum.displayName.uppercase(),
                        color = Gold500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Correo: ${usuario.correo}",
                color = Muted,
                fontSize = 13.sp
            )
            Text(
                text = "DNI: ${usuario.dni}  |  Teléfono: ${usuario.telefono.ifBlank { "N/A" }}",
                color = Muted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de Estado (Activo/Inactivo)
                Surface(
                    color = if (esActivo) Color(0xFF1B4332) else Color(0xFF3A1C1C),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (esActivo) "🟢 ACTIVO" else "🔴 INACTIVO",
                        color = if (esActivo) Color(0xFF4EBA6F) else Color(0xFFE57373),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEditar,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Gold500),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold500)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Switch(
                        checked = esActivo,
                        onCheckedChange = onCambiarEstado,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Navy900,
                            checkedTrackColor = Color(0xFF4EBA6F),
                            uncheckedThumbColor = Ink,
                            uncheckedTrackColor = Color(0xFF3A1C1C)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEditarUsuarioDialog(
    usuario: UsuarioGestion?,
    guardando: Boolean,
    onDismiss: () -> Unit,
    onGuardar: (id: String, nombre: String, apellidos: String, correo: String, dni: String, telefono: String, rolKey: String) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var apellidos by remember { mutableStateOf(usuario?.apellidos ?: "") }
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    var dni by remember { mutableStateOf(usuario?.dni ?: "") }
    var telefono by remember { mutableStateOf(usuario?.telefono ?: "") }
    var rolSeleccionado by remember { mutableStateOf(usuario?.rolEnum ?: rolesAdministrativos[0]) }
    var expandedRol by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold500,
        unfocusedBorderColor = Muted,
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Gold500
    )

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        containerColor = Navy900,
        title = {
            Text(
                text = if (usuario == null) "Registrar Personal / Rol" else "Editar Usuario Administrativo",
                color = Gold500,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre", color = Muted) },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos", color = Muted) },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it.filter { c -> c.isDigit() }.take(8) },
                    label = { Text("DNI (8 dígitos)", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { c -> c.isDigit() || c == '+' }.take(15) },
                    label = { Text("Teléfono", color = Muted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector Desplegable de Roles Administrativos (Excluye 'socio')
                ExposedDropdownMenuBox(
                    expanded = expandedRol,
                    onExpandedChange = { expandedRol = !expandedRol }
                ) {
                    OutlinedTextField(
                        value = rolSeleccionado.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol Asignado", color = Muted) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedRol,
                        onDismissRequest = { expandedRol = false },
                        modifier = Modifier.background(Navy800)
                    ) {
                        rolesAdministrativos.forEach { rol ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = rol.displayName,
                                        color = Ink,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    rolSeleccionado = rol
                                    expandedRol = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        usuario?.id ?: "",
                        nombre,
                        apellidos,
                        correo,
                        dni,
                        telefono,
                        rolSeleccionado.key
                    )
                },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = Gold500)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Navy900, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (usuario == null) "Registrar" else "Guardar Cambios",
                        color = Navy900,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !guardando
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}
