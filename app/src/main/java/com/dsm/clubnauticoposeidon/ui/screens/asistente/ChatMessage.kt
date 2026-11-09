package com.dsm.clubnauticoposeidon.ui.screens.asistente

enum class MessageSender { USER, BOT }

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String
)
