package com.dsm.clubnauticoposeidon.ui.screens.profile



import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class FotoUiState(
    val foto: Bitmap? = null,
    val guardando: Boolean = false,
    val mensaje: String? = null
)

class FotoPerfilViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _ui = MutableStateFlow(FotoUiState())
    val ui: StateFlow<FotoUiState> = _ui

    init { cargar() }

    // Busca el documento del usuario por correo (igual que ProfileViewModel)
    private suspend fun documentoId(): String? {
        val correo = FirebaseAuth.getInstance().currentUser?.email?.trim()?.lowercase() ?: return null
        return db.collection("usuarios")
            .whereEqualTo("correo", correo)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()?.id
    }

    private fun cargar() {
        viewModelScope.launch {
            try {
                val id = documentoId() ?: return@launch
                val base64 = db.collection("usuarios").document(id).get().await().getString("foto")
                if (!base64.isNullOrBlank()) {
                    val bmp = withContext(Dispatchers.Default) {
                        val bytes = Base64.decode(base64, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    _ui.update { it.copy(foto = bmp) }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(mensaje = "No se pudo cargar la foto") }
            }
        }
    }

    fun guardar(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true) }
            try {
                val id = documentoId() ?: throw IllegalStateException("Sin perfil")
                val (bmp, base64) = withContext(Dispatchers.IO) { prepararFoto(resolver, uri) }
                db.collection("usuarios").document(id)
                    .set(mapOf("foto" to base64), SetOptions.merge())
                    .await()
                _ui.update { it.copy(foto = bmp, guardando = false, mensaje = "Foto actualizada") }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = "No se pudo guardar la foto") }
            }
        }
    }

    // Lee la imagen, la recorta en cuadrado, la reduce a 300x300 y la comprime a JPEG
    private fun prepararFoto(resolver: ContentResolver, uri: Uri): Pair<Bitmap, String> {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

        var muestra = 1
        while (limites.outWidth / muestra > 600 || limites.outHeight / muestra > 600) muestra *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw IllegalStateException("Imagen no válida")

        val lado = minOf(original.width, original.height)
        val cuadrada = Bitmap.createBitmap(
            original,
            (original.width - lado) / 2,
            (original.height - lado) / 2,
            lado,
            lado
        )
        val final = Bitmap.createScaledBitmap(cuadrada, 300, 300, true)

        val salida = ByteArrayOutputStream()
        final.compress(Bitmap.CompressFormat.JPEG, 80, salida)
        return final to Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }
}