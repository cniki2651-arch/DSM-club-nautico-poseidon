package com.dsm.clubnauticoposeidon.model



import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Rada(
    @DocumentId val id: String = "",
    val codigo: String = "",
    val estado: String = EstadoRada.DISPONIBLE,
    val muelle: String = "",
    val tipo: String = "",
    @get:PropertyName("eslora_max") @set:PropertyName("eslora_max")
    var esloraMax: Double = 0.0
)