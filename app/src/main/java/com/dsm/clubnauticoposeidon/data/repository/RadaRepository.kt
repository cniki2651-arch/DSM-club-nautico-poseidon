package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.Asignacion
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.HistorialItem
import com.dsm.clubnauticoposeidon.model.Rada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RadaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val radasRef = db.collection("radas")
    private val asignacionesRef = db.collection("asignaciones")
    private val embarcacionesRef = db.collection("embarcaciones")

    suspend fun obtenerRadas(): List<Rada> =
        radasRef.get().await().toObjects(Rada::class.java)

    fun observarRadas(): Flow<List<Rada>> = callbackFlow {
        val listener = radasRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObjects(Rada::class.java) ?: emptyList())
        }
        awaitClose { listener.remove() }
    }

    suspend fun obtenerEmbarcacion(matricula: String): Embarcacion? =
        embarcacionesRef
            .whereEqualTo("matricula", matricula.trim().uppercase())
            .limit(1)
            .get()
            .await()
            .toObjects(Embarcacion::class.java)
            .firstOrNull()

    suspend fun guardarAsignacion(asignacion: Asignacion) {
        val radaDoc = radasRef.document(asignacion.radaAsignada)
        val asignacionDoc = asignacionesRef.document()

        db.runTransaction { tx ->
            val estadoActual = tx.get(radaDoc).getString("estado")
            if (estadoActual != EstadoRada.DISPONIBLE) {
                throw IllegalStateException("La rada ya no está disponible")
            }
            tx.set(asignacionDoc, asignacion)
            tx.update(radaDoc, "estado", EstadoRada.OCUPADA)
        }.await()
    }

    suspend fun cambiarEstado(radaId: String, nuevoEstado: String) {
        val radaDoc = radasRef.document(radaId)
        db.runTransaction { tx ->
            val actual = tx.get(radaDoc).getString("estado")
            val permitido =
                (actual == EstadoRada.DISPONIBLE && nuevoEstado == EstadoRada.MANTENIMIENTO) ||
                        (actual == EstadoRada.MANTENIMIENTO && nuevoEstado == EstadoRada.DISPONIBLE)
            if (!permitido) throw IllegalStateException("Ese cambio de estado no está permitido")
            tx.update(radaDoc, "estado", nuevoEstado)
        }.await()
    }

    suspend fun liberarRada(radaId: String) {
        val activas = asignacionesRef
            .whereEqualTo("rada_asignada", radaId)
            .whereEqualTo("estado", "activa")
            .get()
            .await()

        val batch = db.batch()
        activas.documents.forEach { batch.update(it.reference, "estado", "finalizada") }
        batch.update(radasRef.document(radaId), "estado", EstadoRada.DISPONIBLE)
        batch.commit().await()
    }

    suspend fun obtenerHistorial(): List<HistorialItem> {
        val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val hoy = LocalDate.now()

        fun parsear(texto: String): LocalDate? =
            runCatching { LocalDate.parse(texto, formato) }.getOrNull()

        val asignaciones = asignacionesRef.get().await().toObjects(Asignacion::class.java)
        val naves = embarcacionesRef.get().await()
            .toObjects(Embarcacion::class.java)
            .associateBy { it.matricula }
        val socios = db.collection("usuarios").get().await().documents
            .mapNotNull { doc ->
                val dni = doc.getString("dni") ?: return@mapNotNull null
                val nombre = "${doc.getString("nombre").orEmpty()} ${doc.getString("apellidos").orEmpty()}".trim()
                dni to nombre
            }
            .toMap()

        return asignaciones
            .sortedByDescending { parsear(it.fechaIngreso) ?: LocalDate.MIN }
            .map { a ->
                val nave = naves[a.matricula]
                val salida = parsear(a.fechaSalida)
                HistorialItem(
                    id = a.id,
                    socio = socios[a.dniSocio]?.ifBlank { null } ?: "Socio sin datos",
                    dni = a.dniSocio,
                    nave = nave?.nombreNave ?: "Nave no registrada",
                    tipo = nave?.tipo ?: "-",
                    eslora = nave?.eslora,
                    matricula = a.matricula,
                    rada = a.radaAsignada,
                    fechaIngreso = a.fechaIngreso.ifBlank { "-" },
                    fechaSalida = a.fechaSalida.ifBlank { "-" },
                    estado = a.estado,
                    salidaPendiente = a.estado == "activa" && salida != null && !salida.isAfter(hoy)
                )
            }
    }
}