package com.dsm.clubnauticoposeidon.ui.screens.initial



import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.clubnauticoposeidon.R
import com.dsm.clubnauticoposeidon.ui.screens.login.signInWithGoogle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado que la pantalla dibuja
data class InitialUiState(
    val cargando: Boolean = false
)

// Destinos a los que puede navegar la pantalla inicial
enum class DestinoInicial {
    SOCIO,
    ADMIN,
    SECRETARIA,
    JEFE,
    SEGUIMIENTO
}

// Eventos de una sola vez (navegar o mostrar un mensaje)
sealed interface InitialEvent {
    data class Navegar(val destino: DestinoInicial) : InitialEvent
    data class MensajeRes(@StringRes val resId: Int) : InitialEvent
    data class Mensaje(val texto: String) : InitialEvent
}

class InitialViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(InitialUiState())
    val state: StateFlow<InitialUiState> = _state.asStateFlow()

    private val _events = Channel<InitialEvent>(Channel.BUFFERED)
    val events: Flow<InitialEvent> = _events.receiveAsFlow()

    fun onGoogleSignIn(context: Context, webClientId: String) {
        if (_state.value.cargando) return

        _state.update { it.copy(cargando = true) }

        viewModelScope.launch {
            try {
                signInWithGoogle(
                    context = context,
                    auth = auth,
                    webClientId = webClientId,

                    onSocio = { terminar(InitialEvent.Navegar(DestinoInicial.SOCIO)) },
                    onAdmin = { terminar(InitialEvent.Navegar(DestinoInicial.ADMIN)) },
                    onSecretaria = { terminar(InitialEvent.Navegar(DestinoInicial.SECRETARIA)) },
                    onJefe = { terminar(InitialEvent.Navegar(DestinoInicial.JEFE)) },
                    onSeguimiento = { terminar(InitialEvent.Navegar(DestinoInicial.SEGUIMIENTO)) },

                    onFinanzas = {
                        auth.signOut()
                        terminar(InitialEvent.MensajeRes(R.string.panel_finanzas))
                    },
                    onMantenimiento = {
                        auth.signOut()
                        terminar(InitialEvent.MensajeRes(R.string.panel_mantenimiento))
                    },

                    onError = { msg -> terminar(InitialEvent.Mensaje(msg)) }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                terminar(
                    e.message?.let { InitialEvent.Mensaje(it) }
                        ?: InitialEvent.MensajeRes(R.string.error_inicio_sesion_google)
                )
            }
        }
    }

    // Quita el estado de carga y envía el evento a la pantalla
    private fun terminar(evento: InitialEvent) {
        _state.update { it.copy(cargando = false) }
        _events.trySend(evento)
    }
}