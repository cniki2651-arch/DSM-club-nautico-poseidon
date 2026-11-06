package com.dsm.clubnauticoposeidon.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.data.local.entity.SocioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocioDao {

    @Upsert
    suspend fun guardarSocio(socio: SocioEntity)

    @Query("SELECT * FROM socio WHERE uid = :uid")
    fun observarSocio(uid: String): Flow<SocioEntity?>

    @Query("SELECT * FROM socio WHERE uid = :uid")
    suspend fun obtenerSocio(uid: String): SocioEntity?

    @Query(
        "UPDATE socio SET estado_socio = :estado, bloqueado_zarpe = :bloqueado, " +
                "actualizado_en = :ahora WHERE uid = :uid"
    )
    suspend fun actualizarEstado(uid: String, estado: String, bloqueado: Boolean, ahora: Long)

    @Upsert
    suspend fun guardarEmbarcaciones(embarcaciones: List<EmbarcacionEntity>)

    @Query("SELECT * FROM embarcacion WHERE socio_uid = :uid ORDER BY nombre_nave")
    fun observarEmbarcaciones(uid: String): Flow<List<EmbarcacionEntity>>
}
