package com.dsm.clubnauticoposeidon.data.repository

import android.util.Log
import com.dsm.clubnauticoposeidon.model.ReporteDanio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReporteDanioRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coleccion = firestore.collection("reportes_danio")
    private val coleccionServicios = firestore.collection("solicitudes_servicios")

    /**
     * El socio crea un reporte de daño para una de sus embarcaciones registradas.
     * Se registra en 'reportes_danio' y también en 'solicitudes_servicios' con módulo 'asistencia'.
     */
    suspend fun crearReporte(reporte: ReporteDanio): String {
        val user = auth.currentUser
        val email = user?.email.orEmpty()
        val uid = user?.uid.orEmpty()

        val reporteAjustado = reporte.copy(
            socioUid = uid,
            socioEmail = email,
            fechaReporte = System.currentTimeMillis(),
            estado = "pendiente"
        )
        val docRef = coleccion.add(reporteAjustado).await()

        try {
            val servicioData = hashMapOf(
                "modulo" to "asistencia",
                "tipo_solicitud" to "reporte_danio",
                "descripcion" to "[REPORTE DE DAÑO] ${reporte.nombreNave} (${reporte.matricula}): ${reporte.descripcion}",
                "ubicacion_rada" to "Yate: ${reporte.nombreNave} [${reporte.matricula}]",
                "estado" to "pendiente",
                "fecha_solicitud" to FieldValue.serverTimestamp(),
                "dni_socio" to reporte.socioDni,
                "socio_email" to email,
                "total_pagar" to 0.0,
                "reporte_danio_id" to docRef.id,
                "items_solicitados" to listOf(
                    mapOf(
                        "nombre" to "Inspección técnica de avería - ${reporte.nombreNave}",
                        "cantidad" to 1
                    )
                )
            )
            val docServicio = coleccionServicios.add(servicioData).await()
            docRef.update("solicitud_servicio_id", docServicio.id).await()
        } catch (e: Exception) {
            Log.e("REPORTE_DANIO", "Error al crear mirror en solicitudes_servicios", e)
        }

        return docRef.id
    }

    /**
     * Escucha en tiempo real todos los reportes de daño pendientes (para Mantenimiento).
     * Con protección total ante errores para evitar cierres abruptos.
     */
    fun observarReportesPendientes(): Flow<List<ReporteDanio>> = callbackFlow {
        val listener = try {
            coleccion
                .whereIn("estado", listOf("pendiente", "en_inspeccion"))
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("REPORTE_DANIO", "Error en observarReportesPendientes: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val lista = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(ReporteDanio::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("REPORTE_DANIO", "Error al deserializar reporte ${doc.id}", e)
                            null
                        }
                    }.orEmpty().sortedByDescending { it.fechaReporte }
                    trySend(lista)
                }
        } catch (e: Exception) {
            Log.e("REPORTE_DANIO", "Error inicial en observarReportesPendientes", e)
            trySend(emptyList())
            null
        }

        awaitClose { listener?.remove() }
    }

    /**
     * Escucha los reportes de un socio específico por DNI.
     * Con protección total ante errores para evitar cierres abruptos.
     */
    fun observarReportesSocio(dni: String): Flow<List<ReporteDanio>> = callbackFlow {
        if (dni.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = try {
            coleccion
                .whereEqualTo("socio_dni", dni)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("REPORTE_DANIO", "Error en observarReportesSocio: ${error.message}")
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val lista = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(ReporteDanio::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Log.e("REPORTE_DANIO", "Error al deserializar reporte del socio ${doc.id}", e)
                            null
                        }
                    }.orEmpty().sortedByDescending { it.fechaReporte }
                    trySend(lista)
                }
        } catch (e: Exception) {
            Log.e("REPORTE_DANIO", "Error inicial en observarReportesSocio", e)
            trySend(emptyList())
            null
        }

        awaitClose { listener?.remove() }
    }

    /**
     * Actualiza el estado de un reporte de daño.
     */
    suspend fun actualizarEstado(reporteId: String, nuevoEstado: String) {
        try {
            coleccion.document(reporteId)
                .update("estado", nuevoEstado)
                .await()
        } catch (e: Exception) {
            Log.e("REPORTE_DANIO", "Error al actualizar estado", e)
        }
    }

    /**
     * Vincula un reporte del socio con la evaluación de avería del operario.
     */
    suspend fun vincularEvaluacion(reporteId: String, evaluacionAveriaId: String) {
        try {
            val doc = coleccion.document(reporteId).get().await()
            val servicioId = doc.getString("solicitud_servicio_id").orEmpty()

            coleccion.document(reporteId)
                .update(
                    mapOf(
                        "estado" to "en_reparacion",
                        "evaluacion_averia_id" to evaluacionAveriaId
                    )
                )
                .await()

            if (servicioId.isNotBlank()) {
                try {
                    coleccionServicios.document(servicioId)
                        .update("estado", "en_reparacion")
                        .await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("REPORTE_DANIO", "Error al vincular evaluación", e)
        }
    }
}
