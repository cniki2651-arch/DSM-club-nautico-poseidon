package com.dsm.clubnauticoposeidon.ui.screens.asistente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AsistenteUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.BOT,
            text = "¡Hola, Socio! Soy tu Asistente Náutico Poseidón. ⚓ ¿En qué te puedo ayudar hoy?",
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
    ),
    val inputQuery: String = "",
    val isLoading: Boolean = false
)

class AsistenteNauticoViewModel : ViewModel() {

    // Firebase AI Logic — autenticado vía google-services.json + App Check.
    // No se necesita API Key en código fuente.
    private val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-3.5-flash-lite",
        generationConfig = generationConfig {
            temperature = 0.4f
            maxOutputTokens = 500
        },
        systemInstruction = content {
            text(
                """
                Eres el Asistente Náutico Inteligente del Club Náutico Poseidón, 
                una herramienta de IA integrada en la aplicación móvil oficial del club.
                
                Tu objetivo es orientar a los socios sobre TODOS los servicios de la app y del club:

                SERVICIOS DEL CLUB NÁUTICO:
                - Gestión de membresías y beneficios de socios.
                - Reglas de uso de radas, muelles y embarcaderos del club.
                - Servicio de catering a bordo y en las instalaciones del club.
                - Servicios de limpieza y mantenimiento de embarcaciones.
                - Suministros náuticos (combustible, agua, electricidad).
                - Asistencia técnica y remolque de emergencia.

                NAVEGACIÓN Y REGULACIONES:
                - Requisitos de la Declaración de Zarpe exigidos por Capitanía de Puerto (DICAPI).
                - Procedimientos de solicitud de zarpe dentro de la aplicación.
                - Maniobras náuticas: fondeo, anclaje en rada, nudos marineros y seguridad a bordo.
                - Protocolos de emergencia en mar abierto.
                - Normativa de navegación costera y de alta mar vigente en Perú.

                FUNCIONALIDADES DE LA APP:
                - Registro y gestión de embarcaciones ("Mis Naves").
                - Solicitud y seguimiento de asignación de radas.
                - Consulta de clima y condiciones marítimas en tiempo real.
                - Historial de consumos y facturación electrónica.
                - Perfil de socio y configuración de cuenta.

                REGLAS DE COMPORTAMIENTO:
                - Responde siempre en español, con un tono educado, conciso y técnico.
                - Usa terminología náutica apropiada.
                - Si la pregunta no concierne al ámbito náutico o del club, redirige amablemente 
                  hacia temas de navegación o servicios del club.
                - Cuando no tengas certeza sobre un dato específico del club, indica al socio 
                  que consulte con la administración del club para información actualizada.
                - No inventes tarifas, horarios ni datos específicos que no conozcas.
                """.trimIndent()
            )
        }
    )

    // Chat con historial persistente durante la sesión
    private val chat = generativeModel.startChat()

    private val _uiState = MutableStateFlow(AsistenteUiState())
    val uiState: StateFlow<AsistenteUiState> = _uiState

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(inputQuery = query) }
    }

    fun sendMessage(userQuery: String) {
        if (userQuery.isBlank()) return
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userQuery.trim(),
            timestamp = timeFormat.format(Date())
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                inputQuery = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                // Enviar mensaje al chat (mantiene historial automáticamente)
                val response = chat.sendMessage(userQuery.trim())
                val botText = response.text
                    ?: "Lo siento, no pude procesar tu consulta en este momento."
                val botMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.BOT,
                    text = botText,
                    timestamp = timeFormat.format(Date())
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + botMsg,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("FIREBASE_AI", "Error al llamar a Firebase AI Logic", e)
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.BOT,
                    text = "⚓ Capitán Poseidón fuera de línea temporalmente. (Verifica tu conexión a internet).",
                    timestamp = timeFormat.format(Date())
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMsg,
                        isLoading = false
                    )
                }
            }
        }
    }
}
