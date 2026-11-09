package com.dsm.clubnauticoposeidon.ui.screens.naves

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.EmbarcacionRepository
import com.dsm.clubnauticoposeidon.data.repository.SolicitudRadaRepository
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.SolicitudRada
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MisNavesViewModel(
    private val repo: EmbarcacionRepository = EmbarcacionRepository(),
    private val solicitudRepo: SolicitudRadaRepository = SolicitudRadaRepository()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _ui = MutableStateFlow(MisNavesUiState())
    val ui: StateFlow<MisNavesUiState> = _ui

    init {
        viewModelScope.launch {
            val dni = try { repo.obtenerDniActual() } catch (e: Exception) { null }
            if (dni.isNullOrBlank()) {
                _ui.update { it.copy(cargando = false, mensaje = "No se encontró tu DNI en el perfil") }
                return@launch
            }
            _ui.update { it.copy(dni = dni) }

            launch {
                repo.observarNaves(dni)
                    .catch { e -> _ui.update { it.copy(cargando = false, mensaje = "Error: ${e.message}") } }
                    .collect { lista -> _ui.update { it.copy(naves = lista, cargando = false) } }
            }
            launch {
                solicitudRepo.observarPorDni(dni)
                    .catch { e -> _ui.update { it.copy(mensaje = "Error: ${e.message}") } }
                    .collect { lista ->
                        _ui.update { it.copy(solicitudes = lista.sortedByDescending { s -> s.fechaSolicitud }) }
                    }
            }
            launch {
                db.collection("zarpes")
                    .whereIn("estado", listOf("pendiente_aprobacion", "aprobado", "en_mar", "activo"))
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener
                        val map = mutableMapOf<String, Pair<String, String>>()
                        for (doc in snapshot.documents) {
                            val embId = doc.getString("embarcacionId") ?: continue
                            val estado = doc.getString("estado") ?: ""
                            map[embId] = Pair(doc.id, estado)
                        }
                        _ui.update { it.copy(zarpesActivosMap = map) }
                    }
            }
        }
    }

    fun registrar(matricula: String, nombre: String, tipo: String, esloraTexto: String) {
        val dni = _ui.value.dni ?: return
        val eslora = esloraTexto.replace(',', '.').toDoubleOrNull()
        if (eslora == null || eslora <= 0 || eslora > 200) {
            _ui.update { it.copy(mensaje = "Ingresa una eslora válida") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true) }
            try {
                repo.registrarNave(dni, matricula.trim().uppercase(), nombre.trim(), tipo, eslora)
                _ui.update { it.copy(guardando = false, registrada = true, mensaje = "Embarcación registrada") }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = e.message ?: "No se pudo registrar") }
            }
        }
    }

    fun solicitarRada(nave: Embarcacion, fechaIngreso: String, fechaSalida: String) {
        viewModelScope.launch {
            _ui.update { it.copy(guardando = true) }
            try {
                solicitudRepo.crear(nave, fechaIngreso, fechaSalida)
                _ui.update { it.copy(guardando = false, solicitudEnviada = true, mensaje = "Solicitud enviada al naviero") }
            } catch (e: Exception) {
                _ui.update { it.copy(guardando = false, mensaje = e.message ?: "No se pudo enviar") }
            }
        }
    }

    fun mensajeMostrado() = _ui.update { it.copy(mensaje = null) }
    fun registroProcesado() = _ui.update { it.copy(registrada = false) }
    fun solicitudProcesada() = _ui.update { it.copy(solicitudEnviada = false) }
}
