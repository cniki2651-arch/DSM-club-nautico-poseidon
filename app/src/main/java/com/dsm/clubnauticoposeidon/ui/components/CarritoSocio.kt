package com.dsm.clubnauticoposeidon.ui.components

import android.content.Context
import com.dsm.clubnauticoposeidon.model.ItemCarrito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

// Guarda el pedido que el socio va armando.
// No se pierde al ir a la pantalla de inicio, al retroceder ni al cerrar la app.
// Hay un carrito por socio y por módulo (catering, limpieza, suministros...)
object CarritoSocio {

    private const val ARCHIVO = "carrito_socio"
    private val flujos = mutableMapOf<String, MutableStateFlow<List<ItemCarrito>>>()

    fun observar(context: Context, uid: String, modulo: String): StateFlow<List<ItemCarrito>> =
        flujo(context, clave(uid, modulo))

    fun agregar(context: Context, uid: String, modulo: String, nombre: String, precioUnitario: Double) {
        val clave = clave(uid, modulo)
        val actual = flujo(context, clave).value
        val nueva = if (actual.any { it.nombre == nombre }) {
            actual.map { if (it.nombre == nombre) it.copy(cantidad = it.cantidad + 1) else it }
        } else {
            actual + ItemCarrito(nombre = nombre, precioUnitario = precioUnitario, cantidad = 1)
        }
        guardar(context, clave, nueva)
    }

    fun quitar(context: Context, uid: String, modulo: String, nombre: String) {
        val clave = clave(uid, modulo)
        val nueva = flujo(context, clave).value.mapNotNull {
            when {
                it.nombre != nombre -> it
                it.cantidad > 1 -> it.copy(cantidad = it.cantidad - 1)
                else -> null
            }
        }
        guardar(context, clave, nueva)
    }

    // Se llama cuando el pedido ya se confirmó
    fun vaciar(context: Context, uid: String, modulo: String) {
        guardar(context, clave(uid, modulo), emptyList())
    }

    private fun flujo(context: Context, clave: String): MutableStateFlow<List<ItemCarrito>> =
        synchronized(flujos) {
            flujos.getOrPut(clave) { MutableStateFlow(leer(context, clave)) }
        }

    private fun guardar(context: Context, clave: String, lista: List<ItemCarrito>) {
        flujo(context, clave).value = lista

        val arreglo = JSONArray()
        lista.forEach { item ->
            arreglo.put(
                JSONObject()
                    .put("nombre", item.nombre)
                    .put("precio", item.precioUnitario)
                    .put("cantidad", item.cantidad)
            )
        }
        prefs(context).edit().putString(clave, arreglo.toString()).apply()
    }

    private fun leer(context: Context, clave: String): List<ItemCarrito> {
        val texto = prefs(context).getString(clave, null) ?: return emptyList()
        return try {
            val arreglo = JSONArray(texto)
            (0 until arreglo.length()).map { i ->
                val o = arreglo.getJSONObject(i)
                ItemCarrito(
                    nombre = o.getString("nombre"),
                    precioUnitario = o.getDouble("precio"),
                    cantidad = o.getInt("cantidad")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    private fun clave(uid: String, modulo: String) = "carrito_${modulo}_$uid"
}