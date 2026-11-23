package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EvaluacionAveria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AveriasRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coleccionReportes = firestore.collection("reportes_averias")
    private val coleccionEmbarcaciones = firestore.collection("embarcaciones")

    /**
     * Guarda un nuevo reporte de avería evaluado por el operario.
     */
    suspend fun guardarReporte(reporte: EvaluacionAveria): String {
        val user = auth.currentUser
        val email = user?.email.orEmpty()
        val uid = user?.uid.orEmpty()

        val reporteAjustado = reporte.copy(
            operarioEmail = email,
            operarioUid = uid,
            fechaRegistro = System.currentTimeMillis()
        )

        val docRef = coleccionReportes.add(reporteAjustado).await()

        // Si este reporte proviene de un reporte de daño de un socio, vincularlo
        if (reporte.reporteDanioId.isNotBlank()) {
            try {
                ReporteDanioRepository().vincularEvaluacion(reporte.reporteDanioId, docRef.id)
            } catch (_: Exception) {}
        }

        return docRef.id
    }

    /**
     * Escucha en tiempo real la lista de reportes ordenados por fecha descendente.
     */
    fun observarReportes(): Flow<List<EvaluacionAveria>> = callbackFlow {
        val listener = coleccionReportes
            .orderBy("fecha_registro", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lista = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(EvaluacionAveria::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(lista)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Obtiene todas las embarcaciones registradas para el selector de yates.
     */
    suspend fun obtenerEmbarcaciones(): List<Embarcacion> {
        return try {
            val snapshot = coleccionEmbarcaciones.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Embarcacion::class.java)?.copy(id = doc.id)
            }.sortedBy { it.nombreNave.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Actualiza el estado de una avería (ej. "en_reparacion", "resuelto").
     */
    suspend fun actualizarEstado(reporteId: String, nuevoEstado: String) {
        coleccionReportes.document(reporteId)
            .update("estado", nuevoEstado)
            .await()
    }
}
