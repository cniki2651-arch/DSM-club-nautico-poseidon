package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Embarcaciones del socio en Firestore: listar, registrar (pantalla Mis naves)
class EmbarcacionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val coleccion = firestore.collection("embarcaciones")

    // Busca el DNI del usuario logueado en la colección "usuarios"
    suspend fun obtenerDniActual(): String? {
        val uid = auth.currentUser?.uid ?: return null

        val porId = firestore.collection("usuarios").document(uid).get().await()
        porId.get("dni")?.toString()?.takeIf { it.isNotBlank() }?.let { return it }

        val porCampo = firestore.collection("usuarios")
            .whereEqualTo("uid", uid)
            .limit(1)
            .get()
            .await()
        return porCampo.documents.firstOrNull()?.get("dni")?.toString()
    }

    // Escucha en tiempo real las naves del socio
    fun observarNaves(dni: String): Flow<List<Embarcacion>> = callbackFlow {
        val registro = coleccion
            .whereEqualTo("dni_socio", dni)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val naves = snapshot?.toObjects(Embarcacion::class.java).orEmpty()
                    .sortedBy { it.nombreNave.lowercase() }
                trySend(naves)
            }
        awaitClose { registro.remove() }
    }

    // Registra una nave nueva; la matrícula no se puede repetir
    suspend fun registrarNave(
        dni: String,
        matricula: String,
        nombre: String,
        tipo: String,
        eslora: Double
    ) {
        require(matricula.isNotBlank()) { "Ingresa la matrícula" }
        require(nombre.isNotBlank()) { "Ingresa el nombre de la nave" }

        val repetida = coleccion
            .whereEqualTo("matricula", matricula)
            .limit(1)
            .get()
            .await()
        if (!repetida.isEmpty) {
            throw IllegalStateException("Ya existe una embarcación con la matrícula $matricula")
        }

        val datos = hashMapOf(
            "matricula" to matricula,
            "nombre_nave" to nombre,
            "tipo" to tipo,
            "eslora" to eslora,
            "dni_socio" to dni
        )
        coleccion.add(datos).await()
    }
}