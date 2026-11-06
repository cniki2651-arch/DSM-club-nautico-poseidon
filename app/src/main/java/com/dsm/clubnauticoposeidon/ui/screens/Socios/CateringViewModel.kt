package com.dsm.clubnauticoposeidon.ui.screens.Socios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.ConsumoRepository
import com.dsm.clubnauticoposeidon.data.repository.PlatoRepository
import com.dsm.clubnauticoposeidon.model.ItemCarrito
import com.dsm.clubnauticoposeidon.model.Plato
import com.dsm.clubnauticoposeidon.ui.components.CarritoSocio
import com.dsm.clubnauticoposeidon.ui.components.ImagenesMenu
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MODULO = "catering"

// Todo lo que la pantalla de Catering necesita dibujar
data class CateringUiState(
    val platos: List<Plato> = emptyList(),
    val categoriaSeleccionada: String = "Desayunos",
    val carrito: List<ItemCarrito> = emptyList(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val pedidoConfirmado: Boolean = false
) {
    val categorias: List<String>
        get() = platos.map { it.categoria }.distinct()

    val platosFiltrados: List<Plato>
        get() = platos.filter { it.categoria == categoriaSeleccionada }

    val total: Double
        get() = carrito.sumOf { it.precioUnitario * it.cantidad }

    val productos: Int
        get() = carrito.sumOf { it.cantidad }

    fun cantidadDe(nombre: String): Int =
        carrito.firstOrNull { it.nombre == nombre }?.cantidad ?: 0
}

class CateringViewModel(private val app: Application) : AndroidViewModel(app) {

    private val platoRepo = PlatoRepository()
    private val consumoRepo = ConsumoRepository(app)
    private val usuario = FirebaseAuth.getInstance().currentUser

    val uid: String = usuario?.uid.orEmpty()

    private val _ui = MutableStateFlow(CateringUiState())
    val ui: StateFlow<CateringUiState> = _ui

    init {
        // Menú desde Firestore (con caché para cuando no hay internet)
        viewModelScope.launch {
            platoRepo.observarPlatos(MODULO)
                .catch {
                    _ui.update { it.copy(cargando = false, mensaje = "Error al cargar el menú de la nube") }
                }
                .collect { platos ->
                    _ui.update { estado ->
                        val categoria =
                            if (platos.any { p -> p.categoria == estado.categoriaSeleccionada }) estado.categoriaSeleccionada
                            else platos.firstOrNull()?.categoria.orEmpty()
                        estado.copy(platos = platos, categoriaSeleccionada = categoria, cargando = false)
                    }
                    // Guarda en el celular las fotos de todas las categorías
                    ImagenesMenu.precargar(app, platos.map { it.imagen })
                }
        }

        // Carrito guardado en el celular: no se pierde al ir al inicio, rotar ni cerrar la app
        viewModelScope.launch {
            CarritoSocio.observar(app, uid, MODULO).collect { carrito ->
                _ui.update { it.copy(carrito = carrito) }
            }
        }
    }

    fun seleccionarCategoria(categoria: String) =
        _ui.update { it.copy(categoriaSeleccionada = categoria) }

    fun agregar(plato: Plato) =
        CarritoSocio.agregar(app, uid, MODULO, plato.nombre, plato.precio)

    fun quitar(plato: Plato) =
        CarritoSocio.quitar(app, uid, MODULO, plato.nombre)

    // ubicacion = texto de la embarcación (y rada) elegida en el selector
    fun confirmar(ubicacion: String?) {
        val estado = _ui.value
        val socio = usuario
        val destino = ubicacion?.takeIf { it.isNotBlank() }

        when {
            estado.guardando -> Unit
            socio == null -> mostrar("Inicia sesión para hacer un pedido")
            estado.carrito.isEmpty() -> mostrar("Agrega al menos un plato")
            destino == null -> mostrar("Elige tu embarcación")
            else -> viewModelScope.launch {
                _ui.update { it.copy(guardando = true) }
                try {
                    // 1) Se guarda en SQLite  2) WorkManager lo envía a Firestore
                    consumoRepo.registrarPedido(
                        socioUid = socio.uid,
                        correo = socio.email.orEmpty(),
                        modulo = MODULO,
                        ubicacionRada = destino,
                        items = estado.carrito
                    )
                    CarritoSocio.vaciar(app, uid, MODULO)
                    _ui.update {
                        it.copy(
                            guardando = false,
                            pedidoConfirmado = true,
                            mensaje = "Pedido registrado. Se enviará a cocina automáticamente"
                        )
                    }
                } catch (e: Exception) {
                    _ui.update { it.copy(guardando = false, mensaje = "No se pudo guardar el pedido") }
                }
            }
        }
    }

    private fun mostrar(texto: String) = _ui.update { it.copy(mensaje = texto) }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }
    fun pedidoProcesado() = _ui.update { it.copy(pedidoConfirmado = false) }
}