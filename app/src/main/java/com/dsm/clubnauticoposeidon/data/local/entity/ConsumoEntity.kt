package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID


@Entity(
    tableName = "consumo",
    foreignKeys = [
        ForeignKey(
            entity = SocioEntity::class,
            parentColumns = ["uid"],
            childColumns = ["socio_uid"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FacturaEntity::class,
            parentColumns = ["id"],
            childColumns = ["factura_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("socio_uid"), Index("factura_id")]
)
data class ConsumoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),  // será el ID del documento en Firestore
    @ColumnInfo(name = "socio_uid") val socioUid: String,       // = uid_socio
    @ColumnInfo(name = "factura_id") val facturaId: String? = null, // nulo hasta que se factura
    val modulo: String,                                         // catering / limpieza / suministros / asistencia
    @ColumnInfo(name = "ubicacion_rada") val ubicacionRada: String = "",
    @ColumnInfo(name = "total_pagar") val totalPagar: Double,
    val estado: String = "pendiente",                           // pendiente / entregado / facturado
    @ColumnInfo(name = "fecha_solicitud") val fechaSolicitud: Long = System.currentTimeMillis(),
    val sincronizado: Boolean = false
)
