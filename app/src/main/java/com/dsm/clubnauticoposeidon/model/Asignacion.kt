package com.dsm.clubnauticoposeidon.model


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class Asignacion(
    @DocumentId val id: String = "",
    val matricula: String = "",
    val estado: String = "activa",
    @get:PropertyName("dni_socio") @set:PropertyName("dni_socio")
    var dniSocio: String = "",
    @get:PropertyName("rada_asignada") @set:PropertyName("rada_asignada")
    var radaAsignada: String = "",
    @get:PropertyName("fecha_ingreso") @set:PropertyName("fecha_ingreso")
    var fechaIngreso: String = "",
    @get:PropertyName("fecha_salida") @set:PropertyName("fecha_salida")
    var fechaSalida: String = ""
)