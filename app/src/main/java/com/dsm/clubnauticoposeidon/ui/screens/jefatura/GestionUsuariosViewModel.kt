package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.GestionUsuariosRepository
import com.dsm.clubnauticoposeidon.data.repository.GestionUsuariosRepositoryImpl
import com.dsm.clubnauticoposeidon.model.RolUsuario
import com.dsm.clubnauticoposeidon.model.UsuarioGestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GestionUsuariosUiState(
    val usuarios: List<UsuarioGestion> = emptyList(),
    val usuariosFiltrados: List<UsuarioGestion> = emptyList(),
    val filtroRol: RolUsuario? = null,
    val busquedaQuery: String = "",
    val isLoading: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val mostrarDialogo: Boolean = false,
    val usuarioEnEdicion: UsuarioGestion? = null
)

class GestionUsuariosViewModel(
    private val repository: GestionUsuariosRepository = GestionUsuariosRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState

    init {
        observarUsuarios()
    }

    private fun observarUsuarios() {
        viewModelScope.launch {
            repository.observarUsuarios()
                .catch { e ->
                    Log.e("GestionUsuariosVM", "Error al observar usuarios", e)
                    _uiState.update { it.copy(isLoading = false, mensaje = "Error al cargar usuarios: ${e.message}") }
                }
                .collect { lista ->
                    // Regla de Negocio Estricta: Excluir cuentas de socios (tienen su propio módulo)
                    val listaSinSocios = lista.filter { it.rol != "socio" }
                    _uiState.update { state ->
                        val filtrada = aplicarFiltros(listaSinSocios, state.filtroRol, state.busquedaQuery)
                        state.copy(
                            usuarios = listaSinSocios,
                            usuariosFiltrados = filtrada,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onBusquedaChanged(query: String) {
        _uiState.update { state ->
            val filtrada = aplicarFiltros(state.usuarios, state.filtroRol, query)
            state.copy(busquedaQuery = query, usuariosFiltrados = filtrada)
        }
    }

    fun onFiltroRolChanged(rol: RolUsuario?) {
        _uiState.update { state ->
            val filtrada = aplicarFiltros(state.usuarios, rol, state.busquedaQuery)
            state.copy(filtroRol = rol, usuariosFiltrados = filtrada)
        }
    }

    private fun aplicarFiltros(
        lista: List<UsuarioGestion>,
        rol: RolUsuario?,
        query: String
    ): List<UsuarioGestion> {
        return lista.filter { u ->
            val coincideRol = (rol == null || u.rol == rol.key)
            val queryLower = query.trim().lowercase()
            val coincideQuery = queryLower.isBlank() ||
                    u.nombreCompleto.lowercase().contains(queryLower) ||
                    u.dni.lowercase().contains(queryLower) ||
                    u.correo.lowercase().contains(queryLower)
            coincideRol && coincideQuery
        }
    }

    fun abrirDialogoCrear() {
        _uiState.update { it.copy(mostrarDialogo = true, usuarioEnEdicion = null) }
    }

    fun abrirDialogoEditar(usuario: UsuarioGestion) {
        _uiState.update { it.copy(mostrarDialogo = true, usuarioEnEdicion = usuario) }
    }

    fun cerrarDialogo() {
        _uiState.update { it.copy(mostrarDialogo = false, usuarioEnEdicion = null) }
    }

    fun mensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun guardarUsuario(
        id: String,
        nombre: String,
        apellidos: String,
        correo: String,
        dni: String,
        telefono: String,
        rolKey: String
    ) {
        if (nombre.isBlank() || apellidos.isBlank()) {
            _uiState.update { it.copy(mensaje = "Ingresa el nombre y apellidos completos") }
            return
        }
        if (!correo.contains("@") || !correo.contains(".")) {
            _uiState.update { it.copy(mensaje = "Ingresa un correo electrónico válido") }
            return
        }
        if (dni.length != 8 || !dni.all { it.isDigit() }) {
            _uiState.update { it.copy(mensaje = "El DNI debe tener exactamente 8 dígitos numéricos") }
            return
        }
        if (rolKey == "socio") {
            _uiState.update { it.copy(mensaje = "No puedes asignar el rol 'socio' desde esta pantalla") }
            return
        }

        val esNuevo = id.isBlank()
        val usuario = UsuarioGestion(
            id = id,
            nombre = nombre.trim(),
            apellidos = apellidos.trim(),
            correo = correo.trim().lowercase(),
            dni = dni.trim(),
            telefono = telefono.trim(),
            rol = rolKey.lowercase(),
            estado = _uiState.value.usuarioEnEdicion?.estado ?: "activo"
        )

        _uiState.update { it.copy(guardando = true) }

        viewModelScope.launch {
            val resultado = if (esNuevo) {
                repository.crearUsuario(usuario)
            } else {
                repository.actualizarUsuario(usuario)
            }

            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mostrarDialogo = false,
                            usuarioEnEdicion = null,
                            mensaje = if (esNuevo) "Usuario administrativo registrado" else "Datos del usuario actualizados"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Error al guardar: ${e.message}"
                        )
                    }
                }
            )
        }
    }

    fun cambiarEstadoUsuario(usuario: UsuarioGestion, activo: Boolean) {
        val nuevoEstado = if (activo) "activo" else "inactivo"
        viewModelScope.launch {
            repository.cambiarEstadoUsuario(usuario.id, nuevoEstado).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            mensaje = "Cuenta de ${usuario.nombreCompleto} ${if (activo) "activada" else "desactivada"}"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(mensaje = "Error al cambiar estado: ${e.message}") }
                }
            )
        }
    }
}
