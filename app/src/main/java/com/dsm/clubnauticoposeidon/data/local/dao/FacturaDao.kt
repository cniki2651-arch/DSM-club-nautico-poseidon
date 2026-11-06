package com.dsm.clubnauticoposeidon.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dsm.clubnauticoposeidon.data.local.entity.FacturaEntity
import kotlinx.coroutines.flow.Flow

// Resultado del dashboard "Facturación y morosidad"
data class ConteoPorEstado(
    val estado: String,
    val cantidad: Int
)

@Dao
interface FacturaDao {

    @Upsert
    suspend fun guardarFacturas(facturas: List<FacturaEntity>)

    @Query("SELECT * FROM factura WHERE socio_uid = :uid ORDER BY periodo DESC")
    fun observarFacturas(uid: String): Flow<List<FacturaEntity>>

    // HU13: facturas pendientes cuya fecha de vencimiento ya pasó
    @Query(
        "SELECT * FROM factura WHERE socio_uid = :uid AND estado = 'Pendiente' " +
                "AND fecha_vencimiento < :hoy"
    )
    suspend fun vencidasPendientes(uid: String, hoy: Long): List<FacturaEntity>

    @Query("UPDATE factura SET estado = 'Vencida', recargo = :recargo WHERE id = :id")
    suspend fun marcarVencida(id: String, recargo: Double)

    @Query("SELECT estado AS estado, COUNT(*) AS cantidad FROM factura WHERE socio_uid = :uid GROUP BY estado")
    fun conteoPorEstado(uid: String): Flow<List<ConteoPorEstado>>
}
