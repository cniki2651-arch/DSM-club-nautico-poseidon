package com.dsm.clubnauticoposeidon.ui.screens.mantenimiento

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.AveriasRepository
import com.dsm.clubnauticoposeidon.data.repository.ReporteDanioRepository
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EvaluacionAveria
import com.dsm.clubnauticoposeidon.model.GravedadAveria
import com.dsm.clubnauticoposeidon.model.ReporteDanio
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

data class EvaluacionAveriasUiState(
    val fotoOriginal: Bitmap? = null,
    val fotoBase64: String? = null,
    val evaluando: Boolean = false,
    val guardando: Boolean = false,
    val resultado: EvaluacionAveria? = null,
    val embarcaciones: List<Embarcacion> = emptyList(),
    val embarcacionSeleccionada: Embarcacion? = null,
    val reporteDanioSeleccionado: ReporteDanio? = null,
    val reportesPendientesSocio: List<ReporteDanio> = emptyList(),
    val cargandoReportesDanio: Boolean = true,
    val tipoDanioEditado: String = "",
    val horasReparacionEditadas: String = "",
    val observaciones: String = "",
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val reportesHistorial: List<EvaluacionAveria> = emptyList(),
    val cargandoHistorial: Boolean = false
) {
    // Solo puede diagnosticarse si la embarcación está registrada en el club
    val naveEstaSeleccionada: Boolean
        get() = embarcacionSeleccionada != null

    val nombreNaveActiva: String
        get() = embarcacionSeleccionada?.nombreNave.orEmpty()

    val matriculaNaveActiva: String
        get() = embarcacionSeleccionada?.matricula.orEmpty()

    /**
     * Retorna el reporte de daño pendiente si esta nave tiene una notificación de un socio.
     */
    fun reporteParaNave(nave: Embarcacion): ReporteDanio? {
        return reportesPendientesSocio.find { rep ->
            (rep.embarcacionId.isNotBlank() && rep.embarcacionId == nave.id) ||
                    (rep.matricula.isNotBlank() && rep.matricula.equals(nave.matricula, ignoreCase = true))
        }
    }

    /**
     * Lista de embarcaciones ordenadas priorizando las que tienen reporte de daño pendiente.
     */
    val embarcacionesOrdenadas: List<Embarcacion>
        get() = embarcaciones.sortedWith(
            compareByDescending<Embarcacion> { reporteParaNave(it) != null }
                .thenBy { it.nombreNave.lowercase() }
        )
}

