

package com.dsm.clubnauticoposeidon.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.dsm.clubnauticoposeidon.data.local.AppDatabase
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoItemEntity
import com.dsm.clubnauticoposeidon.data.local.entity.SocioEntity
import com.dsm.clubnauticoposeidon.data.worker.SyncScheduler
import com.dsm.clubnauticoposeidon.model.ItemCarrito

class ConsumoRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = AppDatabase.get(appContext)

    // Guarda el pedido SIEMPRE en SQLite (Room) y programa su envío con WorkManager
    suspend fun registrarPedido(
        socioUid: String,
        correo: String,
        modulo: String,
        ubicacionRada: String,
        items: List<ItemCarrito>
    ) {
        val socioDao = db.socioDao()
        val consumoDao = db.consumoDao()

        val consumo = ConsumoEntity(
            socioUid = socioUid,
            modulo = modulo,
            ubicacionRada = ubicacionRada,
            totalPagar = items.sumOf { it.precioUnitario * it.cantidad }
        )
        val detalle = items.map {
            ConsumoItemEntity(
                consumoId = consumo.id,
                servicioId = it.servicioId,
                nombre = it.nombre,
                cantidad = it.cantidad,
                precioUnitario = it.precioUnitario
            )
        }

        // Cabecera + detalle se guardan juntos
        db.withTransaction {
            // La clave foránea exige que el socio exista en la tabla local
            if (socioDao.obtenerSocio(socioUid) == null) {
                socioDao.guardarSocio(SocioEntity(uid = socioUid, correo = correo))
            }
            consumoDao.guardarConsumo(consumo)
            consumoDao.guardarItems(detalle)
        }


        SyncScheduler.sincronizarConsumos(appContext)
    }
}
