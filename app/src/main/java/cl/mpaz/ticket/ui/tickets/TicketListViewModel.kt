package cl.mpaz.ticket.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Pestañas de la lista: todos los roles ven todos los tickets y pueden acotar la vista. */
enum class VistaTickets(val etiqueta: String) {
    TODOS("Todos"),
    MIOS("Míos"),
    ASIGNADOS("Asignados a mí")
}

class TicketListViewModel(
    repository: TicketRepository,
    private val sesion: SesionActual
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    private val _vista = MutableStateFlow(vistaInicial(sesion.usuario.value))
    val vista: StateFlow<VistaTickets> = _vista.asStateFlow()

    private val _filtro = MutableStateFlow<EstadoTicket?>(null)
    val filtro: StateFlow<EstadoTicket?> = _filtro.asStateFlow()

    val tickets: StateFlow<List<Ticket>> = combine(
        repository.observarTodos(), _vista, _filtro, sesion.usuario
    ) { lista, vista, estado, usuario ->
        val porVista = when (vista) {
            VistaTickets.TODOS -> lista
            VistaTickets.MIOS -> lista.filter { it.solicitanteId == usuario?.id }
            VistaTickets.ASIGNADOS -> lista.filter { it.asignadoId == usuario?.id }
        }
        if (estado == null) porVista else porVista.filter { it.estado == estado }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seleccionarVista(vista: VistaTickets) {
        _vista.value = vista
    }

    fun seleccionarFiltro(estado: EstadoTicket?) {
        _filtro.value = estado
    }

    fun cerrarSesion() = sesion.cerrar()

    private companion object {
        fun vistaInicial(usuario: Usuario?): VistaTickets = when (usuario?.rol) {
            Rol.FUNCIONARIO -> VistaTickets.MIOS
            Rol.AUXILIAR -> VistaTickets.ASIGNADOS
            else -> VistaTickets.TODOS
        }
    }
}