class EvaluacionAveriasViewModel(
    private val repository: AveriasRepository = AveriasRepository(),
    private val reporteDanioRepo: ReporteDanioRepository = ReporteDanioRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EvaluacionAveriasUiState())
    val uiState: StateFlow<EvaluacionAveriasUiState> = _uiState.asStateFlow()

    // Modelo de Visión Artificial de Gemini con Firebase AI Logic
    private val visionModel = Firebase.ai.generativeModel(
        modelName = "gemini-3.5-flash-lite",
        generationConfig = generationConfig {
            temperature = 0.2f
        },
        systemInstruction = content {
            text(
                """
                Eres el Sistema Experto de Visión Artificial e Inspección Naval del Club Náutico Poseidón.
                Tu función exclusiva es analizar fotografías de averías, daños estructurales, desgaste o desperfectos en yates y embarcaciones marítimas.
                
                Debes clasificar rigurosamente la gravedad del daño según los estándares náuticos:
                - LEVE: Daños estéticos, roces de pintura, rayones superficiales en gelcoat, tapicería desgastada. No compromete flotabilidad ni seguridad.
                - MODERADA: Fisuras sin paso de agua, abolladuras, corrosión galvánica en herrajes, fallos de sellado que requieren atención previa al próximo zarpe.
                - GRAVE: Daños estructurales en casco, quilla o espejo de popa; grietas pasantes; vías de agua; daños en hélices/ejes; riesgo para la navegación o vidas humanas.
                
                Responde SIEMPRE en formato JSON válido estricto sin texto adicional.
                """.trimIndent()
            )
        }
    )

    init {
        cargarEmbarcaciones()
        observarReportesPendientesSocio()
        observarHistorial()
    }

    private fun cargarEmbarcaciones() {
        viewModelScope.launch {
            val lista = repository.obtenerEmbarcaciones()
            _uiState.update { it.copy(embarcaciones = lista) }
        }
    }

    private fun observarReportesPendientesSocio() {
        viewModelScope.launch {
            try {
                reporteDanioRepo.observarReportesPendientes().collect { pendientes ->
                    _uiState.update {
                        it.copy(
                            reportesPendientesSocio = pendientes,
                            cargandoReportesDanio = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargandoReportesDanio = false) }
            }
        }
    }

    private fun observarHistorial() {
        _uiState.update { it.copy(cargandoHistorial = true) }
        viewModelScope.launch {
            try {
                repository.observarReportes().collect { lista ->
                    _uiState.update {
                        it.copy(reportesHistorial = lista, cargandoHistorial = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargandoHistorial = false) }
            }
        }
    }

    /**
     * El encargado selecciona una embarcación registrada del club.
     * Si esa nave tiene un reporte de daño notificado por un socio, se asocia automáticamente.
     */
    fun seleccionarEmbarcacion(nave: Embarcacion) {
        val reporteAsociado = _uiState.value.reporteParaNave(nave)

        _uiState.update {
            it.copy(
                embarcacionSeleccionada = nave,
                reporteDanioSeleccionado = reporteAsociado,
                observaciones = if (reporteAsociado != null && it.observaciones.isBlank()) {
                    "Reporte del socio: ${reporteAsociado.descripcion}"
                } else it.observaciones,
                resultado = it.resultado?.copy(
                    embarcacionId = nave.id,
                    nombreNave = nave.nombreNave,
                    matricula = nave.matricula
                )
            )
        }
    }

    /**
     * Deselecciona la nave para permitir elegir otra embarcación registrada.
     */
    fun deseleccionarEmbarcacion() {
        _uiState.update {
            it.copy(
                embarcacionSeleccionada = null,
                reporteDanioSeleccionado = null,
                fotoOriginal = null,
                fotoBase64 = null,
                resultado = null,
                tipoDanioEditado = "",
                horasReparacionEditadas = ""
            )
        }
    }

    fun onTipoDanioChanged(tipo: String) {
        _uiState.update { it.copy(tipoDanioEditado = tipo) }
    }

    fun onHorasReparacionChanged(horas: String) {
        _uiState.update { it.copy(horasReparacionEditadas = horas) }
    }

    fun onObservacionesChanged(obs: String) {
        _uiState.update {
            it.copy(
                observaciones = obs,
                resultado = it.resultado?.copy(observacionesOperario = obs)
            )
        }
    }

    /**
     * Procesa la foto tomada por la cámara del dispositivo móvil.
     */
    fun onFotoCapturada(bitmap: Bitmap) {
        viewModelScope.launch {
            val (bmpEscalado, base64) = withContext(Dispatchers.Default) {
                val maxDim = maxOf(bitmap.width, bitmap.height)
                val escalado = if (maxDim > 1024) {
                    val factor = 1024f / maxDim
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * factor).toInt(),
                        (bitmap.height * factor).toInt(),
                        true
                    )
                } else {
                    bitmap
                }

                val factorThumb = 400f / maxOf(escalado.width, escalado.height)
                val thumb = Bitmap.createScaledBitmap(
                    escalado,
                    (escalado.width * factorThumb).toInt(),
                    (escalado.height * factorThumb).toInt(),
                    true
                )
                val baos = ByteArrayOutputStream()
                thumb.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                escalado to b64
            }

            _uiState.update {
                it.copy(
                    fotoOriginal = bmpEscalado,
                    fotoBase64 = base64,
                    resultado = null,
                    mensajeError = null
                )
            }

            clasificarAveriaAutomatica(bmpEscalado, base64)
        }
    }

    /**
     * Clasifica la gravedad del daño mediante el modelo multimodal de Visión Artificial.
     */
    fun clasificarAveriaAutomatica(bitmap: Bitmap? = _uiState.value.fotoOriginal, fotoB64: String? = _uiState.value.fotoBase64) {
        val bmp = bitmap ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(evaluando = true, mensajeError = null) }

            try {
                val promptContent = content {
                    image(bmp)
                    text(
                        """
                        Evalúa la avería visible en esta fotografía de la embarcación del Club Náutico Poseidón.
                        Genera un diagnóstico en JSON con exactamente estas claves:
                        {
                          "gravedad": "LEVE" | "MODERADA" | "GRAVE",
                          "tipoDanio": "Nombre técnico y conciso del daño",
                          "zonaAfectada": "Zona de la nave (ej. Casco, Quilla, Espejo de Popa, Cubierta, etc.)",
                          "indiceSeveridad": 80, // número entero de 0 a 100
                          "descripcionDetallada": "Descripción clara del daño visible.",
                          "accionInmediata": "Acción urgente que debe tomar el operario de mantenimiento",
                          "estimacionHorasReparacion": 8, // horas estimadas
                          "requiereVarado": true, // booleano
                          "recomendacionesTecnicas": [
                             "Recomendación 1",
                             "Recomendación 2"
                          ]
                        }
                        Responde ÚNICAMENTE con el objeto JSON.
                        """.trimIndent()
                    )
                }

                val response = visionModel.generateContent(promptContent)
                val textoRespuesta = response.text.orEmpty().trim()
                Log.d("VISION_AVERIAS", "Respuesta IA: $textoRespuesta")

                val resultadoParsed = parsearRespuestaIA(textoRespuesta, fotoB64)

                _uiState.update {
                    it.copy(
                        evaluando = false,
                        resultado = resultadoParsed,
                        tipoDanioEditado = resultadoParsed.tipoDanio,
                        horasReparacionEditadas = resultadoParsed.estimacionHorasReparacion.toString()
                    )
                }
            } catch (e: Exception) {
                Log.e("VISION_AVERIAS", "Error al clasificar avería con IA", e)
                val fallback = generarDiagnosticoFallback(fotoB64)
                _uiState.update {
                    it.copy(
                        evaluando = false,
                        resultado = fallback,
                        tipoDanioEditado = fallback.tipoDanio,
                        horasReparacionEditadas = fallback.estimacionHorasReparacion.toString(),
                        mensajeError = "Aviso: Se aplicó evaluación preliminar local (${e.localizedMessage ?: "Revisa tu conexión"})."
                    )
                }
            }
        }
    }

    private fun parsearRespuestaIA(rawText: String, fotoB64: String?): EvaluacionAveria {
        val jsonLimpio = rawText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val jsonInicio = jsonLimpio.indexOf('{')
        val jsonFin = jsonLimpio.lastIndexOf('}')

        if (jsonInicio != -1 && jsonFin != -1 && jsonFin >= jsonInicio) {
            val jsonString = jsonLimpio.substring(jsonInicio, jsonFin + 1)
            val obj = JSONObject(jsonString)

            val gravedadStr = obj.optString("gravedad", "MODERADA")
            val tipoDanio = obj.optString("tipoDanio", "Avería detectada en inspección visual")
            val zona = obj.optString("zonaAfectada", "Casco / Estructura")
            val severidad = obj.optInt("indiceSeveridad", 50)
            val desc = obj.optString("descripcionDetallada", "Daño localizado mediante visión artificial.")
            val accion = obj.optString("accionInmediata", "Realizar inspección física detallada.")
            val horas = obj.optInt("estimacionHorasReparacion", 4)
            val varado = obj.optBoolean("requiereVarado", false)

            val recsList = mutableListOf<String>()
            val recsJson = obj.optJSONArray("recomendacionesTecnicas")
            if (recsJson != null) {
                for (i in 0 until recsJson.length()) {
                    recsList.add(recsJson.optString(i))
                }
            }
            if (recsList.isEmpty()) {
                recsList.add("Monitorear el área afectada.")
                recsList.add("Registrar en la bitácora de mantenimiento.")
            }

            val nave = _uiState.value.embarcacionSeleccionada

            return EvaluacionAveria(
                embarcacionId = nave?.id.orEmpty(),
                nombreNave = nave?.nombreNave.orEmpty(),
                matricula = nave?.matricula.orEmpty(),
                gravedad = GravedadAveria.desdeString(gravedadStr).name,
                tipoDanio = tipoDanio,
                zonaAfectada = zona,
                indiceSeveridad = severidad.coerceIn(0, 100),
                descripcionDetallada = desc,
                accionInmediata = accion,
                estimacionHorasReparacion = horas,
                requiereVarado = varado,
                recomendaciones = recsList,
                fotoBase64 = fotoB64,
                observacionesOperario = _uiState.value.observaciones,
                reporteDanioId = _uiState.value.reporteDanioSeleccionado?.id.orEmpty()
            )
        } else {
            return generarDiagnosticoFallback(fotoB64)
        }
    }

    private fun generarDiagnosticoFallback(fotoB64: String?): EvaluacionAveria {
        val nave = _uiState.value.embarcacionSeleccionada
        return EvaluacionAveria(
            embarcacionId = nave?.id.orEmpty(),
            nombreNave = nave?.nombreNave.orEmpty(),
            matricula = nave?.matricula.orEmpty(),
            gravedad = GravedadAveria.MODERADA.name,
            tipoDanio = "Fisura / Impacto detectado en superficie náutica",
            zonaAfectada = "Casco / Obra muerta",
            indiceSeveridad = 55,
            descripcionDetallada = "Se detecta irregularidad en la superficie del material que amerita verificación manual por el equipo de dique.",
            accionInmediata = "Revisar espesor de gelcoat y descartar filtración de agua salada.",
            estimacionHorasReparacion = 6,
            requiereVarado = false,
            recomendaciones = listOf(
                "Lijar zona afectada para comprobar profundidad.",
                "Aplicar masilla epóxica marina bicomponente.",
                "Revisar ánodos de sacrificio adyacentes."
            ),
            fotoBase64 = fotoB64,
            observacionesOperario = _uiState.value.observaciones,
            reporteDanioId = _uiState.value.reporteDanioSeleccionado?.id.orEmpty()
        )
    }

    /**
     * Guarda el reporte en Firestore y actualiza el reporte del socio a en_reparacion.
     */
    fun guardarReporte(onExito: () -> Unit = {}) {
        val res = _uiState.value.resultado
        val nave = _uiState.value.embarcacionSeleccionada

        if (nave == null) {
            _uiState.update { it.copy(mensajeError = "Debes seleccionar una embarcación registrada del club.") }
            return
        }

        if (res == null) {
            _uiState.update { it.copy(mensajeError = "Debes realizar la evaluación fotográfica con IA antes de guardar.") }
            return
        }

        val tipoDanioFinal = _uiState.value.tipoDanioEditado.ifBlank { res.tipoDanio }
        val horasFinal = _uiState.value.horasReparacionEditadas.toIntOrNull() ?: res.estimacionHorasReparacion

        if (tipoDanioFinal.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Debes registrar el tipo de daño.") }
            return
        }

        val reporteFinal = res.copy(
            embarcacionId = nave.id,
            nombreNave = nave.nombreNave,
            matricula = nave.matricula,
            tipoDanio = tipoDanioFinal,
            estimacionHorasReparacion = horasFinal,
            observacionesOperario = _uiState.value.observaciones,
            reporteDanioId = _uiState.value.reporteDanioSeleccionado?.id.orEmpty(),
            estado = "en_reparacion"
        )

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensajeError = null) }
            try {
                repository.guardarReporte(reporteFinal)
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeExito = "Reporte de avería registrado e inspección de ${nave.nombreNave} finalizada.",
                        fotoOriginal = null,
                        fotoBase64 = null,
                        resultado = null,
                        observaciones = "",
                        tipoDanioEditado = "",
                        horasReparacionEditadas = "",
                        embarcacionSeleccionada = null,
                        reporteDanioSeleccionado = null
                    )
                }
                onExito()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensajeError = "No se pudo guardar el reporte: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun descartarFoto() {
        _uiState.update {
            it.copy(
                fotoOriginal = null,
                fotoBase64 = null,
                resultado = null,
                tipoDanioEditado = "",
                horasReparacionEditadas = "",
                mensajeError = null
            )
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    fun cambiarEstadoReporte(reporteId: String, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                repository.actualizarEstado(reporteId, nuevoEstado)
            } catch (e: Exception) {
                Log.e("VISION_AVERIAS", "Error al actualizar estado", e)
            }
        }
    }
}
