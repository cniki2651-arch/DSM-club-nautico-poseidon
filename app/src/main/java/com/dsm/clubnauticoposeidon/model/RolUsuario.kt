package com.dsm.clubnauticoposeidon.model

/**
 * Enum que representa los roles del sistema registrados en Firestore (almacenados en minúsculas).
 */
enum class RolUsuario(val key: String, val displayName: String) {
    JEFE("jefe", "Jefatura"),
    NAVIERO("naviero", "Servicios Navieros"),
    SECRETARIA("secretaria", "Secretaría"),
    FINANZAS("finanzas", "Finanzas"),
    MANTENIMIENTO("mantenimiento", "Mantenimiento"),
    SOCIO("socio", "Socio"),
    POSTULANTE("postulante", "Postulante");

    companion object {
        fun fromKey(key: String?): RolUsuario {
            val keyClean = key?.trim()?.lowercase()
            return entries.find { it.key == keyClean } ?: SOCIO
        }
    }
}
