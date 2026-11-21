package com.dsm.clubnauticoposeidon.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

class AsistenteRepositoryImpl : AsistenteRepository {

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-3.5-flash-lite",
        generationConfig = generationConfig {
            temperature = 0.7f
        },
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

    private val chat = generativeModel.startChat()

    override suspend fun enviarMensaje(mensaje: String): String {
        return try {
            val response = chat.sendMessage(mensaje)
            response.text ?: "Lo siento, no pude procesar tu consulta en este momento."
        } catch (e: Exception) {
            Log.e("FIREBASE_AI", "Error al enviar mensaje a Firebase AI: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
}
