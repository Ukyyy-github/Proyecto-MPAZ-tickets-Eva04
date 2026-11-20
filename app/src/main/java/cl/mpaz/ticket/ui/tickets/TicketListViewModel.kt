package cl.mpaz.ticket.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.ticket.data.repository.TicketRepository
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import cl.mpaz.ticket.session.SesionActual
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TicketListViewModel(
    repository: TicketRepository,
    private val sesion: SesionActual
) : ViewModel() {

    val usuario: StateFlow<Usuario?> = sesion.usuario

    private val _filtro = MutableStateFlow<EstadoTicket?>(null)
    val filtro: StateFlow<EstadoTicket?> = _filtro.asStateFlow()

    private val visibles = sesion.usuario
        .filterNotNull()
        .flatMapLatest { repository.observarVisibles(it) }

    val tickets: StateFlow<List<Ticket>> = combine(visibles, _filtro) { lista, estado ->
        if (estado == null) lista else lista.filter { it.estado == estado }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seleccionarFiltro(estado: EstadoTicket?) {
        _filtro.value = estado
    }

    fun cerrarSesion() = sesion.cerrar()
}
