package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "consumo_item",
    foreignKeys = [
        ForeignKey(
            entity = ConsumoEntity::class,
            parentColumns = ["id"],
            childColumns = ["consumo_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["servicio_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("consumo_id"), Index("servicio_id")]
)
data class ConsumoItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "consumo_id") val consumoId: String,
    @ColumnInfo(name = "servicio_id") val servicioId: String? = null,
    val nombre: String,
    val cantidad: Int = 1,
    @ColumnInfo(name = "precio_unitario") val precioUnitario: Double
)
