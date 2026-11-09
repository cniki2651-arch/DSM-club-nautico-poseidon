package com.dsm.clubnauticoposeidon.ui.screens.asistente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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

    private val apiKey = BuildConfig.GEMINI_API_KEY.trim()

    private val generativeModel = if (apiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-3.5-flash-lite",
            apiKey = apiKey,
            systemInstruction = content {
                text(
                    """
                    Eres el Asistente Náutico Inteligente del Club Náutico Poseidón.
                    Tu objetivo es orientar a los socios sobre:
                    - Reglas de uso de radas, muelles y embarcaderos del club.
                    - Requisitos de la Declaración de Zarpe exigidos por Capitanía de Puerto (DICAPI).
                    - Maniobras náuticas (fondeo, anclaje en rada, nudos marineros y seguridad a bordo).
                    - Protocolos de emergencia en mar abierto.
                    Responde siempre en español, con un tono educado, conciso y técnico. Si la pregunta no concierne al ámbito náutico o del club, redirige amablemente hacia temas de navegación.
                    """.trimIndent()
                )
            }
        )
    } else {
        null
    }

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
            if (generativeModel == null) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.BOT,
                    text = "Aviso: La clave API de Gemini (GEMINI_API_KEY) no está configurada en local.properties. Por favor configure su clave para interactuar con el Asistente Náutico.",
                    timestamp = timeFormat.format(Date())
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMsg,
                        isLoading = false
                    )
                }
                return@launch
            }

            try {
                val response = generativeModel.generateContent(userQuery.trim())
                val botText = response.text ?: "Lo siento, no pude procesar tu consulta en este momento."
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
                Log.e("GEMINI_DEBUG", "Error al llamar a Gemini API", e)
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
