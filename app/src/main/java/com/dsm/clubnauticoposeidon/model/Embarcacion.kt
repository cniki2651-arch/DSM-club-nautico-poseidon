package com.dsm.clubnauticoposeidon.model



import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Embarcacion(
    @DocumentId val id: String = "",
    val matricula: String = "",
    val tipo: String = "",
    val eslora: Double = 0.0,
    @get:PropertyName("dni_socio") @set:PropertyName("dni_socio")
    var dniSocio: String = "",
    @get:PropertyName("nombre_nave") @set:PropertyName("nombre_nave")
    var nombreNave: String = ""
)