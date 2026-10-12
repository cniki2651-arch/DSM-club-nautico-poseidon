package com.dsm.clubnauticoposeidon.data.repository



import com.dsm.clubnauticoposeidon.model.Asignacion
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EstadoRada
import com.dsm.clubnauticoposeidon.model.Rada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RadaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val radasRef = db.collection("radas")
    private val asignacionesRef = db.collection("asignaciones")
    private val embarcacionesRef = db.collection("embarcaciones")

    // Lectura única de todas las radas
    suspend fun obtenerRadas(): List<Rada> =
        radasRef.get().await().toObjects(Rada::class.java)

    // Tiempo real: el panel de disponibles/ocupadas se actualiza solo
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

    // Las embarcaciones tienen ID automático, por eso se busca por el campo "matricula"
    suspend fun obtenerEmbarcacion(matricula: String): Embarcacion? =
        embarcacionesRef
            .whereEqualTo("matricula", matricula.trim().uppercase())
            .limit(1)
            .get()
            .await()
            .toObjects(Embarcacion::class.java)
            .firstOrNull()

    // Guarda la asignación y marca la rada como ocupada en una sola transacción.
    // Si otra persona ocupó la rada mientras tanto, lanza error y no guarda nada.
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

            // Verde <-> Amarillo. Una rada ocupada no puede cambiarse por aquí.
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

            // Rojo: cierra la asignación activa y deja la rada disponible
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
        }.await()
    }
}