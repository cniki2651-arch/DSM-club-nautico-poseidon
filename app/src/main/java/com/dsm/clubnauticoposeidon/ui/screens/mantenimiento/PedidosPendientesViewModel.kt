package com.dsm.clubnauticoposeidon.ui.screens.mantenimiento

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ItemPendiente(
    val nombre: String,
    val cantidad: Int
)

data class PedidoPendiente(
    val id: String,
    val modulo: String,
    val total: Double,
    val fecha: Long,
    val ubicacion: String,
    val nota: String,
    val items: List<ItemPendiente>
)

// Escucha en vivo los pedidos pendientes de TODOS los módulos
// y permite al encargado marcarlos como entregados.
class PedidosPendientesViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _pedidos = MutableStateFlow<List<PedidoPendiente>>(emptyList())
    val pedidos: StateFlow<List<PedidoPendiente>> = _pedidos

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var registro: ListenerRegistration? = null

    init {
        registro = firestore.collection("solicitudes_servicios")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "No se pudieron cargar los pedidos"
                    _cargando.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    _pedidos.value = snapshot.documents.map { it.aPendiente() }
                    _cargando.value = false
                }
            }
    }

    // Guarda quién entregó y cuándo (trazabilidad). El pedido desaparece de la bandeja
    // y el historial del socio se actualiza solo.
    fun marcarEntregado(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        firestore.collection("solicitudes_servicios")
            .document(id)
            .update(
                mapOf(
                    "estado" to "entregado",
                    "fecha_entrega" to FieldValue.serverTimestamp(),
                    "entregado_por" to uid
                )
            )
            .addOnFailureListener {
                _error.value = "No se pudo marcar como entregado"
            }
    }

    fun limpiarError() {
        _error.value = null
    }

    override fun onCleared() {
        registro?.remove()
        super.onCleared()
    }
}

private fun DocumentSnapshot.aPendiente(): PedidoPendiente {
    val fecha = when (val f = get("fecha_solicitud", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)) {
        is Timestamp -> f.toDate().time
        is Number -> f.toLong()
        else -> 0L
    }

    val items = (get("items_solicitados") as? List<*>).orEmpty().mapNotNull { elemento ->
        val mapa = elemento as? Map<*, *> ?: return@mapNotNull null
        ItemPendiente(
            nombre = mapa["nombre"]?.toString().orEmpty(),
            cantidad = (mapa["cantidad"] as? Number)?.toInt() ?: 1
        )
    }

    // Limpieza y asistencia pueden traer una descripción en lugar de productos
    val nota = getString("descripcion")
        ?: getString("detalle")
        ?: getString("observaciones")
        ?: getString("nota")
        ?: ""

    return PedidoPendiente(
        id = id,
        modulo = getString("modulo").orEmpty(),
        total = (get("total_pagar") as? Number)?.toDouble() ?: 0.0,
        fecha = fecha,
        ubicacion = getString("ubicacion_rada").orEmpty(),
        nota = nota,
        items = items
    )
}
