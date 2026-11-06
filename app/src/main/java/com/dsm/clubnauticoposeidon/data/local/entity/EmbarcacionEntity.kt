package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "embarcacion",
    foreignKeys = [
        ForeignKey(
            entity = SocioEntity::class,
            parentColumns = ["uid"],
            childColumns = ["socio_uid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("socio_uid"),
        Index(value = ["matricula"], unique = true)
    ]
)
data class EmbarcacionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "socio_uid") val socioUid: String,
    @ColumnInfo(name = "nombre_nave") val nombreNave: String,
    val matricula: String,
    val tipo: String = "",
    val eslora: Double = 0.0
)
