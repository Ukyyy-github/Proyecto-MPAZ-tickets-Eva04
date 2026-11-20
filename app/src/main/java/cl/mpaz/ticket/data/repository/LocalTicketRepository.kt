package cl.mpaz.ticket.data.repository

import cl.mpaz.ticket.data.local.ComentarioDao
import cl.mpaz.ticket.data.local.TicketDao
import cl.mpaz.ticket.model.Categoria
import cl.mpaz.ticket.model.Comentario
import cl.mpaz.ticket.model.EstadoTicket
import cl.mpaz.ticket.model.Prioridad
import cl.mpaz.ticket.model.Rol
import cl.mpaz.ticket.model.Ticket
import cl.mpaz.ticket.model.Usuario
import kotlinx.coroutines.flow.Flow

class LocalTicketRepository(
    private val ticketDao: TicketDao,
    private val comentarioDao: ComentarioDao,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : TicketRepository {

    override fun observarVisibles(usuario: Usuario): Flow<List<Ticket>> = when (usuario.rol) {
        Rol.ENCARGADO_TI -> ticketDao.observarTodos()
        Rol.FUNCIONARIO -> ticketDao.observarPorSolicitante(usuario.id)
        Rol.AUXILIAR -> ticketDao.observarPorAsignado(usuario.id)
    }

    override fun observar(id: Long): Flow<Ticket?> = ticketDao.observarPorId(id)

    override fun observarHistorial(ticketId: Long): Flow<List<Comentario>> =
        comentarioDao.observarPorTicket(ticketId)

    override suspend fun obtener(id: Long): Ticket? = ticketDao.buscarPorId(id)

    override suspend fun crear(
        titulo: String,
        categoria: Categoria,
        ubicacion: String,
        descripcion: String,
        fotoPath: String?,
        solicitante: Usuario
    ): Long {
        val ahora = reloj()
        val id = ticketDao.insertar(
            Ticket(
                titulo = titulo.trim(),
                categoria = categoria,
                ubicacion = ubicacion.trim(),
                descripcion = descripcion.trim(),
                fotoPath = fotoPath,
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
        fotoPath: String?,
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
                fotoPath = fotoPath,
                actualizadoEn = ahora
            )
        )
        registrar(ticketId, autor, "Ticket editado", ahora)
    }

    override suspend fun eliminar(ticketId: Long) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        comentarioDao.eliminarPorTicket(ticketId)
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

    override suspend fun asignar(ticketId: Long, auxiliar: Usuario, autor: Usuario) {
        val ticket = ticketDao.buscarPorId(ticketId) ?: return
        val ahora = reloj()
        val nuevoEstado =
            if (ticket.estado == EstadoTicket.PENDIENTE || ticket.estado == EstadoTicket.REVISADO) {
                EstadoTicket.ASIGNADO
            } else ticket.estado
        ticketDao.actualizar(
            ticket.copy(
                asignadoId = auxiliar.id,
                asignadoNombre = auxiliar.nombre,
                estado = nuevoEstado,
                actualizadoEn = ahora
            )
        )
        val detalleEstado =
            if (nuevoEstado != ticket.estado) " (estado: ${nuevoEstado.etiqueta})" else ""
        registrar(ticketId, autor, "Asignado a ${auxiliar.nombre}$detalleEstado", ahora)
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
