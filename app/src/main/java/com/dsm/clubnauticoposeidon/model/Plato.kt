package com.dsm.clubnauticoposeidon.model

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore

data class Plato(
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val modulo: String = "",
    val disponible: Boolean = true
)

@Composable
fun CateringScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    // Estados para guardar los platos que vienen de Firebase y la categoría activa
    var listaPlatos by remember { mutableStateOf<List<Plato>>(emptyList()) }
    var categoriaSeleccionada by remember { mutableStateOf("Desayunos") }

    // 2. LA CONEXIÓN A FIREBASE OCURRE AQUÍ (al abrir la pantalla)
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("catalogo_servicios")
            .whereEqualTo("modulo", "catering")
            .get()
            .addOnSuccessListener { result ->
                // Convierte los documentos de Firebase directamente a una lista de objetos Plato
                listaPlatos = result.toObjects(Plato::class.java)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar el menú desde la nube", Toast.LENGTH_SHORT).show()
            }
    }

    // Filtramos los platos según la categoría que el usuario toque en las pestañas
    val platosFiltrados = listaPlatos.filter { it.categoria == categoriaSeleccionada }

    // 3. A partir de aquí mantienes tu diseño visual (Scaffold, ScrollableTabRow, LazyColumn)
}