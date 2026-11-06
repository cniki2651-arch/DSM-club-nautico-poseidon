package com.dsm.clubnauticoposeidon.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dsm.clubnauticoposeidon.data.local.AppDatabase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

// HU07: sube a Firestore los consumos guardados en Room que aún no se enviaron
class SincronizarConsumosWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.get(applicationContext).consumoDao()
        val firestore = FirebaseFirestore.getInstance()

        return try {
            for (consumo in dao.pendientes()) {
                // items_solicitados como lista de objetos (no de textos sueltos)
                val items = dao.itemsDe(consumo.id).map { item ->
                    mapOf(
                        "nombre" to item.nombre,
                        "cantidad" to item.cantidad,
                        "precio_unitario" to item.precioUnitario,
                        "servicio_id" to item.servicioId
                    )
                }

                val documento = mapOf(
                    "estado" to consumo.estado,
                    "fecha_solicitud" to Timestamp(Date(consumo.fechaSolicitud)),
                    "items_solicitados" to items,
                    "modulo" to consumo.modulo,
                    "total_pagar" to consumo.totalPagar,
                    "ubicacion_rada" to consumo.ubicacionRada,
                    "uid_socio" to consumo.socioUid
                )

                // El ID del documento es el del consumo local: si se reintenta, no se duplica
                firestore.collection("solicitudes_servicios")
                    .document(consumo.id)
                    .set(documento)
                    .await()

                dao.marcarSincronizado(consumo.id)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}
