package cl.mpaz.ticket.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.FotoStorage
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.data.repository.UsuarioRepository
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketDetailViewModel(
    private val repository: TicketRepository,
    usuarios: UsuarioRepository,
    private val sesion: SesionActual,
    private val fotos: FotoStorage,
    private val ticketId: Long
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    val ticket: StateFlow<Ticket?> = repository.observar(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val historial: StateFlow<List<Comentario>> = repository.observarHistorial(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _auxiliares = MutableStateFlow<List<Usuario>>(emptyList())
    val auxiliares: StateFlow<List<Usuario>> = _auxiliares.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch { _auxiliares.value = usuarios.listarAuxiliares() }
    }

    fun cambiarEstado(estado: EstadoTicket) =
        ejecutar { autor -> repository.cambiarEstado(ticketId, estado, autor) }

    fun cambiarPrioridad(prioridad: Prioridad) =
        ejecutar { autor -> repository.cambiarPrioridad(ticketId, prioridad, autor) }

    fun asignar(auxiliar: Usuario) =
        ejecutar { autor -> repository.asignar(ticketId, auxiliar, autor) }

    fun comentar(texto: String) =
        ejecutar { autor -> repository.comentar(ticketId, autor, texto) }

    fun eliminar(alTerminar: () -> Unit) {
        val ruta = ticket.value?.fotoPath
        ejecutar {
            repository.eliminar(ticketId)
            ruta?.let(fotos::eliminar)
            alTerminar()
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    private fun ejecutar(bloque: suspend (Usuario) -> Unit) {
        val autor = sesion.usuario.value ?: return
        viewModelScope.launch {
            try {
                bloque(autor)
            } catch (e: Exception) {
                _error.value = "No se pudo completar la acción: ${e.message ?: "error desconocido"}"
            }
        }
    }
}
