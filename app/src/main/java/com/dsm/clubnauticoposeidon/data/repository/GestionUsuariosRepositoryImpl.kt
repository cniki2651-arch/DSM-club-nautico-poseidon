package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.UsuarioGestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GestionUsuariosRepositoryImpl(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GestionUsuariosRepository {

    private val usuariosRef = db.collection("usuarios")

    override fun observarUsuarios(): Flow<List<UsuarioGestion>> = callbackFlow {
        val listener = usuariosRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val lista = snapshot?.documents?.mapNotNull { doc ->
                val u = doc.toObject(UsuarioGestion::class.java)
                u?.copy(id = doc.id)
            } ?: emptyList()
            trySend(lista)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun crearUsuario(usuario: UsuarioGestion): Result<Unit> = runCatching {
        val docRef = if (usuario.id.isNotBlank()) {
            usuariosRef.document(usuario.id)
        } else {
            usuariosRef.document()
        }

        val datos = hashMapOf(
            "nombre" to usuario.nombre,
            "apellidos" to usuario.apellidos,
            "correo" to usuario.correo.trim().lowercase(),
            "dni" to usuario.dni,
            "telefono" to usuario.telefono,
            "rol" to usuario.rol.lowercase(),
            "estado" to usuario.estado.lowercase(),
            "fechaRegistro" to if (usuario.fechaRegistro > 0) usuario.fechaRegistro else System.currentTimeMillis()
        )

        docRef.set(datos).await()
    }

    override suspend fun actualizarUsuario(usuario: UsuarioGestion): Result<Unit> = runCatching {
        require(usuario.id.isNotBlank()) { "El ID del usuario no puede estar vacío" }

        val updates = mapOf(
            "nombre" to usuario.nombre,
            "apellidos" to usuario.apellidos,
            "correo" to usuario.correo.trim().lowercase(),
            "dni" to usuario.dni,
            "telefono" to usuario.telefono,
            "rol" to usuario.rol.lowercase(),
            "estado" to usuario.estado.lowercase()
        )

        usuariosRef.document(usuario.id).update(updates).await()
    }

    override suspend fun cambiarEstadoUsuario(usuarioId: String, nuevoEstado: String): Result<Unit> = runCatching {
        require(usuarioId.isNotBlank()) { "El ID del usuario no puede estar vacío" }
        usuariosRef.document(usuarioId).update("estado", nuevoEstado.lowercase()).await()
    }
}
