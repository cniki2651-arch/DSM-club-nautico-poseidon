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
        private val rolesAdministrativosSet = setOf(
            "jefe", "jefatura",
            "naviero", "servicios navieros", "admin",
            "secretaria", "secretaría",
            "finanzas",
            "mantenimiento"
        )

        fun fromKey(key: String?): RolUsuario {
            val k = key?.trim()?.lowercase() ?: return SOCIO
            return when {
                k in setOf("jefe", "jefatura") -> JEFE
                k in setOf("naviero", "servicios navieros", "admin") -> NAVIERO
                k in setOf("secretaria", "secretaría") -> SECRETARIA
                k == "finanzas" -> FINANZAS
                k == "mantenimiento" -> MANTENIMIENTO
                k == "postulante" -> POSTULANTE
                k == "socio" -> SOCIO
                else -> SOCIO
            }
        }

        fun esAdministrativo(key: String?): Boolean {
            val k = key?.trim()?.lowercase() ?: return false
            return k !in setOf("socio", "postulante") && k in rolesAdministrativosSet
        }
    }
}
