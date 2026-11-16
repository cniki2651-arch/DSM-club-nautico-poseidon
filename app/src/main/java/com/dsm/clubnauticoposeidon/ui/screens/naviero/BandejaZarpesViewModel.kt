package com.dsm.clubnauticoposeidon.ui.screens.naviero

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.BandejaZarpesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class BandejaZarpesViewModel(
    private val repository: BandejaZarpesRepository = BandejaZarpesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BandejaZarpesState>(BandejaZarpesState.Cargando)
    val uiState: StateFlow<BandejaZarpesState> = _uiState

    init {
        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            repository.observarZarpesActivos()
                .combine(repository.observarTripulantes()) { zarpes, tripulantes ->
                    BandejaZarpesState.Exito(
                        solicitudesPendientes = zarpes,
                        tripulantesAutorizados = tripulantes
                    )
                }
                .catch { e ->
                    Log.e("BandejaZarpesVM", "Error al observar datos", e)
                    emit(BandejaZarpesState.Exito(emptyList(), emptyList()))
                }
                .collect { estado ->
                    _uiState.value = estado
                }
        }
    }

    fun aprobarZarpe(zarpeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.aprobarZarpe(zarpeId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al aprobar zarpe")
            }
        }
    }

    fun rechazarZarpe(zarpeId: String, motivo: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.rechazarZarpe(zarpeId, motivo)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al rechazar zarpe")
            }
        }
    }

    fun finalizarZarpe(zarpeId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.finalizarZarpe(zarpeId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al registrar arribo a puerto")
            }
        }
    }

    fun registrarTripulante(
        nombre: String,
        dni: String,
        licencia: String,
        cargo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.registrarTripulante(nombre, dni, licencia, cargo)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al registrar tripulante")
            }
        }
    }
}
