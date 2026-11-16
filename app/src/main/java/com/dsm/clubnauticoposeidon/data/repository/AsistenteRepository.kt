package com.dsm.clubnauticoposeidon.data.repository

interface AsistenteRepository {
    suspend fun enviarMensaje(mensaje: String): String
}
