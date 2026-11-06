package com.dsm.clubnauticoposeidon.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dsm.clubnauticoposeidon.data.local.dao.CatalogoDao
import com.dsm.clubnauticoposeidon.data.local.dao.ConsumoDao
import com.dsm.clubnauticoposeidon.data.local.dao.FacturaDao
import com.dsm.clubnauticoposeidon.data.local.dao.SocioDao
import com.dsm.clubnauticoposeidon.data.local.entity.CategoriaServicioEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ConsumoItemEntity
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.data.local.entity.FacturaEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ServicioEntity
import com.dsm.clubnauticoposeidon.data.local.entity.SocioEntity

@Database(
    entities = [
        SocioEntity::class,
        EmbarcacionEntity::class,
        CategoriaServicioEntity::class,
        ServicioEntity::class,
        FacturaEntity::class,
        ConsumoEntity::class,
        ConsumoItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun socioDao(): SocioDao
    abstract fun catalogoDao(): CatalogoDao
    abstract fun consumoDao(): ConsumoDao
    abstract fun facturaDao(): FacturaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase = instancia ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "poseidon.db"
            )
                .fallbackToDestructiveMigration(true) // solo mientras desarrollan
                .build()
                .also { instancia = it }
        }
    }
}
