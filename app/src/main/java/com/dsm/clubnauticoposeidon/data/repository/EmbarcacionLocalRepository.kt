package com.dsm.clubnauticoposeidon.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.dsm.clubnauticoposeidon.data.local.AppDatabase
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.data.local.entity.SocioEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

// Copia local (SQLite) de las embarcaciones del socio, para el selector de Catering, Limpieza, etc.
class EmbarcacionLocalRepository(context: Context) {

    private val db = AppDatabase.get(context.applicationContext)
    private val firestore = FirebaseFirestore.getInstance()

    // La pantalla lee siempre de SQLite, así también funciona sin internet
    fun observar(uid: String): Flow<List<EmbarcacionEntity>> =
        db.socioDao().observarEmbarcaciones(uid)

    // Descarga las embarcaciones del socio y las guarda en SQLite
    suspend fun refrescar(uid: String) {
        val dni = obtenerDni(uid) ?: return

        val documentos = firestore.collection("embarcaciones")
            .whereEqualTo("dni_socio", dni)
            .get()
            .await()
            .documents

        val lista = documentos.map { d ->
            EmbarcacionEntity(
                id = d.id,
                socioUid = uid,
                nombreNave = d.getString("nombre_nave").orEmpty(),
                matricula = d.getString("matricula").orEmpty(),
                tipo = d.getString("tipo").orEmpty(),
                eslora = d.getDouble("eslora") ?: 0.0
            )
        }

        db.withTransaction {
            // La clave foránea exige que el socio exista en la tabla local
            if (db.socioDao().obtenerSocio(uid) == null) {
                db.socioDao().guardarSocio(SocioEntity(uid = uid, dni = dni))
            }
            db.socioDao().guardarEmbarcaciones(lista)
        }
    }

    // Las embarcaciones se guardan con dni_socio, así que primero se busca el DNI del usuario
    private suspend fun obtenerDni(uid: String): String? {
        val porId = firestore.collection("usuarios").document(uid).get().await()
        porId.get("dni")?.toString()?.let { return it }

        val porCampo = firestore.collection("usuarios")
            .whereEqualTo("uid", uid)
            .limit(1)
            .get()
            .await()
        return porCampo.documents.firstOrNull()?.get("dni")?.toString()
    }
}