package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.data.local.AdjuntoDao
import cl.mpaz.ticket.data.local.ComentarioDao
import cl.mpaz.ticket.data.local.TicketDao
import cl.mpaz.ticket.model.Adjunto
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import kotlinx.coroutines.flow.Flow

class LocalTicketRepository(
    private val ticketDao: TicketDao,
    private val comentarioDao: ComentarioDao,
    private val adjuntoDao: AdjuntoDao,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : TicketRepository {

    override fun observarTodos(): Flow<List<Ticket>> = ticketDao.observarTodos()

    override fun observar(id: Long): Flow<Ticket?> = ticketDao.observarPorId(id)

    override fun observarHistorial(ticketId: Long): Flow<List<Comentario>> =
        comentarioDao.observarPorTicket(ticketId)

    override fun observarAdjuntos(ticketId: Long): Flow<List<Adjunto>> =
        adjuntoDao.observarPorTicket(ticketId)

    override suspend fun obtener(id: Long): Ticket? = ticketDao.buscarPorId(id)

    override suspend fun crear(
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        solicitante: Usuario
    ): Long {
        val ahora = reloj()
        val id = ticketDao.insertar(
            Ticket(
                titulo = titulo.trim(),
                categoria = categoria,
                ubicacion = ubicacion.trim(),
                descripcion = descripcion.trim(),
                solicitanteId = solicitante.id,
                solicitanteNombre = solicitante.nombre,
                creadoEn = ahora,
                actualizadoEn = ahora
            )
        )
        registrar(id, solicitante, "Ticket creado", ahora)
        return id
    }

    override suspend fun editar(
        ticketId: Long,
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        autor: Usuario
    ) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        val ahora = reloj()
        ticketDao.actualizar(
            ticket.copy(
                titulo = titulo.trim(),
                categoria = categoria,
                ubicacion = ubicacion.trim(),
                descripcion = descripcion.trim(),
                actualizadoEn = ahora
            )
        )
        registrar(ticketId, autor, "Ticket editado", ahora)
    }

    override suspend fun eliminar(ticketId: Long) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        comentarioDao.eliminarPorTicket(ticketId)
        adjuntoDao.eliminarPorTicket(ticketId)
        ticketDao.eliminar(ticket)
    }

    override suspend fun cambiarEstado(ticketId: Long, estado: EstadoTicket, autor: Usuario) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        if (ticket.estado == estado) return
        val ahora = reloj()
        ticketDao.actualizar(
            ticket.copy(
                estado = estado,
                actualizadoEn = ahora,
                resueltoEn = when (estado) {
                    EstadoTicket.SOLUCIONADO -> ahora
                    EstadoTicket.CERRADO -> ticket.resueltoEn ?: ahora
                    else -> null
                }
            )
        )
        registrar(ticketId, autor, "Estado: ${ticket.estado.etiqueta} → ${estado.etiqueta}", ahora)
    }

    override suspend fun cambiarPrioridad(ticketId: Long, prioridad: Prioridad, autor: Usuario) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        if (ticket.prioridad == prioridad) return
        val ahora = reloj()
        ticketDao.actualizar(ticket.copy(prioridad = prioridad, actualizadoEn = ahora))
        registrar(ticketId, autor, "Prioridad: ${ticket.prioridad.etiqueta} → ${prioridad.etiqueta}", ahora)
    }

    override suspend fun asignar(ticketId: Long, asignado: Usuario, autor: Usuario) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        val ahora = reloj()
        val nuevoEstado =
            if (ticket.estado == EstadoTicket.PENDIENTE || ticket.estado == EstadoTicket.REVISADO) {
                EstadoTicket.ASIGNADO
            } else ticket.estado
        ticketDao.actualizar(
            ticket.copy(
                asignadoId = asignado.id,
                asignadoNombre = asignado.nombre,
                estado = nuevoEstado,
                actualizadoEn = ahora
            )
        )
        val detalleEstado =
            if (nuevoEstado != ticket.estado) " (estado: ${nuevoEstado.etiqueta})" else ""
        registrar(ticketId, autor, "Asignado a ${asignado.nombre}$detalleEstado", ahora)
    }

    override suspend fun quitarAsignacion(ticketId: Long, autor: Usuario) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        if (ticket.asignadoId == null) return
        val ahora = reloj()
        // Un ticket "Asignado" sin responsable vuelve a "Revisado"; los demás estados se mantienen.
        val nuevoEstado =
            if (ticket.estado == EstadoTicket.ASIGNADO) EstadoTicket.REVISADO else ticket.estado
        ticketDao.actualizar(
            ticket.copy(
                asignadoId = null,
                asignadoNombre = null,
                estado = nuevoEstado,
                actualizadoEn = ahora
            )
        )
        val detalleEstado =
            if (nuevoEstado != ticket.estado) " (estado: ${nuevoEstado.etiqueta})" else ""
        registrar(ticketId, autor, "Asignación quitada (era ${ticket.asignadoNombre})$detalleEstado", ahora)
    }

    override suspend fun comentar(ticketId: Long, autor: Usuario, texto: String) {
        val ahora = reloj()
        comentarioDao.insertar(
            Comentario(
                ticketId = ticketId,
                autorId = autor.id,
                autorNombre = autor.nombre,
                texto = texto.trim(),
                creadoEn = ahora
            )
        )
    }

    override suspend fun agregarAdjunto(
        ticketId: Long,
        nombre: String,
        tipoMime: String,
        ruta: String,
        tamano: Long,
        autor: Usuario
    ) {
        val ahora = reloj()
        adjuntoDao.insertar(
            Adjunto(
                ticketId = ticketId,
                nombre = nombre,
                tipoMime = tipoMime,
                ruta = ruta,
                tamano = tamano,
                subidoPorId = autor.id,
                subidoPorNombre = autor.nombre,
                creadoEn = ahora
            )
        )
        registrar(ticketId, autor, "Documento agregado: $nombre", ahora)
    }

    override suspend fun eliminarAdjunto(adjuntoId: Long, autor: Usuario) {
        val adjunto = adjuntoDao.buscarPorId(adjuntoId) ?: return
        adjuntoDao.eliminar(adjunto)
        registrar(adjunto.ticketId, autor, "Documento eliminado: ${adjunto.nombre}", reloj())
    }

    private suspend fun registrar(ticketId: Long, autor: Usuario, texto: String, ahora: Long) {
        comentarioDao.insertar(
            Comentario(
                ticketId = ticketId,
                autorId = autor.id,
                autorNombre = autor.nombre,
                texto = texto,
                esCambioAutomatico = true,
                creadoEn = ahora
            )
        )
    }
}
