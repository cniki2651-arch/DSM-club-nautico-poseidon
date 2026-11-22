package com.dsm.clubnauticoposeidon.ui.screens.jefatura

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ResumenPedidos(
    val total: Int = 0,
    val pendientes: Int = 0,
    val entregados: Int = 0,
    val montoTotal: Double = 0.0
)

// Datos del dashboard de Jefatura. Se mantienen actualizados en vivo:
//  - membresías por estado  (colección usuarios)
//  - consumos por módulo    (colección solicitudes_servicios)
class DashboardJefaturaViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _membresias = MutableStateFlow<Map<String, Int>>(emptyMap())
    val membresias: StateFlow<Map<String, Int>> = _membresias

    private val _consumoPorModulo = MutableStateFlow<Map<String, Double>>(emptyMap())
    val consumoPorModulo: StateFlow<Map<String, Double>> = _consumoPorModulo

    private val _pedidos = MutableStateFlow(ResumenPedidos())
    val pedidos: StateFlow<ResumenPedidos> = _pedidos

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val registros = mutableListOf<ListenerRegistration>()

    init {
        registros += firestore.collection("usuarios")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _error.value = "No se pudieron cargar las membresías"
                    return@addSnapshotListener
                }
                val conteo = mutableMapOf<String, Int>()
                for (d in snapshot.documents) {
                    // El personal (jefe, secretaria...) no tiene estado y no se cuenta.
                    // Un socio sin estado (datos de prueba) se cuenta como aprobado.
                    val estado = d.getString("estado")
                        ?: if (d.getString("rol") == "socio") "aprobado" else null
                    if (estado != null) conteo[estado] = (conteo[estado] ?: 0) + 1
                }
                _membresias.value = conteo
            }

        registros += firestore.collection("solicitudes_servicios")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    _error.value = "No se pudieron cargar los consumos"
                    return@addSnapshotListener
                }
                val porModulo = mutableMapOf<String, Double>()
                var pendientes = 0
                var entregados = 0
                var monto = 0.0
                for (d in snapshot.documents) {
                    val total = (d.get("total_pagar") as? Number)?.toDouble() ?: 0.0
                    val modulo = d.getString("modulo").orEmpty()
                    porModulo[modulo] = (porModulo[modulo] ?: 0.0) + total
                    monto += total
                    when (d.getString("estado")) {
                        "pendiente" -> pendientes++
                        "entregado" -> entregados++
                    }
                }
                _consumoPorModulo.value = porModulo
                _pedidos.value = ResumenPedidos(
                    total = snapshot.size(),
                    pendientes = pendientes,
                    entregados = entregados,
                    montoTotal = monto
                )
            }
    }

    override fun onCleared() {
        registros.forEach { it.remove() }
        super.onCleared()
    }
}