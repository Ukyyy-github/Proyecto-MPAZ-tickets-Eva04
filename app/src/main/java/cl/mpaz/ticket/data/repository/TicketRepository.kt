package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a tickets (CRUD + flujo de trabajo + bitácora).
 * Implementación actual: Room ([LocalTicketRepository]). La remota (Retrofit) y la
 * sincronización offline se agregan como otra implementación de esta interfaz.
 */
interface TicketRepository {
    /** Tickets que el usuario puede ver según su rol. */
    fun observarVisibles(usuario: Usuario): Flow<List<Ticket>>
    fun observar(id: Long): Flow<Ticket?>
    fun observarHistorial(ticketId: Long): Flow<List<Comentario>>
    suspend fun obtener(id: Long): Ticket?

    suspend fun crear(
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        fotoPath: String?,
        solicitante: Usuario
    ): Long

    suspend fun editar(
        ticketId: Long,
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        fotoPath: String?,
        autor: Usuario
    )

    suspend fun eliminar(ticketId: Long)
    suspend fun cambiarEstado(ticketId: Long, estado: EstadoTicket, autor: Usuario)
    suspend fun cambiarPrioridad(ticketId: Long, prioridad: Prioridad, autor: Usuario)
    suspend fun asignar(ticketId: Long, auxiliar: Usuario, autor: Usuario)
    suspend fun comentar(ticketId: Long, autor: Usuario, texto: String)
}
