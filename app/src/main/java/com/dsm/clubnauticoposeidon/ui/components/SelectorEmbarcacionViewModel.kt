package com.dsm.clubnauticoposeidon.ui.components

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.local.entity.EmbarcacionEntity
import com.dsm.clubnauticoposeidon.data.repository.EmbarcacionLocalRepository
import com.dsm.clubnauticoposeidon.model.Asignacion
import com.dsm.clubnauticoposeidon.model.Rada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Lo que el selector necesita dibujar
data class EstadoEmbarcacionUi(
    val embarcaciones: List<EmbarcacionEntity> = emptyList(),
    val seleccionada: EmbarcacionEntity? = null,
    val cargando: Boolean = true,
    val rada: Rada? = null,
    val radaCargando: Boolean = false
)

private data class InfoRada(val rada: Rada?, val cargando: Boolean)

// Lógica del selector de embarcación (se reutiliza en Catering, Limpieza, Suministros...)
@OptIn(ExperimentalCoroutinesApi::class)
class SelectorEmbarcacionViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repo = EmbarcacionLocalRepository(app)
    private val db = FirebaseFirestore.getInstance()

    private val uid = MutableStateFlow<String?>(null)
    private val idElegido = MutableStateFlow<String?>(null)
    private val sincronizando = MutableStateFlow(true)

    // null = SQLite todavía no respondió
    private val lista: StateFlow<List<EmbarcacionEntity>?> = uid
        .filterNotNull()
        .flatMapLatest<String, List<EmbarcacionEntity>?> { u -> repo.observar(u) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Si el socio tiene una sola embarcación, queda elegida automáticamente
    private val seleccionada: Flow<EmbarcacionEntity?> =
        combine(lista, idElegido) { l, id ->
            l?.firstOrNull { it.id == id } ?: l?.singleOrNull()
        }.distinctUntilChanged()

    // Rada de la nave elegida (solo si el naviero aprobó la solicitud)
    private val infoRada: Flow<InfoRada> = seleccionada
        .map { it?.matricula }
        .distinctUntilChanged()
        .flatMapLatest { matricula ->
            if (matricula.isNullOrBlank()) flowOf(InfoRada(null, false))
            else radaActiva(matricula).onStart { emit(InfoRada(null, true)) }
        }

    val estado: StateFlow<EstadoEmbarcacionUi> =
        combine(lista, seleccionada, sincronizando, infoRada) { l, sel, sinc, rada ->
            val listaSegura = l.orEmpty()
            EstadoEmbarcacionUi(
                embarcaciones = listaSegura,
                seleccionada = sel,
                cargando = l == null || (listaSegura.isEmpty() && sinc),
                rada = rada.rada,
                radaCargando = rada.cargando
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoEmbarcacionUi())

    init {
        // La elegida automáticamente también se guarda, para que siga igual si luego registra otra nave
        viewModelScope.launch {
            seleccionada.collect { sel ->
                val u = uid.value ?: return@collect
                if (sel != null && sel.id != idElegido.value) {
                    idElegido.value = sel.id
                    PreferenciasSocio.guardarEmbarcacion(app, u, sel.id)
                }
            }
        }
    }

    fun iniciar(uidSocio: String) {
        if (uid.value == uidSocio) return
        // La elección guardada en el celular: no se pierde al retroceder ni al cerrar la app
        idElegido.value = PreferenciasSocio.embarcacionSeleccionada(app, uidSocio)
        uid.value = uidSocio

        if (uidSocio.isBlank()) {
            sincronizando.value = false
            return
        }
        viewModelScope.launch {
            sincronizando.value = true
            try {
                repo.refrescar(uidSocio)
            } catch (e: Exception) {
                // Sin internet: se usan las embarcaciones ya guardadas en SQLite
            }
            sincronizando.value = false
        }
    }

    fun seleccionar(embarcacion: EmbarcacionEntity) {
        val u = uid.value ?: return
        idElegido.value = embarcacion.id
        PreferenciasSocio.guardarEmbarcacion(app, u, embarcacion.id)
    }

    private fun radaActiva(matricula: String): Flow<InfoRada> =
        asignacionActiva(matricula)
            .distinctUntilChanged()
            .flatMapLatest { radaId ->
                if (radaId.isBlank()) flowOf(InfoRada(null, false))
                else datosRada(radaId).map { InfoRada(it, false) }
            }

    // Asignación activa de la nave (se crea cuando el naviero aprueba la solicitud)
    private fun asignacionActiva(matricula: String): Flow<String> = callbackFlow {
        val escucha = db.collection("asignaciones")
            .whereEqualTo("matricula", matricula)
            .whereEqualTo("estado", "activa")
            .limit(1)
            .addSnapshotListener { snapshot, _ ->
                val radaId = snapshot?.documents?.firstOrNull()
                    ?.toObject(Asignacion::class.java)
                    ?.radaAsignada
                trySend(radaId.orEmpty())
            }
        awaitClose { escucha.remove() }
    }

    private fun datosRada(radaId: String): Flow<Rada> = callbackFlow {
        val escucha = db.collection("radas").document(radaId)
            .addSnapshotListener { doc, _ ->
                trySend(doc?.toObject(Rada::class.java) ?: Rada(id = radaId, codigo = radaId))
            }
        awaitClose { escucha.remove() }
    }
}