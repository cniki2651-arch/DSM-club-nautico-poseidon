package com.dsm.clubnauticoposeidon.ui.screens.asistente

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.AsistenteRepository
import com.dsm.clubnauticoposeidon.data.repository.AsistenteRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private fun obtenerHoraLima(): String {
    val localeEs = Locale.Builder().setLanguage("es").setRegion("PE").build()
    val sdf = SimpleDateFormat("HH:mm", localeEs).apply {
        timeZone = TimeZone.getTimeZone("America/Lima")
    }
    return sdf.format(Date())
}

data class AsistenteUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.BOT,
            text = "¡Hola, Socio! Soy tu Asistente Náutico Poseidón. ⚓ ¿En qué te puedo ayudar hoy?",
            timestamp = obtenerHoraLima()
        )
    ),
    val inputQuery: String = "",
    val isLoading: Boolean = false
)

class AsistenteNauticoViewModel(
    private val asistenteRepository: AsistenteRepository = AsistenteRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsistenteUiState())
    val uiState: StateFlow<AsistenteUiState> = _uiState

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(inputQuery = query) }
    }

    fun sendMessage(userQuery: String) {
        if (userQuery.isBlank()) return
        val horaActual = obtenerHoraLima()
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userQuery.trim(),
            timestamp = horaActual
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
                val botText = asistenteRepository.enviarMensaje(userQuery.trim())
                val botMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.BOT,
                    text = botText,
                    timestamp = obtenerHoraLima()
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + botMsg,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("FIREBASE_AI", "Error en ViewModel al procesar respuesta: ${e.javaClass.simpleName} - ${e.message}", e)
                val errorText = if (e is IllegalStateException) {
                    "Aviso: ${e.localizedMessage ?: "Clave API no configurada."}"
                } else {
                    "⚓ Capitán Poseidón fuera de línea temporalmente. (Verifica tu conexión a internet)."
                }
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.BOT,
                    text = errorText,
                    timestamp = obtenerHoraLima()
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
