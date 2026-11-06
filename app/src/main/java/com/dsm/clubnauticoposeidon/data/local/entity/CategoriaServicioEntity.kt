package com.dsm.clubnauticoposeidon.data.local.entity




import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "categoria_servicio")
data class CategoriaServicioEntity(
    @PrimaryKey val id: String,
    val nombre: String
)
