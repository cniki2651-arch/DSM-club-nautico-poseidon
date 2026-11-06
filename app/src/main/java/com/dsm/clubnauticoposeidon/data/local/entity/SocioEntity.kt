package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "socio")
data class SocioEntity(
    @PrimaryKey val uid: String,
    val dni: String = "",
    val nombres: String = "",
    val correo: String = "",
    @ColumnInfo(name = "estado_socio") val estadoSocio: String = "habil", // habil / moroso
    @ColumnInfo(name = "tipo_socio") val tipoSocio: String = "",
    @ColumnInfo(name = "bloqueado_zarpe") val bloqueadoZarpe: Boolean = false,
    @ColumnInfo(name = "qr_data") val qrData: String = "",
    @ColumnInfo(name = "actualizado_en") val actualizadoEn: Long = System.currentTimeMillis()

)
