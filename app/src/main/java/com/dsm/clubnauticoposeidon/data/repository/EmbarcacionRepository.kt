package com.dsm.clubnauticoposeidon.data.repository



import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmbarcacionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val navesRef = db.collection("embarcaciones")

    // El DNI se toma del perfil del socio que inició sesión
    suspend fun obtenerDniActual(): String? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return db.collection("usuarios").document(uid).get().await().getString("dni")
    }

    fun observarNaves(dni: String): Flow<List<Embarcacion>> = callbackFlow {
        val listener = navesRef.whereEqualTo("dni_socio", dni)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Embarcacion::class.java) ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun registrarNave(
        dni: String,
        matricula: String,
        nombre: String,
        tipo: String,
        eslora: Double
    ) {
        val existe = navesRef.whereEqualTo("matricula", matricula).limit(1).get().await()
        if (!existe.isEmpty) throw IllegalStateException("Esa matrícula ya está registrada")

        navesRef.add(
            hashMapOf(
                "matricula" to matricula,
                "nombre_nave" to nombre,
                "tipo" to tipo,
                "eslora" to eslora,
                "dni_socio" to dni
            )
        ).await()
    }
}