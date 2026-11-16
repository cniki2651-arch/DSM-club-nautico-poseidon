package com.dsm.clubnauticoposeidon.data.repository

import com.dsm.clubnauticoposeidon.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class AsistenteRepositoryImpl : AsistenteRepository {

    private val apiKey = BuildConfig.GEMINI_API_KEY.trim()

    private val generativeModel = if (apiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
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

    private val chat = generativeModel?.startChat()

    override suspend fun enviarMensaje(mensaje: String): String {
        if (generativeModel == null) {
            throw IllegalStateException("La clave API de Gemini (GEMINI_API_KEY) no está configurada en local.properties.")
        }
        val response = if (chat != null) {
            chat.sendMessage(mensaje)
        } else {
            generativeModel.generateContent(mensaje)
        }
        return response.text ?: "Lo siento, no pude procesar tu consulta en este momento."
    }
}
