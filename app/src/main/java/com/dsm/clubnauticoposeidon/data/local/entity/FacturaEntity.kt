

package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "factura",
    foreignKeys = [
        ForeignKey(
            entity = SocioEntity::class,
            parentColumns = ["uid"],
            childColumns = ["socio_uid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("socio_uid")]
)
data class FacturaEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "socio_uid") val socioUid: String,
    val periodo: String,                                   // "2026-11"
    val total: Double = 0.0,
    val recargo: Double = 0.0,
    val estado: String = "Pendiente",                      // Pendiente / Pagada / Vencida
    @ColumnInfo(name = "fecha_vencimiento") val fechaVencimiento: Long = 0L
)
