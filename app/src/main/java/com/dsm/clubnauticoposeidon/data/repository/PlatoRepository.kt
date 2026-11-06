package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.model.Plato
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// Menú de cada módulo (catering, limpieza...) desde Firestore.
// Usa un listener: funciona sin internet con lo que Firestore ya guardó en el celular.
class PlatoRepository {

    private val ref = FirebaseFirestore.getInstance().collection("catalogo_servicios")

    fun observarPlatos(modulo: String): Flow<List<Plato>> = callbackFlow {
        val escucha = ref
            .whereEqualTo("modulo", modulo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val platos = snapshot?.toObjects(Plato::class.java).orEmpty()
                    .filter { it.disponible }
                trySend(platos)
            }
        awaitClose { escucha.remove() }
    }
}