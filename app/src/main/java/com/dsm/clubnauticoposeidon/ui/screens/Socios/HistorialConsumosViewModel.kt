package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.local.AppDatabase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class ItemPedidoHistorial(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
)

// Pedido
data class PedidoHistorial(
    val id: String,
    val modulo: String,
    val estado: String,
    val total: Double,
    val fecha: Long,
    val ubicacion: String,
    val items: List<ItemPedidoHistorial>,
    val pendienteDeEnvio: Boolean
)



class HistorialConsumosViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).consumoDao()

    private val _nube = MutableStateFlow<List<PedidoHistorial>>(emptyList())
    private val _locales = MutableStateFlow<List<PedidoHistorial>>(emptyList())

    // Lista final: pendientes del celular + enviados, sin repetir, del más reciente al más antiguo
    val pedidos: StateFlow<List<PedidoHistorial>> =
        combine(_nube, _locales) { nube, locales ->
            val idsEnNube = nube.map { it.id }.toSet()
            (locales.filter { it.id !in idsEnNube } + nube)
                .sortedByDescending { it.fecha }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var registro: ListenerRegistration? = null

    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            _cargando.value = false
            _error.value = "Inicia sesión para ver tu historial"
        } else {
            // 1) SQLite: funciona sin internet y se actualiza solo
            viewModelScope.launch {
                dao.pendientesDe(uid).collect { lista ->
                    _locales.value = lista.map { p ->
                        PedidoHistorial(
                            id = p.id,
                            modulo = p.modulo,
                            estado = p.estado,
                            total = p.total,
                            fecha = p.fecha,
                            ubicacion = p.ubicacion.orEmpty(),
                            items = dao.detalleDe(p.id).map {
                                ItemPedidoHistorial(it.nombre, it.cantidad, it.precioUnitario)
                            },
                            pendienteDeEnvio = true
                        )
                    }
                    _cargando.value = false
                }
            }

            // 2) Firestore: pedidos ya enviados.

            registro = FirebaseFirestore.getInstance()
                .collection("solicitudes_servicios")
                .whereEqualTo("uid_socio", uid)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                    if (e != null) {
                        _error.value = "No se pudo cargar el historial"
                        _cargando.value = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _nube.value = snapshot.documents.map { it.aPedido() }
                        _error.value = null
                        _cargando.value = false
                    }
                }
        }
    }

    override fun onCleared() {
        registro?.remove()
        super.onCleared()
    }
}

private fun DocumentSnapshot.aPedido(): PedidoHistorial {
    val fecha = when (val f = get("fecha_solicitud", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)) {
        is Timestamp -> f.toDate().time
        is Number -> f.toLong()
        else -> 0L
    }
    return PedidoHistorial(
        id = id,
        modulo = getString("modulo").orEmpty(),
        estado = getString("estado") ?: "pendiente",
        total = aDouble(get("total_pagar")),
        fecha = fecha,
        ubicacion = getString("ubicacion_rada").orEmpty(),
        items = parsearItems(get("items_solicitados")),
        // true = guardado en el celular y todavía no confirmado por el servidor
        pendienteDeEnvio = metadata.hasPendingWrites()
    )
}

private fun aDouble(valor: Any?): Double = when (valor) {
    is Number -> valor.toDouble()
    is String -> valor.replace("S/", "").trim().toDoubleOrNull() ?: 0.0
    else -> 0.0
}

private fun aInt(valor: Any?): Int = when (valor) {
    is Number -> valor.toInt()
    is String -> valor.trim().toIntOrNull() ?: 1
    else -> 1
}

private fun parsearItems(crudo: Any?): List<ItemPedidoHistorial> {
    val lista = crudo as? List<*> ?: return emptyList()
    val items = mutableListOf<ItemPedidoHistorial>()

    var nombre: String? = null
    var cantidad = 1
    var precio = 0.0

    fun cerrar() {
        nombre?.let { items.add(ItemPedidoHistorial(it, cantidad, precio)) }
        nombre = null
        cantidad = 1
        precio = 0.0
    }

    for (elemento in lista) {
        when (elemento) {
            is Map<*, *> -> {
                cerrar()
                items.add(
                    ItemPedidoHistorial(
                        nombre = elemento["nombre"]?.toString().orEmpty(),
                        cantidad = aInt(elemento["cantidad"]),
                        precioUnitario = aDouble(elemento["precio_unitario"] ?: elemento["precio"])
                    )
                )
            }
            is String -> {
                val i = elemento.indexOf(':')
                if (i > 0) {
                    val clave = elemento.substring(0, i).trim().lowercase()
                    val valor = elemento.substring(i + 1).trim().trim('"').trim()
                    when (clave) {
                        "nombre" -> {
                            cerrar()
                            nombre = valor
                        }
                        "cantidad" -> cantidad = valor.toIntOrNull() ?: 1
                        "precio_unitario", "precio" -> precio = valor.toDoubleOrNull() ?: 0.0
                    }
                }
            }
        }
    }
    cerrar()
    return items
}