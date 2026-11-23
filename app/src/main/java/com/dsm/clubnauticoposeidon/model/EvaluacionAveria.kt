package com.dsm.clubnauticoposeidon.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

enum class GravedadAveria(
    val titulo: String,
    val descripcionCorta: String,
    val colorHex: Long
) {
    LEVE(
        titulo = "Leve",
        descripcionCorta = "Daño cosmético o superficial. No compromete flotabilidad ni seguridad.",
        colorHex = 0xFF4CAF50 // Verde náutico
    ),
    MODERADA(
        titulo = "Moderada",
        descripcionCorta = "Fisuras, corrosión o fallas menores. Requiere atención antes de próxima navegación.",
        colorHex = 0xFFFFA000 // Ámbar / Naranja preventivo
    ),
    GRAVE(
        titulo = "Grave / Crítica",
        descripcionCorta = "Daño estructural, vía de agua o falla motriz. ¡Zarpe prohibido, varado urgente!",
        colorHex = 0xFFE53935 // Rojo de alerta
    );

    val color: Color get() = Color(colorHex)

    companion object {
        fun desdeString(valor: String?): GravedadAveria {
            return when (valor?.trim()?.uppercase()) {
                "GRAVE", "CRÍTICA", "CRITICA", "ALTA", "CRITICAL", "HIGH" -> GRAVE
                "MODERADA", "MEDIA", "MODERATE", "MEDIUM" -> MODERADA
                else -> LEVE
            }
        }
    }
}

/**
 * Reporte de Evaluación de Avería generado mediante Visión Artificial (Gemini Vision).
 */
data class EvaluacionAveria(
    @DocumentId
    val id: String = "",

    @get:PropertyName("embarcacion_id") @set:PropertyName("embarcacion_id")
    var embarcacionId: String = "",

    @get:PropertyName("nombre_nave") @set:PropertyName("nombre_nave")
    var nombreNave: String = "",

    var matricula: String = "",

    var gravedad: String = GravedadAveria.LEVE.name,

    @get:PropertyName("tipo_danio") @set:PropertyName("tipo_danio")
    var tipoDanio: String = "",

    @get:PropertyName("zona_afectada") @set:PropertyName("zona_afectada")
    var zonaAfectada: String = "",

    @get:PropertyName("indice_severidad") @set:PropertyName("indice_severidad")
    var indiceSeveridad: Int = 0,

    @get:PropertyName("descripcion_detallada") @set:PropertyName("descripcion_detallada")
    var descripcionDetallada: String = "",

    @get:PropertyName("accion_inmediata") @set:PropertyName("accion_inmediata")
    var accionInmediata: String = "",

    @get:PropertyName("estimacion_horas_reparacion") @set:PropertyName("estimacion_horas_reparacion")
    var estimacionHorasReparacion: Int = 0,

    @get:PropertyName("requiere_varado") @set:PropertyName("requiere_varado")
    var requiereVarado: Boolean = false,

    var recomendaciones: List<String> = emptyList(),

    @get:PropertyName("foto_base64") @set:PropertyName("foto_base64")
    var fotoBase64: String? = null,

    @get:PropertyName("fecha_registro") @set:PropertyName("fecha_registro")
    var fechaRegistro: Long = System.currentTimeMillis(),

    @get:PropertyName("operario_email") @set:PropertyName("operario_email")
    var operarioEmail: String = "",

    @get:PropertyName("operario_uid") @set:PropertyName("operario_uid")
    var operarioUid: String = "",

    @get:PropertyName("observaciones_operario") @set:PropertyName("observaciones_operario")
    var observacionesOperario: String = "",

    @get:PropertyName("reporte_danio_id") @set:PropertyName("reporte_danio_id")
    var reporteDanioId: String = "",

    var estado: String = "pendiente_inspeccion" // "pendiente_inspeccion", "en_reparacion", "resuelto"
) {
    val gravedadEnum: GravedadAveria
        get() = GravedadAveria.desdeString(gravedad)
}
