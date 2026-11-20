package cl.mpaz.ticket.util

import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario

/** Permisos por rol. Lógica pura: fácil de probar con pruebas unitarias. */
object ReglasTicket {

    fun puedeCrear(usuario: Usuario): Boolean = usuario.rol != Rol.AUXILIAR

    fun puedeEditar(usuario: Usuario, ticket: Ticket): Boolean = when (usuario.rol) {
        Rol.ENCARGADO_TI -> true
        Rol.FUNCIONARIO -> ticket.solicitanteId == usuario.id && ticket.estado == EstadoTicket.PENDIENTE
        Rol.AUXILIAR -> false
    }

    fun puedeEliminar(usuario: Usuario, ticket: Ticket): Boolean = puedeEditar(usuario, ticket)

    fun puedeAsignar(usuario: Usuario): Boolean = usuario.rol == Rol.ENCARGADO_TI

    fun puedePriorizar(usuario: Usuario): Boolean = usuario.rol == Rol.ENCARGADO_TI

    fun puedeComentar(usuario: Usuario, ticket: Ticket): Boolean =
        usuario.rol == Rol.ENCARGADO_TI ||
            ticket.solicitanteId == usuario.id ||
            ticket.asignadoId == usuario.id

    fun estadosPermitidos(usuario: Usuario, ticket: Ticket): List<EstadoTicket> = when (usuario.rol) {
        Rol.ENCARGADO_TI -> EstadoTicket.entries.filter { it != ticket.estado }
        Rol.AUXILIAR ->
            if (ticket.asignadoId == usuario.id && ticket.estado != EstadoTicket.CERRADO) {
                listOf(EstadoTicket.EN_PROCESO, EstadoTicket.SOLUCIONADO).filter { it != ticket.estado }
            } else emptyList()
        Rol.FUNCIONARIO ->
            if (ticket.solicitanteId == usuario.id && ticket.estado == EstadoTicket.SOLUCIONADO) {
                listOf(EstadoTicket.CERRADO)
            } else emptyList()
    }
}
