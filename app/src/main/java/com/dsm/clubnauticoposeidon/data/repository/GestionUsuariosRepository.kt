package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.UsuarioGestion
import kotlinx.coroutines.flow.Flow

interface GestionUsuariosRepository {
    fun observarUsuarios(): Flow<List<UsuarioGestion>>
    suspend fun crearUsuario(usuario: UsuarioGestion): Result<Unit>
    suspend fun actualizarUsuario(usuario: UsuarioGestion): Result<Unit>
    suspend fun cambiarEstadoUsuario(usuarioId: String, nuevoEstado: String): Result<Unit>
}
