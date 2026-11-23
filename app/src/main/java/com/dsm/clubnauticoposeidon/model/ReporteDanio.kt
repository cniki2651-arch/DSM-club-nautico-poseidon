package com.dsm.clubnauticoposeidon.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Reporte de daño enviado por un Socio para que Mantenimiento lo inspeccione.
 * Colección Firestore: "reportes_danio"
 */
data class ReporteDanio(
    @DocumentId
    var id: String = "",

    @get:PropertyName("embarcacion_id") @set:PropertyName("embarcacion_id")
    var embarcacionId: String = "",

    @get:PropertyName("nombre_nave") @set:PropertyName("nombre_nave")
    var nombreNave: String = "",

    var matricula: String = "",

    var descripcion: String = "",

    @get:PropertyName("socio_uid") @set:PropertyName("socio_uid")
    var socioUid: String = "",

    @get:PropertyName("socio_email") @set:PropertyName("socio_email")
    var socioEmail: String = "",

    @get:PropertyName("socio_dni") @set:PropertyName("socio_dni")
    var socioDni: String = "",

    @get:PropertyName("fecha_reporte") @set:PropertyName("fecha_reporte")
    var fechaReporte: Long = System.currentTimeMillis(),

    // "pendiente", "en_inspeccion", "en_reparacion", "resuelto"
    var estado: String = "pendiente",

    // ID del reporte de avería vinculado (se llena al inspeccionar con IA)
    @get:PropertyName("evaluacion_averia_id") @set:PropertyName("evaluacion_averia_id")
    var evaluacionAveriaId: String = "",

    @get:PropertyName("solicitud_servicio_id") @set:PropertyName("solicitud_servicio_id")
    var solicitudServicioId: String = ""
)
