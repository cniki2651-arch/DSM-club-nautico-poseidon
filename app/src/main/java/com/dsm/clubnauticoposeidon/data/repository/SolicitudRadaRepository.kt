package com.dsm.clubnauticoposeidon.data.repository


import com.dsm.clubnauticoposeidon.model.Asignacion
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.EstadoSolicitud
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SolicitudRadaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ref = db.collection("solicitudes_rada")
    private val radasRef = db.collection("radas")
    private val asignacionesRef = db.collection("asignaciones")

    private fun observar(query: Query): Flow<List<SolicitudRada>> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(SolicitudRada::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    // Solicitudes de un socio (todas)
    fun observarPorDni(dni: String): Flow<List<SolicitudRada>> =
        observar(ref.whereEqualTo("dni_socio", dni))

    // Bandeja del naviero
    fun observarPendientes(): Flow<List<SolicitudRada>> =
        observar(ref.whereEqualTo("estado", EstadoSolicitud.PENDIENTE))

    suspend fun crear(nave: Embarcacion, fechaIngreso: String, fechaSalida: String) {
        val pendiente = ref
            .whereEqualTo("matricula", nave.matricula)
            .whereEqualTo("estado", EstadoSolicitud.PENDIENTE)
            .limit(1).get().await()
        if (!pendiente.isEmpty) throw IllegalStateException("Esa nave ya tiene una solicitud pendiente")

        val activa = asignacionesRef
            .whereEqualTo("matricula", nave.matricula)
            .whereEqualTo("estado", "activa")
            .limit(1).get().await()
        if (!activa.isEmpty) throw IllegalStateException("Esa nave ya tiene una rada asignada")

        ref.add(
            hashMapOf(
                "matricula" to nave.matricula,
                "nombre_nave" to nave.nombreNave,
                "tipo" to nave.tipo,
                "eslora" to nave.eslora,
                "dni_socio" to nave.dniSocio,
                "fecha_ingreso" to fechaIngreso,
                "fecha_salida" to fechaSalida,
                "estado" to EstadoSolicitud.PENDIENTE,
                "rada_asignada" to "",
                "motivo_rechazo" to "",
                "fecha_solicitud" to System.currentTimeMillis()
            )
        ).await()
    }

    // Aprueba: crea la asignación, ocupa la rada y cierra la solicitud, todo en una transacción
    suspend fun aprobar(solicitud: SolicitudRada, radaId: String) {
        val solDoc = ref.document(solicitud.id)
        val radaDoc = radasRef.document(radaId)
        val asigDoc = asignacionesRef.document()

        db.runTransaction { tx ->
            val sol = tx.get(solDoc)
            val rada = tx.get(radaDoc)

            if (sol.getString("estado") != EstadoSolicitud.PENDIENTE)
                throw IllegalStateException("La solicitud ya fue atendida")
            if (rada.getString("estado") != EstadoRada.DISPONIBLE)
                throw IllegalStateException("La rada ya no está disponible")
            if ((rada.getDouble("eslora_max") ?: 0.0) < solicitud.eslora)
                throw IllegalStateException("La nave no cabe en esa rada")

            tx.set(
                asigDoc,
                Asignacion(
                    matricula = solicitud.matricula,
                    dniSocio = solicitud.dniSocio,
                    radaAsignada = radaId,
                    fechaIngreso = solicitud.fechaIngreso,
                    fechaSalida = solicitud.fechaSalida
                )
            )
            tx.update(radaDoc, "estado", EstadoRada.OCUPADA)
            tx.update(
                solDoc,
                mapOf<String, Any>("estado" to EstadoSolicitud.APROBADA, "rada_asignada" to radaId)
            )
        }.await()
    }

    suspend fun rechazar(solicitudId: String, motivo: String) {
        ref.document(solicitudId).update(
            mapOf<String, Any>("estado" to EstadoSolicitud.RECHAZADA, "motivo_rechazo" to motivo)
        ).await()
    }
}