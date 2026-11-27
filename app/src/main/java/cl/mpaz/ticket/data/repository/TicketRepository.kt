package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a tickets (CRUD + flujo de trabajo + bitácora + adjuntos).
 * Implementación actual: Room ([LocalTicketRepository]). La remota (Retrofit) y la
 * sincronización offline se agregan como otra implementación de esta interfaz.
 */
interface TicketRepository {
    /** Todos los tickets: cualquier rol puede verlos (los permisos de acción dependen del rol). */
    fun observarTodos(): Flow<List<Ticket>>
    fun observar(id: Long): Flow<Ticket?>
    fun observarHistorial(ticketId: Long): Flow<List<Comentario>>
    fun observarAdjuntos(ticketId: Long): Flow<List<Adjunto>>
    suspend fun obtener(id: Long): Ticket?

    suspend fun crear(
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        solicitante: Usuario
    ): Long

    suspend fun editar(
        ticketId: Long,
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        autor: Usuario
    )

    suspend fun eliminar(ticketId: Long)
    suspend fun cambiarEstado(ticketId: Long, estado: EstadoTicket, autor: Usuario)
    suspend fun cambiarPrioridad(ticketId: Long, prioridad: Prioridad, autor: Usuario)
    suspend fun asignar(ticketId: Long, asignado: Usuario, autor: Usuario)
    suspend fun quitarAsignacion(ticketId: Long, autor: Usuario)
    suspend fun comentar(ticketId: Long, autor: Usuario, texto: String)

    suspend fun agregarAdjunto(
        ticketId: Long,
        nombre: String,
        tipoMime: String,
        ruta: String,
        tamano: Long,
        autor: Usuario
    )

    suspend fun eliminarAdjunto(adjuntoId: Long, autor: Usuario)
}
