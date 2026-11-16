package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.ui.screens.naviero.Tripulante
import com.dsm.clubnauticoposeidon.ui.screens.zarpe.SolicitudZarpe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ZarpeRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun verificarZarpeActivo(embarcacionId: String): SolicitudZarpe? {
        if (embarcacionId.isBlank()) return null
        val query = db.collection("zarpes")
            .whereEqualTo("embarcacionId", embarcacionId)
            .whereIn("estado", listOf("pendiente_aprobacion", "aprobado", "en_mar", "activo", "rechazado"))
            .limit(1)
            .get()
            .await()

        val doc = query.documents.firstOrNull() ?: return null
        return doc.toObject(SolicitudZarpe::class.java)?.copy(id = doc.id)
    }

    suspend fun obtenerUsuarioActual(userId: String): DocumentSnapshot {
        return db.collection("usuarios").document(userId).get().await()
    }

    suspend fun solicitarZarpe(nuevoZarpe: SolicitudZarpe) {
        val zarpeRef = db.collection("zarpes").document(nuevoZarpe.id.ifBlank { db.collection("zarpes").document().id })
        val zarpeConId = nuevoZarpe.copy(id = zarpeRef.id)
        zarpeRef.set(zarpeConId).await()
    }

    suspend fun actualizarZarpeEnMar(zarpeId: String, lat: Double, lng: Double) {
        db.collection("zarpes").document(zarpeId)
            .update(
                mapOf(
                    "estado" to "en_mar",
                    "latitud" to lat,
                    "longitud" to lng,
                    "ultimaActualizacion" to System.currentTimeMillis()
                )
            ).await()
    }

    suspend fun finalizarZarpe(zarpeId: String) {
        db.collection("zarpes").document(zarpeId)
            .update(
                mapOf(
                    "estado" to "finalizado",
                    "fechaArribo" to System.currentTimeMillis()
                )
            ).await()
    }

    fun observarTripulantesAutorizados(): Flow<List<Tripulante>> = callbackFlow {
        val listener = db.collection("tripulantes")
            .whereEqualTo("estado", "autorizado")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.map { doc ->
                    Tripulante(
                        id = doc.id,
                        nombreCompleto = doc.getString("nombreCompleto") ?: "",
                        dni = doc.getString("dni") ?: "",
                        numeroLicencia = doc.getString("numeroLicencia") ?: "",
                        cargo = doc.getString("cargo") ?: "",
                        estado = doc.getString("estado") ?: "autorizado"
                    )
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }
}
