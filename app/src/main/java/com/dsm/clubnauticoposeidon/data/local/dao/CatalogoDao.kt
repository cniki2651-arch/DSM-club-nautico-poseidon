package com.dsm.clubnauticoposeidon.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dsm.clubnauticoposeidon.data.local.entity.CategoriaServicioEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ServicioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoDao {

    // Guardar SIEMPRE primero las categorías y luego los servicios (clave foránea)
    @Upsert
    suspend fun guardarCategorias(categorias: List<CategoriaServicioEntity>)

    @Upsert
    suspend fun guardarServicios(servicios: List<ServicioEntity>)

    @Query("SELECT * FROM categoria_servicio ORDER BY nombre")
    fun observarCategorias(): Flow<List<CategoriaServicioEntity>>

    @Query("SELECT * FROM servicio ORDER BY nombre")
    fun observarServicios(): Flow<List<ServicioEntity>>

    @Query("SELECT * FROM servicio WHERE modulo = :modulo ORDER BY nombre")
    fun observarPorModulo(modulo: String): Flow<List<ServicioEntity>>
}
