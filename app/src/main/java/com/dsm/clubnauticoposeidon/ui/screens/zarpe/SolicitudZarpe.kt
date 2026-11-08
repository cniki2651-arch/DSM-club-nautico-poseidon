package com.dsm.clubnauticoposeidon.ui.screens.zarpe

data class SolicitudZarpe(
    val id: String = "",
    val embarcacionId: String = "",
    val embarcacionNombre: String = "",
    val socioId: String = "",
    val dni: String = "",
    val destino: String = "",
    val fechaPartida: String = "",
    val horaPartida: String = "",
    val fechaRetorno: String = "",
    val horaRetorno: String = "",
    val fechaHoraSalida: String = "",
    val fechaHoraRegreso: String = "",
    val cantidadTripulantes: Int = 1,
    val manifiestoPasajeros: String = "",
    val observacionesTripulacion: String = "",
    val tripulacionEspecializada: List<String> = emptyList(),
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val estado: String = "pendiente_aprobacion", // pendiente_aprobacion | aprobado | activo | finalizado | rechazado
    val motivoRechazo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val ultimaActualizacion: Long = System.currentTimeMillis()
)
