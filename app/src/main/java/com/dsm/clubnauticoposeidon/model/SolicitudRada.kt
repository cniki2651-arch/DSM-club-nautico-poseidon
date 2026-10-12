package com.dsm.clubnauticoposeidon.model


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

object EstadoSolicitud {
    const val PENDIENTE = "pendiente"
    const val APROBADA = "aprobada"
    const val RECHAZADA = "rechazada"
}

data class SolicitudRada(
    @DocumentId val id: String = "",
    val matricula: String = "",
    val tipo: String = "",
    val eslora: Double = 0.0,
    val estado: String = EstadoSolicitud.PENDIENTE,
    @get:PropertyName("nombre_nave") @set:PropertyName("nombre_nave")
    var nombreNave: String = "",
    @get:PropertyName("dni_socio") @set:PropertyName("dni_socio")
    var dniSocio: String = "",
    @get:PropertyName("fecha_ingreso") @set:PropertyName("fecha_ingreso")
    var fechaIngreso: String = "",
    @get:PropertyName("fecha_salida") @set:PropertyName("fecha_salida")
    var fechaSalida: String = "",
    @get:PropertyName("rada_asignada") @set:PropertyName("rada_asignada")
    var radaAsignada: String = "",
    @get:PropertyName("motivo_rechazo") @set:PropertyName("motivo_rechazo")
    var motivoRechazo: String = "",
    @get:PropertyName("fecha_solicitud") @set:PropertyName("fecha_solicitud")
    var fechaSolicitud: Long = 0L
)