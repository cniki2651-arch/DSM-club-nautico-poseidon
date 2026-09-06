package com.dsm.clubnauticoposeidon.ui.screens.home

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Embarcacion(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val imagenUrl: String = "",
    val disponible: Boolean = true
)

data class Actividad(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val imagenUrl: String = ""
)

class HomeViewModel : ViewModel() {
    private var db: FirebaseFirestore = Firebase.firestore

    private val _embarcaciones = MutableStateFlow<List<Embarcacion>>(value = emptyList())
    val embarcaciones: StateFlow<List<Embarcacion>> = _embarcaciones

    private val _actividades = MutableStateFlow<List<Actividad>>(value = emptyList())
    val actividades: StateFlow<List<Actividad>> = _actividades

    init {
        getEmbarcaciones()
        getActividades()
    }

    private fun getEmbarcaciones() {
        db.collection("embarcaciones")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    doc.toObject(Embarcacion::class.java)?.copy(id = doc.id)
                }
                _embarcaciones.value = lista
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HOME", "Error al traer embarcaciones: ${e.message}")
            }
    }

    private fun getActividades() {
        db.collection("actividades")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.documents.mapNotNull { doc ->
                    doc.toObject(Actividad::class.java)?.copy(id = doc.id)
                }
                _actividades.value = lista
            }
            .addOnFailureListener { e ->
                android.util.Log.e("HOME", "Error al traer actividades: ${e.message}")
            }
    }
}