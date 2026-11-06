package com.dsm.clubnauticoposeidon.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "servicio",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoria_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoria_id"), Index("modulo")]
)
data class ServicioEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "categoria_id") val categoriaId: String,
    val nombre: String,
    val descripcion: String = "",
    val precio: Double = 0.0,
    val modulo: String = ""
)
