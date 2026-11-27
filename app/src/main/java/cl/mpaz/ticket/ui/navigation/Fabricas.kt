package cl.mpaz.ticket.ui.navigation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.mpaz.ticket.AppContainer
import cl.mpaz.ticket.ui.auth.AuthViewModel
import cl.mpaz.ticket.ui.perfil.PerfilViewModel
import cl.mpaz.ticket.ui.tickets.TicketDetailViewModel
import cl.mpaz.ticket.ui.tickets.TicketFormViewModel
import cl.mpaz.ticket.ui.tickets.TicketListViewModel

/** Fábricas de ViewModel: conectan cada ViewModel con las dependencias del [AppContainer]. */
object Fabricas {
    fun auth(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(c.usuarioRepository, c.sesion) }
    }

    fun lista(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { TicketListViewModel(c.ticketRepository, c.sesion) }
    }

    fun formulario(c: AppContainer, ticketId: Long?): ViewModelProvider.Factory = viewModelFactory {
        initializer { TicketFormViewModel(c.ticketRepository, c.sesion, c.adjuntos, ticketId) }
    }

    fun detalle(c: AppContainer, ticketId: Long): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            TicketDetailViewModel(c.ticketRepository, c.usuarioRepository, c.sesion, c.adjuntos, ticketId)
        }
    }

    fun perfil(c: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { PerfilViewModel(c.usuarioRepository, c.ticketRepository, c.sesion) }
    }
}
