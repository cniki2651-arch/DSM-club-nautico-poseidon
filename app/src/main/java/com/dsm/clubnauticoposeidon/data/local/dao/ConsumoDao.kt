package com.dsm.clubnauticoposeidon.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoItemEntity
import kotlinx.coroutines.flow.Flow

data class ConsumoConItems(
    @Embedded val consumo: ConsumoEntity,
    @Relation(parentColumn = "id", entityColumn = "consumo_id")
    val items: List<ConsumoItemEntity>
)

// Dashboard "Mis consumos"
data class TotalPorModulo(
    val modulo: String,
    val total: Double
)

// Gasto por categoría del catálogo
data class TotalPorCategoria(
    val categoria: String,
    val total: Double
)

@Dao
interface ConsumoDao {

    @Upsert
    suspend fun guardarConsumo(consumo: ConsumoEntity)

    @Upsert
    suspend fun guardarItems(items: List<ConsumoItemEntity>)

    // HU07: consumos registrados sin internet que aún no se subieron
    @Query("SELECT * FROM consumo WHERE sincronizado = 0")
    suspend fun pendientes(): List<ConsumoEntity>

    @Query("SELECT * FROM consumo_item WHERE consumo_id = :consumoId")
    suspend fun itemsDe(consumoId: String): List<ConsumoItemEntity>

    @Query("UPDATE consumo SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: String)

    // HU06: historial ordenado por fecha, con monto, estado y detalle
    @Transaction
    @Query("SELECT * FROM consumo WHERE socio_uid = :uid ORDER BY fecha_solicitud DESC")
    fun historial(uid: String): Flow<List<ConsumoConItems>>

    @Query(
        "SELECT modulo AS modulo, SUM(total_pagar) AS total FROM consumo " +
                "WHERE socio_uid = :uid GROUP BY modulo"
    )
    fun totalesPorModulo(uid: String): Flow<List<TotalPorModulo>>

    @Query(
        """
        SELECT cs.nombre AS categoria, SUM(i.cantidad * i.precio_unitario) AS total
        FROM consumo_item i
        JOIN consumo c ON i.consumo_id = c.id
        JOIN servicio s ON i.servicio_id = s.id
        JOIN categoria_servicio cs ON s.categoria_id = cs.id
        WHERE c.socio_uid = :uid
        GROUP BY cs.nombre
        """
    )
    fun totalesPorCategoria(uid: String): Flow<List<TotalPorCategoria>>
}
