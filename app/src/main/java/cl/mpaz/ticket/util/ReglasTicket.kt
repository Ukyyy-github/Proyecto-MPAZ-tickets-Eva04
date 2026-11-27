package cl.mpaz.ticket.util

import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario

/** Permisos por rol. Lógica pura: fácil de probar con pruebas unitarias. */
object ReglasTicket {

    const val MAX_ADJUNTOS = 10

    fun puedeCrear(usuario: Usuario): Boolean = usuario.rol != Rol.AUXILIAR

    fun puedeEditar(usuario: Usuario, ticket: Ticket): Boolean = when (usuario.rol) {
        Rol.ENCARGADO_TI -> true
        Rol.FUNCIONARIO -> ticket.solicitanteId == usuario.id && ticket.estado == EstadoTicket.PENDIENTE
        Rol.AUXILIAR -> false
    }

    fun puedeEliminar(usuario: Usuario, ticket: Ticket): Boolean = puedeEditar(usuario, ticket)

    fun puedeAsignar(usuario: Usuario): Boolean = usuario.rol == Rol.ENCARGADO_TI

    /** El encargado de TI puede tomar un ticket para sí mismo. */
    fun puedeAutoAsignarse(usuario: Usuario, ticket: Ticket): Boolean =
        usuario.rol == Rol.ENCARGADO_TI && ticket.asignadoId != usuario.id

    fun puedePriorizar(usuario: Usuario): Boolean = usuario.rol == Rol.ENCARGADO_TI

    /** Participan en el ticket: TI, quien lo reportó y quien lo tiene asignado. */
    fun puedeComentar(usuario: Usuario, ticket: Ticket): Boolean =
        usuario.rol == Rol.ENCARGADO_TI ||
            ticket.solicitanteId == usuario.id ||
            ticket.asignadoId == usuario.id

    /** Todos ven todos los tickets; quien no participa en uno lo ve en solo lectura. */
    fun esSoloLectura(usuario: Usuario, ticket: Ticket): Boolean = !puedeComentar(usuario, ticket)

    fun puedeAdjuntar(usuario: Usuario, ticket: Ticket): Boolean =
        ticket.estado != EstadoTicket.CERRADO && puedeComentar(usuario, ticket)

    fun puedeEliminarAdjunto(usuario: Usuario, adjunto: Adjunto): Boolean =
        usuario.rol == Rol.ENCARGADO_TI || adjunto.subidoPorId == usuario.id

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
