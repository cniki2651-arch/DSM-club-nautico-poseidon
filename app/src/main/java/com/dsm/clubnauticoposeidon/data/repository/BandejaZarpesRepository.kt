package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.ui.screens.naviero.Tripulante
import com.dsm.clubnauticoposeidon.ui.screens.zarpe.SolicitudZarpe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BandejaZarpesRepository {

    private val db = FirebaseFirestore.getInstance()

    fun observarZarpesActivos(): Flow<List<SolicitudZarpe>> = callbackFlow {
        val listener = db.collection("zarpes")
            .whereIn("estado", listOf("pendiente_aprobacion", "aprobado", "activo", "en_mar"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.map { doc ->
                    doc.toObject(SolicitudZarpe::class.java)?.copy(id = doc.id) ?: SolicitudZarpe(id = doc.id)
                } ?: emptyList()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    fun observarTripulantes(): Flow<List<Tripulante>> = callbackFlow {
        val listener = db.collection("tripulantes")
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

    suspend fun aprobarZarpe(zarpeId: String) {
        db.collection("zarpes").document(zarpeId)
            .update("estado", "aprobado").await()
    }

    suspend fun rechazarZarpe(zarpeId: String, motivo: String) {
        db.collection("zarpes").document(zarpeId)
            .update(
                mapOf(
                    "estado" to "rechazado",
                    "motivoRechazo" to motivo
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

    suspend fun registrarTripulante(nombre: String, dni: String, licencia: String, cargo: String) {
        val nuevoTripulante = hashMapOf(
            "nombreCompleto" to nombre,
            "dni" to dni,
            "numeroLicencia" to licencia,
            "cargo" to cargo,
            "estado" to "autorizado"
        )
        db.collection("tripulantes").add(nuevoTripulante).await()
    }
}
