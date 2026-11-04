package com.dsm.clubnauticoposeidon.ui.screens.home



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.data.repository.RadaRepository
import com.dsm.clubnauticoposeidon.model.EstadoRada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class HomeOperacionesViewModel(
    private val repo: RadaRepository = RadaRepository()
) : ViewModel() {

    private val _resumen = MutableStateFlow(ResumenRadas())
    val resumen: StateFlow<ResumenRadas> = _resumen

    init {
        viewModelScope.launch {
            repo.observarRadas()
                .catch { _resumen.update { it.copy(cargando = false) } }
                .collect { radas ->
                    _resumen.value = ResumenRadas(
                        totales = radas.size,
                        ocupadas = radas.count { it.estado == EstadoRada.OCUPADA },
                        disponibles = radas.count { it.estado == EstadoRada.DISPONIBLE },
                        mantenimiento = radas.count { it.estado == EstadoRada.MANTENIMIENTO },
                        cargando = false
                    )
                }
        }
    }
}